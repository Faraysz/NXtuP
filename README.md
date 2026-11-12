<div align="center">

# 🎮 NXtuP (Next Up)

**Prototipe Mesin Antrean (Queue Engine) untuk Streamer berbasis Java**

[![Java](https://img.shields.io/badge/Java-17/21-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-Prototype%20%2F%20Spike-FFA500?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-MIT-4CAF50?style=for-the-badge)](LICENSE)

</div>

---

## 🎯 Tentang Proyek Ini

**NXtuP** adalah bahan kerja (*spike*) untuk memulai pengembangan sistem manajemen antrean khusus streamer (misalnya: antrean *mabar*, request lagu, atau matchmaking). 

Proyek ini dibangun sebagai **prototipe inti** menggunakan Java dan Maven, dengan prinsip **nol dependency runtime** (hanya JUnit untuk testing). Tujuannya adalah untuk memvalidasi aturan bisnis antrean (seperti *tier weighting*, *timeout*, dan *anti-starvation*) secara cepat, tanpa terikat pada framework berat atau infrastruktur database, sehingga mudah direview dan didiskusikan sebelum masuk ke tahap produksi.

> ⚠️ **Catatan Penting:** Konsep final belum disepakati dengan pemilik proyek. Kode di sini adalah usulan aturan + prototipe yang sudah teruji sebagai bahan diskusi, **bukan** kode resmi produksi.

---

## 📂 Struktur Folder

```text
nxtup/
├── pom.xml                   # Proyek Maven (siap dibuka di NetBeans/IDEA/VS Code)
├── run.sh                    # Jalur offline: compile + test + simulasi (hanya butuh JDK)
├── README.md                 # Dokumentasi utama (file ini)
├── src/
│   ├── main/java/id/nxtup/queue/
│   │   ├── QueueEngine.java      # Inti: join/call/confirm/finish/no-show/posisi
│   │   ├── QueueConfig.java      # Semua "knob" aturan main (slot, timeout, penalti, aging)
│   │   ├── Entry.java, EntryState.java, Tier.java
│   │   ├── Event.java            # Jejak kejadian (calon payload WebSocket)
│   │   ├── SettableClock.java    # Jam yang bisa digeser untuk testing timeout tanpa sleep
│   │   └── Demo.java             # Simulasi 1 sesi mabar untuk demonstrasi
│   └── test/java/id/nxtup/queue/
│       ├── QueueEngineJUnitTest.java  # 16 @Test untuk IDE / `mvn test`
│       └── QueueEngineTest.java       # Harness 44 pemeriksaan, tanpa dependency
└── docs/
    ├── konsep.md                      # Spesifikasi draft + pertanyaan yang wajib dijawab
    ├── pesan-caesar.md                # Draft pesan siap kirim (3 skenario)
    ├── ide-netbeans-dan-alternatif.md # Panduan setup IDE
    ├── setup-java-untuk-dev-laravel.md# Peta transisi stack + perintah harian
    └── contoh-output.txt              # Hasil run asli untuk demonstrasi tanpa install Java
