package id.nxtup.queue;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Test tanpa dependency (JUnit belum dipasang supaya bisa jalan offline dengan javac saja).
 * Kalau nanti masuk Spring Boot, pindahkan isi check() ke @Test JUnit 5 — logikanya sama.
 *
 * Jalankan: java -cp build/classes id.nxtup.queue.QueueEngineTest
 */
public final class QueueEngineTest {

    private static int passed = 0;
    private static int failed = 0;
    private static boolean quiet = false;
    private static final java.util.List<String> failures = new java.util.ArrayList<>();

    /**
     * Dipakai adapter JUnit (QueueEngineJUnitTest) supaya assertion yang sama
     * bisa dijalankan lewat IDE/Maven TANPA menduplikasi isi test.
     * Return daftar nama pemeriksaan yang gagal (kosong = grup lulus).
     */
    static java.util.List<String> runGroup(Runnable group) {
        failures.clear();
        boolean prev = quiet;
        quiet = true;
        try {
            group.run();
        } finally {
            quiet = prev;
        }
        return java.util.List.copyOf(failures);
    }

    public static void main(String[] args) {
        fifoUntukTierSama();
        subscriberMenangSaatWaitSama();
        agingMengalahkanSubscriberBaru();
        batasKerasAntiStarvation();
        joinGandaTidakBikinEntriBaru();
        slotPenuhMenolakPanggilan();
        timeoutRequeueDenganPenalti();
        attemptHabisDikeluarkan();
        finishMembebaskanSlot();
        leaveSaatMainMembebaskanSlot();
        skipOlehStreamer();
        posisiKonsistenDenganUrutan();
        seriDipecahkanUrutanJoin();
        requeueTidakMenghukumSelamanya();
        eventTercatat();
        configDivalidasi();

        System.out.println();
        System.out.println("=====================================");
        System.out.printf("lulus: %d   gagal: %d%n", passed, failed);
        System.out.println("=====================================");
        if (failed > 0) System.exit(1);
    }

    // ------------------------------------------------------------------ kasus

    static void fifoUntukTierSama() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.VIEWER);
        c.advanceSeconds(10);
        q.join("b", "B", Tier.VIEWER);
        c.advanceSeconds(10);
        q.join("c", "C", Tier.VIEWER);
        check("tier sama -> yang join dulu dipanggil dulu", picked(q, "a"));
    }

    static void subscriberMenangSaatWaitSama() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("v", "Viewer", Tier.VIEWER);
        q.join("s", "Subscriber", Tier.SUBSCRIBER);
        c.advanceSeconds(60);
        check("wait sama -> bobot subscriber menang", picked(q, "s"));
        check("skor subscriber > viewer", q.score(q.find("s").orElseThrow()) > q.score(q.find("v").orElseThrow()));
    }

    static void agingMengalahkanSubscriberBaru() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("v", "ViewerLama", Tier.VIEWER);
        q.join("s1", "SubBaru1", Tier.SUBSCRIBER);
        c.advanceSeconds(60);
        check("panggilan 1: subscriber baru menang", picked(q, "s1"));
        q.confirm("s1");
        q.finish("s1");
        q.join("s2", "SubBaru2", Tier.SUBSCRIBER);
        c.advanceSeconds(60);
        check("panggilan 2: viewer yang dilewati naik lewat aging", picked(q, "v"));
    }

    static void batasKerasAntiStarvation() {
        SettableClock c = clock();
        // aging dimatikan + penalti raksasa: skor viewer pasti paling rendah.
        QueueConfig cfg = new QueueConfig(1, Duration.ofSeconds(30), 5, 0, 2, 100_000);
        QueueEngine q = new QueueEngine(cfg, c);
        q.join("v", "ViewerSial", Tier.VIEWER);
        q.callNext();
        c.advanceSeconds(30);
        q.tick(); // no-show -> penalti besar
        for (int i = 1; i <= 2; i++) {
            q.join("s" + i, "Sub" + i, Tier.SUBSCRIBER);
            c.advanceSeconds(60);
            check("starvation: panggilan " + i + " masih subscriber", picked(q, "s" + i));
            q.confirm("s" + i);
            q.finish("s" + i);
        }
        check("skor viewer negatif (efek penalti)", q.score(q.find("v").orElseThrow()) < 0);
        q.join("s3", "Sub3", Tier.SUBSCRIBER);
        c.advanceSeconds(60);
        check("skips >= cap -> viewer wajib dipanggil", picked(q, "v"));
    }

    static void joinGandaTidakBikinEntriBaru() {
        QueueEngine q = engine(clock());
        Entry first = q.join("a", "A", Tier.VIEWER);
        Entry again = q.join("a", "A", Tier.SUBSCRIBER);
        check("join ganda idempoten", first == again);
        check("antrean tetap 1 orang", q.waiting().size() == 1);
        check("tier tidak berubah karena spam join", again.tier() == Tier.VIEWER);
    }

    static void slotPenuhMenolakPanggilan() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.VIEWER);
        q.join("b", "B", Tier.VIEWER);
        q.callNext();
        q.confirm("a");
        check("slot penuh -> callNext kosong", q.callNext().isEmpty());
        check("slot terhitung 1/1", q.occupiedSlots() == 1);
        check("peserta CALLED juga mengunci slot", slotTerkunciSaatCalled());
    }

    private static boolean slotTerkunciSaatCalled() {
        QueueEngine q = engine(clock());
        q.join("a", "A", Tier.VIEWER);
        q.join("b", "B", Tier.VIEWER);
        q.callNext();
        return q.occupiedSlots() == 1 && q.callNext().isEmpty();
    }

    static void timeoutRequeueDenganPenalti() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.VIEWER);
        q.callNext();
        c.advanceSeconds(60);
        check("tick melaporkan 1 timeout", q.tick() == 1);
        Entry a = q.find("a").orElseThrow();
        check("no-show -> balik WAITING", a.state() == EntryState.WAITING);
        check("no-show tercatat", a.noShows() == 1);
        q.join("b", "B", Tier.VIEWER);
        c.advanceSeconds(10);
        check("pendatang baru menyalip yang no-show", q.positionOf("b").orElse(-1) == 1);
        check("yang no-show turun ke posisi 2", q.positionOf("a").orElse(-1) == 2);
    }

    static void attemptHabisDikeluarkan() {
        SettableClock c = clock();
        QueueConfig cfg = new QueueConfig(1, Duration.ofSeconds(30), 2, 30, 8, 120);
        QueueEngine q = new QueueEngine(cfg, c);
        q.join("a", "A", Tier.VIEWER);
        q.callNext();
        c.advanceSeconds(30);
        q.tick();
        check("attempt 1 -> masih di antrean", q.find("a").orElseThrow().state() == EntryState.WAITING);
        q.callNext();
        c.advanceSeconds(30);
        q.tick();
        check("attempt habis -> dikeluarkan", q.find("a").orElseThrow().state() == EntryState.NO_SHOW);
        check("tidak lagi dihitung menunggu", q.waiting().isEmpty());
        check("posisi kosong setelah dikeluarkan", q.positionOf("a").isEmpty());
    }

    static void finishMembebaskanSlot() {
        QueueEngine q = engine(clock());
        q.join("a", "A", Tier.VIEWER);
        q.join("b", "B", Tier.VIEWER);
        q.join("c", "C", Tier.VIEWER);
        q.callNext();
        q.confirm("a");
        check("finish membebaskan slot", q.finish("a") && q.occupiedSlots() == 0);
        check("giliran lanjut ke B", picked(q, "b"));
        check("confirm peserta yang masih WAITING ditolak", !q.confirm("c"));
        check("finish peserta yang belum main ditolak", !q.finish("c"));
    }

    static void leaveSaatMainMembebaskanSlot() {
        QueueEngine q = engine(clock());
        q.join("a", "A", Tier.VIEWER);
        q.join("b", "B", Tier.VIEWER);
        q.callNext();
        q.confirm("a");
        check("leave saat PLAYING sukses", q.leave("a"));
        check("slot bebas setelah leave", q.occupiedSlots() == 0);
        check("B bisa dipanggil", picked(q, "b"));
        check("leave dua kali ditolak", !q.leave("a"));
    }

    static void skipOlehStreamer() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.VIEWER);
        q.callNext();
        check("skip peserta CALLED sukses", q.skip("a"));
        check("skip -> balik WAITING", q.find("a").orElseThrow().state() == EntryState.WAITING);
        check("skip dihitung sebagai no-show", q.find("a").orElseThrow().noShows() == 1);
        check("skip peserta WAITING ditolak", !q.skip("a"));
    }

    static void posisiKonsistenDenganUrutan() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.VIEWER);
        c.advanceSeconds(5);
        q.join("b", "B", Tier.FOLLOWER);
        c.advanceSeconds(5);
        q.join("s", "S", Tier.SUBSCRIBER);
        c.advanceSeconds(30);
        List<Entry> order = q.waitingOrder();
        boolean ok = true;
        for (int i = 0; i < order.size(); i++) {
            ok &= q.positionOf(order.get(i).userId()).orElse(-1) == i + 1;
        }
        check("positionOf cocok dengan waitingOrder", ok);
        check("waitingOrder terurut skor menurun",
                q.score(order.get(0)) >= q.score(order.get(1)) && q.score(order.get(1)) >= q.score(order.get(2)));
    }

    static void seriDipecahkanUrutanJoin() {
        QueueEngine q = engine(clock());
        q.join("z", "Z", Tier.VIEWER);
        q.join("y", "Y", Tier.VIEWER);
        check("skor seri -> join lebih dulu menang", picked(q, "z"));
    }

    static void requeueTidakMenghukumSelamanya() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.SUBSCRIBER);
        q.callNext();
        c.advanceSeconds(60);
        q.tick();
        q.join("b", "B", Tier.VIEWER);
        c.advanceSeconds(10);
        check("sesaat setelah penalti: B duluan", picked(q, "b"));
        q.confirm("b");
        q.finish("b");
        c.advanceSeconds(300);
        check("setelah penalti lewat: A kembali kompetitif", picked(q, "a"));
    }

    static void eventTercatat() {
        SettableClock c = clock();
        QueueEngine q = engine(c);
        q.join("a", "A", Tier.VIEWER);
        q.callNext();
        c.advanceSeconds(60);
        q.tick();
        List<String> types = q.events().stream().map(Event::type).toList();
        check("event JOIN/CALL/NO_SHOW tercatat",
                types.contains("JOIN") && types.contains("CALL") && types.contains("NO_SHOW"));
        check("event tidak bisa diubah dari luar", tidakBisaUbahEvents(q));
    }

    private static boolean tidakBisaUbahEvents(QueueEngine q) {
        try {
            q.events().add(new Event(null, "PALSU", "x", ""));
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    static void configDivalidasi() {
        boolean thrown = false;
        try {
            new QueueConfig(0, Duration.ofSeconds(60), 3, 30, 8, 120);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("config slots=0 ditolak", thrown);
    }

    // ------------------------------------------------------------------ util

    private static SettableClock clock() {
        return SettableClock.at("2026-11-11T10:00:00Z");
    }

    private static QueueEngine engine(SettableClock c) {
        return new QueueEngine(QueueConfig.defaults(), c);
    }

    private static boolean picked(QueueEngine q, String userId) {
        Optional<Entry> e = q.callNext();
        return e.isPresent() && e.get().userId().equals(userId);
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            if (!quiet) System.out.println("  OK   " + name);
        } else {
            failed++;
            failures.add(name);
            if (!quiet) System.out.println("  FAIL " + name);
        }
    }
}
