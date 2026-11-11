# Draft pesan ke caesar (siap copy-paste)

Prinsip: satu topik, pertanyaan tertutup, effort jawab rendah, tawaran berbatas.
Jangan kirim "gimana mas?" kosong — itu menambah beban tanpa mempermudah jawaban.

---

## A. Kalau pesan terakhirmu (04:30) belum dibalas

> mas, lanjut yang nxtup. biar saya nggak salah bayangin, ini antrean viewer yang mau giliran main
> bareng streamer, atau antrean request (lagu/konten)?
>
> sambil nunggu, saya iseng bikin prototipe kecil logika antreannya di Java — belum nyambung ke
> apa-apa, cuma aturan mainnya: FIFO berbobot (subscriber dapet bobot lebih, tapi yang nunggu lama
> tetap naik), timeout kalau dipanggil nggak respons, terus di-requeue pakai penalti. ada test-nya
> juga, 44 kasus jalan.
>
> kalau mau saya kirim output simulasinya, tinggal bilang. kalau ternyata arahmu beda, ya buang aja,
> murah kok bikinnya.

Kenapa begini: kamu sudah punya barang untuk ditunjukkan, tapi tidak menagih jawaban dan tidak
menjanjikan apa pun. Kalau dia tertarik, dia yang minta.

## B. Kalau dia jawab "masih from scratch"

> oke. sebelum nyentuh kode, saya tulisin dulu aturan mainnya ya — antrean itu 90% kebijakan,
> 10% kode. saya kirim 8 pertanyaan singkat, dijawab seadanya aja:
>
> 1. giliran 1-on-1 atau party beberapa orang?
> 2. viewer masuk lewat chat Twitch/YouTube, bot Discord, atau web?
> 3. subscriber prioritas atau semua FIFO?
> 4. dipanggil nggak respons → keluar atau balik antrean?
> 5. antrean perlu selamat kalau app restart?
>
> dari situ saya bisa kunci modelnya dulu, baru kita pilih frameworknya. kalau kamu udah ada
> preferensi (Spring Boot? Quarkus?), ikut kamu aja.

## C. Kalau dia jawab "udah ada kode / repo"

> share repo-nya dong mas, saya baca dulu strukturnya biar nggak bikin versi tandingan.
> saya nggak akan push apa-apa sebelum kamu setuju.
>
> btw jujur: harian saya di Laravel, jadi Java saya butuh adaptasi dikit. yang bisa saya pegang
> cepat itu aturan antrean + test-nya (sudah saya prototipe-in), atau dokumentasi konsep.
> kalau mau saya masuk ke kode Java-nya, kasih issue kecil dulu aja buat pemanasan.

---

## Yang perlu kamu putuskan sendiri sebelum kirim

Pilih satu dan sebut eksplisit, jangan digantung:

- **Belajar/iseng** → "saya bikin buat belajar, kalau kepakai syukur" (tanpa ekspektasi apa pun)
- **Kontributor** → "saya pegang modul X, kamu yang review & merge"
- **Berbayar** → "kalau ini masuk kerjaan, kita ngomong scope & rate dulu ya"

Frasa yang sebaiknya dihindari: *"nanti saya coba"* tanpa batas, dan *"saya bikinin semuanya"*.
Dua-duanya bikin susah menentukan harga atau berhenti di tengah jalan.
