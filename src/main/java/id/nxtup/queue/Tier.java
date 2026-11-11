package id.nxtup.queue;

/**
 * Kelas penonton. Bobot menentukan seberapa cepat antrean dia "menua".
 * CATATAN: angka ini masih asumsi produk, belum dikonfirmasi pemilik proyek.
 * Nanti pindah ke konfigurasi/DB, jangan hardcode di enum.
 */
public enum Tier {
    VIEWER(1.0),
    FOLLOWER(1.25),
    SUBSCRIBER(1.6);

    private final double weight;

    Tier(double weight) {
        this.weight = weight;
    }

    public double weight() {
        return weight;
    }
}
