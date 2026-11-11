package id.nxtup.queue;

import java.time.Duration;

/**
 * Aturan main antrean. Semua angka di sini adalah knob produk, bukan konstanta teknis.
 *
 * @param slots               berapa orang bisa main bareng streamer pada satu waktu (1 = giliran 1-on-1)
 * @param callTimeout         lama peserta punya waktu merespons saat dipanggil
 * @param maxCallAttempts     berapa kali dipanggil sebelum akhirnya dikeluarkan
 * @param agingPerSkipSeconds bonus "wait virtual" tiap kali dilewati saat pemilihan (anti-starvation)
 * @param starvationCapSkips  batas keras: dilewati sekian kali -> wajib dipanggil berikutnya
 * @param requeuePenaltySeconds penalti setelah no-show (wait dihitung dari sekarang + penalti)
 */
public record QueueConfig(
        int slots,
        Duration callTimeout,
        int maxCallAttempts,
        int agingPerSkipSeconds,
        int starvationCapSkips,
        int requeuePenaltySeconds) {

    public QueueConfig {
        if (slots < 1) throw new IllegalArgumentException("slots minimal 1");
        if (callTimeout == null || callTimeout.isNegative() || callTimeout.isZero())
            throw new IllegalArgumentException("callTimeout harus positif");
        if (maxCallAttempts < 1) throw new IllegalArgumentException("maxCallAttempts minimal 1");
        if (agingPerSkipSeconds < 0) throw new IllegalArgumentException("aging tidak boleh negatif");
        if (starvationCapSkips < 1) throw new IllegalArgumentException("starvationCapSkips minimal 1");
        if (requeuePenaltySeconds < 0) throw new IllegalArgumentException("penalti tidak boleh negatif");
    }

    public static QueueConfig defaults() {
        return new QueueConfig(1, Duration.ofSeconds(60), 3, 30, 8, 120);
    }

    public QueueConfig withSlots(int n) {
        return new QueueConfig(n, callTimeout, maxCallAttempts, agingPerSkipSeconds, starvationCapSkips, requeuePenaltySeconds);
    }

    public QueueConfig withCallTimeout(Duration d) {
        return new QueueConfig(slots, d, maxCallAttempts, agingPerSkipSeconds, starvationCapSkips, requeuePenaltySeconds);
    }
}
