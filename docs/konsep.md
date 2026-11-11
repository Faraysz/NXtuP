# nxtup — konsep draft & pertanyaan terbuka

Status: **draft usulan**, belum dikonfirmasi pemilik proyek. Dipakai sebagai bahan diskusi,
bukan spesifikasi final.

## 1. Yang sudah dikonfirmasi vs yang masih asumsi

| Info | Sumber | Status |
|---|---|---|
| Nama proyek `nxtup` | chat 09/11 23:58 | dikonfirmasi |
| Tentang queue / manajemen antrean | "iya betul" 11/11 00:49 | dikonfirmasi |
| Untuk streamer | "tp buat streamer" 11/11 00:49 | dikonfirmasi |
| Bahasa Java | "java" 11/11 00:48 | dikonfirmasi |
| Sudah berjalan ("on progress") | chat 09/11 23:58 | dikonfirmasi, tapi sejauh mana belum jelas |
| Antrean **giliran main** penonton bareng streamer | tebakan Faraysz 03:55 | **asumsi, belum dijawab** |
| FIFO berbobot + penalti no-show | usulan dokumen ini | **usulan** |

Kalau asumsi baris ke-6 salah, seluruh aturan di bawah harus direvisi. Itu sebabnya pertanyaan
nomor 1 di bagian 5 harus dijawab sebelum kerja lanjut.

## 2. Model domain

```
Peserta (Entry)
  userId, displayName, tier, joinedAt
  state, callAttempts, skips, noShows

Antrean (Queue) — milik satu streamer / satu sesi
  config: slot, timeout, penalti, bobot tier, aging
  operasi: join, leave, callNext, confirm, finish, skip, tick
  bacaan: waitingOrder, positionOf, snapshot, events
```

Siklus hidup satu peserta:

```
        join
          │
          ▼
      WAITING ──callNext──► CALLED ──confirm──► PLAYING ──finish──► DONE
          ▲                    │
          │                    ├─ timeout / di-skip ─► NO_SHOW ─► keluar (attempt habis)
          └──── requeue + penalti ┘
          
  leave dari WAITING/CALLED/PLAYING ─► LEFT
```

Aturan kunci: peserta `CALLED` **mengunci slot** seperti sedang main. Kalau tidak, streamer bisa
memanggil dua orang untuk satu kursi.

## 3. Aturan pemilihan (usulan)

```
skor = (lama_tunggu_detik + jumlah_dilewati × aging) × bobot_tier
```

- Pemenang: skor tertinggi. Seri → yang join lebih dulu (deterministik, bisa diaudit).
- `aging` membuat penonton biasa yang terus dilewati akhirnya naik — tidak ada yang menunggu selamanya.
- Batas keras: dilewati ≥ `starvationCapSkips` → wajib dipanggil berikutnya, apa pun bobotnya.
- No-show: wait dihitung ulang dari sekarang + penalti, jadi dia turun di bawah pendatang baru,
  tapi tetap kompetitif beberapa menit kemudian.
- Semua angka ada di `QueueConfig`, bukan tersebar di logika.

Kenapa bukan FIFO murni: streamer biasanya ingin menghargai subscriber. Kenapa bukan prioritas
subscriber mutlak: penonton biasa yang tidak pernah dapat giliran akan berhenti ikut antrean.
Skor berbobot + aging adalah jalan tengah yang bisa dijelaskan ke penonton lewat angka di overlay.

## 4. Definisi selesai untuk MVP (usulan)

MVP dianggap selesai kalau:

1. Penonton bisa masuk antrean lewat satu kanal (chat bot **atau** web, pilih satu).
2. Streamer bisa panggil → peserta konfirmasi → giliran selesai, lewat satu tombol.
3. Peserta yang tidak merespons otomatis dilewati tanpa streamer turun tangan.
4. Posisi antrean terlihat realtime dan urutannya bisa dijelaskan ("kenapa dia duluan?").
5. Restart aplikasi tidak menghapus antrean yang sedang berjalan.
6. Satu orang tidak bisa menempati dua slot dengan akun ganda.

Butir 5 dan 6 sengaja masuk MVP: dua-duanya sumber komplain paling cepat di produk antrean.

## 5. Delapan pertanyaan yang harus dijawab caesar

Dibuat tertutup supaya bisa dijawab singkat:

1. Antrean **giliran main bareng streamer**, atau antrean **request** (lagu/konten), atau **matchmaking**?
2. Sudah ada repo/kode? Kalau ada, Java-nya apa: Spring Boot, Android, desktop, atau bot (JDA)?
3. Satu giliran = 1 orang (1-on-1) atau satu party (mis. 5 orang)?
4. Peserta masuk lewat mana: chat Twitch/YouTube, bot Discord, atau halaman web?
5. Subscriber/pemberi donasi dapat prioritas, atau semua setara FIFO?
6. Kalau dipanggil tidak respons: keluar, atau balik antrean? Berapa kali boleh?
7. Antrean perlu bertahan kalau aplikasi restart, atau cukup in-memory per sesi?
8. Peranku di sini apa: coba-coba/belajar, kontributor repo, atau kerja berbayar?

## 6. Risiko yang perlu disebut di awal

- **Salah use case** → prototipe tidak terpakai. Mitigasi: pertanyaan 1 dijawab dulu.
- **Beda stack** (harian Laravel, proyek Java) → kontribusi kode butuh waktu adaptasi. Mitigasi:
  ambil bagian yang language-agnostic dulu (aturan antrean, test, dokumentasi).
- **Tanpa batas kontribusi** → berujung kerja gratis tanpa kesepakatan. Mitigasi: pertanyaan 8.
- **Menimpa kerjaan dia** → bikin dua versi antrean. Mitigasi: baca repo dulu kalau sudah ada.
