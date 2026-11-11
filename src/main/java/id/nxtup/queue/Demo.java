package id.nxtup.queue;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Simulasi satu sesi mabar: bisa dijalankan tanpa server, tanpa DB.
 * Ini yang dipakai untuk menunjukkan konsep ke pemilik proyek.
 */
public final class Demo {

    private static final DateTimeFormatter JAM =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);

    private static SettableClock clock;
    private static QueueEngine q;

    public static void main(String[] args) {
        clock = SettableClock.at("2026-11-11T20:00:00Z");
        q = new QueueEngine(QueueConfig.defaults(), clock);

        System.out.println("=== nxtup - simulasi antrean giliran main (1 slot) ===");
        System.out.println("aturan: " + q.config());
        System.out.println();

        q.join("rina", "Rina", Tier.VIEWER);
        maju(5);
        q.join("dika", "Dika", Tier.FOLLOWER);
        maju(5);
        q.join("bagas", "Bagas", Tier.VIEWER);
        maju(10);
        q.join("sari", "Sari", Tier.SUBSCRIBER);
        cetak("4 penonton masuk. Subscriber BELUM otomatis nomor 1 - lama tunggu ikut dihitung");

        panggil("Rina yang paling lama menunggu");
        q.confirm("rina");
        cetak("Rina respons, slot terkunci");

        maju(180);
        q.finish("rina");
        cetak("giliran Rina selesai (3 menit)");

        panggil("Sari (subscriber) sekarang paling tinggi skornya");
        q.confirm("sari");
        maju(90);
        q.finish("sari");
        cetak("Sari selesai");

        panggil("Dika (follower)");
        maju(10);
        cetak("Dika dipanggil tapi tidak respons...");
        maju(60);
        int expired = q.tick();
        cetak("timeout " + expired + " orang -> Dika di-requeue dengan penalti, posisinya turun");

        panggil("Bagas naik ke depan");
        q.confirm("bagas");
        maju(120);
        q.finish("bagas");
        cetak("Bagas selesai");

        q.join("yoga", "Yoga", Tier.VIEWER);
        cetak("Yoga baru masuk");
        panggil("Dika sudah kompetitif lagi - penalti bukan hukuman permanen");
        q.confirm("dika");
        maju(60);
        q.finish("dika");

        panggil("giliran Yoga");

        System.out.println();
        System.out.println("=== jejak kejadian (siap dikirim ke overlay via WebSocket) ===");
        q.events().forEach(e ->
                System.out.println("  " + JAM.format(e.at()) + "  " + e));

        System.out.println();
        System.out.println("=== kondisi akhir ===");
        System.out.println("  " + q.snapshot());
        System.out.println("  total event: " + q.events().size());
    }

    private static void maju(long detik) {
        clock.advance(Duration.ofSeconds(detik));
    }

    private static void panggil(String catatan) {
        q.callNext().ifPresentOrElse(
                e -> System.out.println(">> " + JAM.format(clock.instant()) + "  DIPANGGIL: "
                        + e.displayName() + "  (" + catatan + ")"),
                () -> System.out.println(">> " + JAM.format(clock.instant()) + "  tidak ada yang bisa dipanggil"));
    }

    private static void cetak(String catatan) {
        System.out.println("   " + JAM.format(clock.instant()) + "  " + catatan);
        System.out.println("   " + q.snapshot());
        System.out.println();
    }
}
