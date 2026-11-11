package id.nxtup.queue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Inti proyek nxtup: mesin antrean giliran main.
 *
 * Sengaja TANPA framework, tanpa database, tanpa Spring. Alasannya:
 *  - bisa dibaca & direview pemilik proyek dalam 5 menit,
 *  - bisa ditempel ke stack apa pun (Spring Boot, Quarkus, Android, plain Java),
 *  - semua aturan produk teruji tanpa perlu infra.
 *
 * Belum thread-safe. Untuk produksi: bungkus dengan lock tunggal per-queue
 * atau jalankan single-threaded per streamer (satu antrean = satu actor).
 */
public final class QueueEngine {

    private final QueueConfig config;
    private final Clock clock;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final List<Event> events = new ArrayList<>();
    private long seqCounter = 0;

    public QueueEngine(QueueConfig config, Clock clock) {
        this.config = config;
        this.clock = clock;
    }

    public QueueConfig config() { return config; }

    // ---------------------------------------------------------------- perintah

    /** Idempoten: user yang masih aktif tidak bikin entri ganda. */
    public Entry join(String userId, String displayName, Tier tier) {
        Entry existing = entries.get(userId);
        if (existing != null && existing.state().isActive()) {
            log("JOIN_DITOLAK", userId, "sudah ada di antrean");
            return existing;
        }
        Entry e = new Entry(seqCounter++, userId, displayName, tier, now());
        entries.put(userId, e);
        log("JOIN", userId, tier + " #" + e.seq());
        return e;
    }

    public boolean leave(String userId) {
        Entry e = entries.get(userId);
        if (e == null || !e.state().isActive()) return false;
        e.state(EntryState.LEFT);
        log("LEAVE", userId, "keluar sendiri");
        return true;
    }

    /**
     * Panggil peserta berikutnya. Otomatis membersihkan yang timeout dulu.
     * Kosong kalau slot penuh atau tidak ada yang menunggu.
     */
    public Optional<Entry> callNext() {
        tick();
        if (occupiedSlots() >= config.slots()) {
            log("CALL_DITOLAK", "-", "slot penuh (" + occupiedSlots() + "/" + config.slots() + ")");
            return Optional.empty();
        }
        Entry picked = selectNext();
        if (picked == null) return Optional.empty();

        picked.state(EntryState.CALLED);
        picked.calledAt(now());
        picked.incCallAttempts();
        for (Entry e : waiting()) {
            if (e != picked) e.incSkips();
        }
        picked.resetSkips();
        log("CALL", picked.userId(), picked.displayName() + " attempt " + picked.callAttempts());
        return Optional.of(picked);
    }

    /** Peserta merespons panggilan -> mulai main, slot terkunci. */
    public boolean confirm(String userId) {
        Entry e = entries.get(userId);
        if (e == null || e.state() != EntryState.CALLED) return false;
        e.state(EntryState.PLAYING);
        log("CONFIRM", userId, "mulai main");
        return true;
    }

    /** Giliran selesai -> slot bebas. */
    public boolean finish(String userId) {
        Entry e = entries.get(userId);
        if (e == null || e.state() != EntryState.PLAYING) return false;
        e.state(EntryState.DONE);
        log("FINISH", userId, "giliran selesai");
        return true;
    }

    /** Streamer melewati/menendang peserta yang sedang dipanggil. */
    public boolean skip(String userId) {
        Entry e = entries.get(userId);
        if (e == null || e.state() != EntryState.CALLED) return false;
        handleNoShow(e, "dilewati streamer");
        return true;
    }

    /**
     * Dipanggil periodik (mis. tiap detik). Mengubah CALLED yang lewat batas
     * menjadi no-show, lalu requeue atau keluarkan. Return jumlah yang kedaluwarsa.
     */
    public int tick() {
        int expired = 0;
        for (Entry e : entries.values()) {
            if (e.state() == EntryState.CALLED
                    && Duration.between(e.calledAt(), now()).compareTo(config.callTimeout()) >= 0) {
                handleNoShow(e, "timeout " + config.callTimeout().toSeconds() + "s");
                expired++;
            }
        }
        return expired;
    }

    private void handleNoShow(Entry e, String reason) {
        e.incNoShows();
        if (e.callAttempts() >= config.maxCallAttempts()) {
            e.state(EntryState.NO_SHOW);
            log("KELUAR", e.userId(), reason + ", attempt habis");
            return;
        }
        e.state(EntryState.WAITING);
        e.calledAt(null);
        e.resetSkips();
        // Kehilangan akumulasi tunggu + penalti: dia turun di bawah pendatang baru,
        // tapi aging tetap menjamin dia tidak hilang selamanya.
        e.effectiveSince(now().plusSeconds(config.requeuePenaltySeconds()));
        log("NO_SHOW", e.userId(), reason + ", requeue dengan penalti " + config.requeuePenaltySeconds() + "s");
    }

    // ---------------------------------------------------------------- pembacaan

    /** Skor pemilihan. Terbuka supaya bisa ditampilkan di UI ("kenapa dia duluan?"). */
    public double score(Entry e) {
        long wait = Duration.between(e.effectiveSince(), now()).toSeconds();
        double aging = (double) e.skips() * config.agingPerSkipSeconds();
        return (wait + aging) * e.tier().weight();
    }

    /** Urutan antrean saat ini: skor tertinggi dulu, seri dipecahkan urutan join. */
    public List<Entry> waitingOrder() {
        List<Entry> list = new ArrayList<>(waiting());
        list.sort(Comparator.comparingDouble(this::score).reversed()
                .thenComparingLong(Entry::seq));
        return list;
    }

    /** Posisi 1-based seperti yang dilihat penonton di overlay. */
    public OptionalInt positionOf(String userId) {
        List<Entry> order = waitingOrder();
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).userId().equals(userId)) return OptionalInt.of(i + 1);
        }
        return OptionalInt.empty();
    }

    public Optional<Entry> find(String userId) {
        return Optional.ofNullable(entries.get(userId));
    }

    public List<Entry> waiting() {
        return entries.values().stream().filter(e -> e.state() == EntryState.WAITING).toList();
    }

    public int occupiedSlots() {
        return (int) entries.values().stream()
                .filter(e -> e.state() == EntryState.PLAYING || e.state() == EntryState.CALLED)
                .count();
    }

    public List<Event> events() {
        return List.copyOf(events);
    }

    /** Payload siap kirim ke overlay: siapa yang main, siapa berikutnya. */
    public String snapshot() {
        StringBuilder sb = new StringBuilder();
        List<Entry> busy = entries.values().stream()
                .filter(e -> e.state() == EntryState.PLAYING || e.state() == EntryState.CALLED).toList();
        sb.append("slot ").append(occupiedSlots()).append('/').append(config.slots());
        sb.append(busy.isEmpty() ? " [kosong]" : " " + busy);
        sb.append(" | antrean: ");
        List<Entry> order = waitingOrder();
        if (order.isEmpty()) sb.append("(kosong)");
        for (int i = 0; i < order.size(); i++) {
            Entry e = order.get(i);
            sb.append(i == 0 ? "" : ", ")
              .append(i + 1).append('.').append(e.displayName())
              .append('[').append(e.tier()).append(" skor=").append(Math.round(score(e))).append(']');
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- internal

    private Entry selectNext() {
        List<Entry> waiting = waiting();
        if (waiting.isEmpty()) return null;

        // Batas keras anti-starvation: yang sudah dilewati terlalu sering menang mutlak.
        List<Entry> starved = waiting.stream()
                .filter(e -> e.skips() >= config.starvationCapSkips()).toList();
        if (!starved.isEmpty()) {
            return starved.stream()
                    .min(Comparator.comparing((Entry e) -> e.effectiveSince()).thenComparingLong(Entry::seq))
                    .orElseThrow();
        }
        return waitingOrder().get(0);
    }

    private Instant now() {
        return clock.instant();
    }

    private void log(String type, String userId, String detail) {
        events.add(new Event(now(), type, userId, detail));
    }
}
