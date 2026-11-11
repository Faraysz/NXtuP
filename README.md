# nxtup — langkah awal

Bahan kerja untuk memulai proyek **nxtup** (manajemen antrean untuk streamer, stack Java).
Proyek Maven, target Java 17, **nol dependency runtime** (JUnit hanya untuk test).

> **Posisi dokumen ini.** Konsep final belum dikirim pemilik proyek (caesar). Yang ada di sini
> adalah *spike*: usulan aturan antrean + prototipe inti yang sudah teruji, untuk dipakai sebagai
> bahan diskusi. Ini **bukan** kode resmi proyek dan jangan di-push ke repo dia sebelum ada kesepakatan.
> Semua asumsi ditandai di `docs/konsep.md`.

---

## Isi folder

```
nxtup/
├── pom.xml                   <- proyek Maven: dibuka NetBeans/IDEA/VS Code tanpa konfigurasi
├── run.sh                    <- jalur offline: compile + test + simulasi, cukup JDK
├── README.md                 <- rencana langkah 0-5 (file ini)
├── src/main/java/id/nxtup/queue/
│   ├── QueueEngine.java      <- inti: join/call/confirm/finish/no-show/posisi
│   ├── QueueConfig.java      <- semua knob aturan main (slot, timeout, penalti, aging)
│   ├── Entry.java, EntryState.java, Tier.java
│   ├── Event.java            <- jejak kejadian, nanti jadi payload WebSocket
│   ├── SettableClock.java    <- jam bisa digeser -> timeout bisa dites tanpa sleep
│   └── Demo.java             <- simulasi 1 sesi mabar, buat ditunjukkan ke caesar
├── src/test/java/id/nxtup/queue/
│   ├── QueueEngineJUnitTest.java  <- 16 @Test untuk IDE / `mvn test`
│   └── QueueEngineTest.java       <- harness 44 pemeriksaan, tanpa dependency
└── docs/
    ├── konsep.md                      <- spesifikasi draft + 8 pertanyaan yang wajib dijawab
    ├── pesan-caesar.md                <- draft pesan siap kirim (3 skenario)
    ├── ide-netbeans-dan-alternatif.md <- langkah buka di NetBeans + pilihan IDE lain
    ├── setup-java-untuk-dev-laravel.md<- peta pindah stack + perintah harian
    └── contoh-output.txt              <- hasil run asli, buat ditunjukkan tanpa install Java
```

---

## Langkah 0 — kunci 3 hal dulu, sebelum nambah kode (hari ini, ~15 menit)

Jangan mulai dari coding. Tiga hal ini menentukan apakah kerjaanmu nanti kepakai atau dibuang:

- [ ] **Use case pasti**: antrean *giliran main bareng streamer* (asumsi sekarang), antrean *request lagu/konten*, atau *matchmaking*? Tiga produk berbeda.
- [ ] **Status kode**: from scratch atau sudah ada repo? Kalau sudah ada, minta akses baca dulu — jangan bikin tandingan.
- [ ] **Peran kamu**: iseng/belajar, kontributor, atau kerja berbayar. Sebut di awal, bukan setelah commit pertama.

Draft pesan siap kirim: **`docs/pesan-caesar.md`** (skenario A: dia belum balas).

**Kalau ternyata sudah ada repo Java:** langkah 1–3 di bawah tetap berlaku, tapi prototipe ini turun
pangkat jadi *referensi aturan*, bukan kode yang dipakai. Baca kode dia dulu, tulis test untuk
perilaku yang sudah ada, baru usulkan perubahan.

## Langkah 1 — siapkan lingkungan (30–60 menit)

- [ ] JDK 21 (Temurin) — proyek ini target 17, jadi 17 atau 21 dua-duanya aman
- [ ] IDE: **IntelliJ IDEA Community** (paling enak untuk Java), **VS Code + Extension Pack for Java**
      (kalau mau satu editor dengan Laravel), atau **NetBeans** (kalau diwajibkan kampus)
- [ ] Maven — sudah bawaan NetBeans/IDEA; perlu install manual kalau pakai VS Code + terminal
- [ ] `git clone` repo caesar kalau ada; kalau belum, repo lokal sendiri

Langkah klik-per-klik NetBeans + perbandingan IDE: **`docs/ide-netbeans-dan-alternatif.md`**
Perintah install + tabel mapping `artisan` → `mvn`, Eloquent → JPA: **`docs/setup-java-untuk-dev-laravel.md`**

## Langkah 2 — tulis aturan main, bukan kode (1–2 jam)

Ini bagian yang paling menentukan dan paling sering dilewati. Antrean itu 90% kebijakan produk,
10% struktur data. Yang harus diputuskan (draft jawabannya sudah ada di `docs/konsep.md`):

- [ ] FIFO murni, atau berbobot subscriber/follower?
- [ ] Berapa lama peserta punya waktu merespons saat dipanggil?
- [ ] No-show: keluar, atau balik antrean dengan penalti? Berapa kali boleh?
- [ ] Berapa orang main sekaligus (1-on-1 vs party 5 orang)?
- [ ] Yang menunggu lama dijamin dapat giliran atau boleh kesalip terus?
- [ ] Peserta lihat posisinya di mana: overlay OBS, chat bot, atau web?

## Langkah 3 — prototipe inti (sudah dikerjakan di folder ini)

Sengaja **tanpa framework, tanpa DB, tanpa Spring**. Alasan: bisa direview caesar dalam 5 menit,
tidak mengunci stack dia, dan semua aturan bisa dites tanpa infra.

**Lewat IDE (NetBeans):** `File > Open Project` → folder `nxtup` → **F6** untuk simulasi,
**Alt+F6** untuk test.

**Lewat terminal:**

```bash
mvn test          # 16 test JUnit
mvn exec:java     # simulasi sesi mabar
bash run.sh       # jalur offline: 44 pemeriksaan + simulasi, tanpa Maven/JUnit
```

Hasil yang sudah diverifikasi: **44 pemeriksaan lulus / 16 test JUnit lulus, 0 gagal**, lalu simulasi:

```
20:00:20  4 penonton masuk. Subscriber BELUM otomatis nomor 1 - lama tunggu ikut dihitung
slot 0/1 [kosong] | antrean: 1.Rina[VIEWER skor=20], 2.Dika[FOLLOWER skor=19],
                            3.Bagas[VIEWER skor=10], 4.Sari[SUBSCRIBER skor=0]
>> DIPANGGIL: Rina  (paling lama menunggu)
...
20:06:00  timeout 1 orang -> Dika di-requeue dengan penalti, posisinya turun
slot 0/1 [kosong] | antrean: 1.Bagas[VIEWER skor=440], 2.Dika[FOLLOWER skor=-150]
```

Yang sudah diputuskan di prototipe (semuanya bisa diubah lewat `QueueConfig`):

| Aturan | Nilai sekarang | Kenapa |
|---|---|---|
| Skor pemilihan | `(lama_tunggu + skips × 30s) × bobot_tier` | subscriber dihargai, tapi tidak menindas penonton biasa |
| Bobot tier | viewer 1.0 / follower 1.25 / subscriber 1.6 | angka produk, **perlu dikonfirmasi** |
| Anti-starvation | dilewati 8× → wajib dipanggil berikutnya | jaminan keras, bukan cuma harapan |
| Timeout panggilan | 60 detik | |
| No-show | requeue + penalti 120s, maksimal 3 panggilan | tidak menghukum permanen, tapi tidak gratis |
| Slot | 1 (giliran 1-on-1) | gampang diubah ke party |

Yang **belum** ada dan memang sengaja ditunda: thread-safety, persistensi, auth, integrasi
Twitch/YouTube/Discord, UI overlay, bot chat.

## Langkah 4 — review bareng pemilik proyek (30 menit, menentukan)

- [ ] Kirim `docs/konsep.md` + output simulasi (bukan seluruh kode)
- [ ] Minta dia mengoreksi **aturan**, bukan mengomentari kode
- [ ] Sepakati batas kontribusi: modul mana yang kamu pegang, siapa yang merge
- [ ] Sepakati sifat kerja: belajar / open source / berbayar

Kalau dia tidak merespons aturan-aturan ini, itu sinyal proyek belum butuh tangan kedua — berhenti
di prototipe, jangan lanjut bangun aplikasi penuh.

## Langkah 5 — baru integrasi (setelah stack jelas)

| Kalau ternyata… | Langkah berikutnya |
|---|---|
| Spring Boot | tinggal tambah parent + starter di `pom.xml`, bungkus `QueueEngine` di `@Service`, REST + WebSocket/STOMP, JPA untuk persistensi |
| Sudah pakai Redis/broker | `QueueEngine` jadi *policy layer*, state pindah ke Redis, tick jadi scheduler |
| Android/desktop | `core` dipakai apa adanya (nol dependency), tinggal UI |
| Bot Discord saja | JDA + `QueueEngine`, perintah `!join`, `!next`, `!leave` |

Urutan yang aman: **aturan → test → API → penyimpanan → realtime → UI**. Jangan mulai dari UI.

---

## Catatan jujur

- `Tier`, bobot, dan package `id.nxtup.queue` adalah karanganku untuk keperluan prototipe — nama dan
  kepemilikan repo ikut keputusan caesar.
- Prototipe ini belum thread-safe. Untuk produksi: satu lock per antrean, atau satu antrean diproses
  single-threaded. Ini sudah ditandai di kode.
- Ada dua file test dengan **satu sumber assertion**: harness offline (`QueueEngineTest`) dan adapter
  JUnit (`QueueEngineJUnitTest`) yang memanggil grup pemeriksaan yang sama. Tidak ada duplikasi logika test.
