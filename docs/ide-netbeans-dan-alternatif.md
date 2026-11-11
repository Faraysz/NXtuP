# Buka nxtup di NetBeans (dan pilihan IDE yang lebih enak)

Proyek ini sekarang proyek **Maven** (`pom.xml` ada di root). Artinya NetBeans, IntelliJ IDEA,
VS Code, dan Eclipse bisa membukanya **tanpa konfigurasi manual** — tidak perlu
"New Project from Existing Sources", tidak perlu setting classpath sendiri.

---

## Bagian 1 — NetBeans, langkah awal

### 0. Prasyarat

1. **JDK 17 atau 21** terpasang lebih dulu (Temurin direkomendasikan). Cek: `java -version`.
2. **Apache NetBeans** versi terbaru. Installer-nya butuh JDK 17+ untuk berjalan.
3. Internet untuk pembukaan pertama — NetBeans akan mengunduh plugin Maven + JUnit sekali saja.

Kalau NetBeans tidak mau start dengan pesan seperti *"Cannot find java 1.8 or higher"*:
buka file `<folder-netbeans>/etc/netbeans.conf`, isi baris ini dengan path JDK-mu:

```
netbeans_jdkhome="C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
```

### 1. Ekstrak zip

Hasil ekstrak harus berupa folder `nxtup` yang **di dalamnya langsung ada `pom.xml`**:

```
nxtup/
├── pom.xml     <- ini yang dikenali NetBeans
├── src/main/java/id/nxtup/queue/...
└── src/test/java/id/nxtup/queue/...
```

### 2. Open Project (bukan New Project)

`File > Open Project…` (**Ctrl+Shift+O**) → arahkan ke folder `nxtup` → folder itu akan
berikon Maven → klik **Open Project**.

Tunggu sampai tulisan *priming build* / progress bar di kanan bawah hilang. Sekali ini saja lama.

Kalau muncul badge merah/tanda seru di nama proyek: klik kanan proyek → **Resolve Project Problems…**
(atau **Reload POM**). Biasanya cuma dependency yang belum terunduh.

### 3. Jalankan simulasinya

`Run > Run Project` (**F6**) → langsung jalan, karena `exec.mainClass` sudah diset di `pom.xml`.

Output muncul di jendela **Output**:

```
=== nxtup - simulasi antrean giliran main (1 slot) ===
   20:00:20  4 penonton masuk. Subscriber BELUM otomatis nomor 1 ...
>> 20:00:20  DIPANGGIL: Rina  (Rina yang paling lama menunggu)
```

Alternatif yang lebih sering dipakai sehari-hari: buka `Demo.java`, klik kanan di editor →
**Run File** (**Shift+F6**).

### 4. Jalankan test-nya

Klik kanan proyek → **Test** (**Alt+F6**) → jendela **Test Results** menampilkan 16 test hijau.

Atau buka `QueueEngineJUnitTest.java` → **Ctrl+F6** (Test File).

> Ada dua file test, sengaja:
> - `QueueEngineJUnitTest` → untuk IDE/Maven (16 @Test, hijau-merah visual)
> - `QueueEngineTest` → harness tanpa dependency, punya `main()`, bisa di-**Run File** juga
>   dan mencetak 44 baris `OK`. Assertion-nya sama, tidak diduplikasi.

### 5. Debug — ini yang bikin pindah dari PHP terasa enak

1. Buka `QueueEngine.java`, cari method `selectNext()`.
2. Klik nomor barisnya (**Ctrl+F8**) → muncul breakpoint merah.
3. `Debug > Debug Project` (**Ctrl+F5**).
4. Saat berhenti: jendela **Variables** menampilkan isi `waiting`, skor tiap peserta, `config`.
   **F8** = step over, **F7** = step into, **Ctrl+F2** = evaluasi ekspresi.

Ini pengganti `dd()`/`dump()`: kamu bisa lihat seluruh state antrean tanpa menambah satu baris log.

### 6. Tombol harian NetBeans

| Aksi | Shortcut |
|---|---|
| Run File | Shift+F6 |
| Test File | Ctrl+F6 |
| Run Project | F6 |
| Debug Project | Ctrl+F5 |
| Autocomplete | Ctrl+Space |
| Quick fix / import | Alt+Enter |
| Fix imports | Ctrl+Shift+I |
| Cari class | Ctrl+O |
| Format kode | Alt+Shift+F |
| Rename (refactor) | Ctrl+R |

### 7. Kalau mentok

| Gejala | Penyebab & obat |
|---|---|
| Folder tidak berikon Maven | Yang dibuka folder induk. Buka folder yang berisi `pom.xml`. |
| Badge merah / priming gagal | Offline/proxy. Jalankan `mvn -U test` di terminal untuk lihat error aslinya. |
| F6 tidak melakukan apa-apa | Properties proyek → **Run** → Main Class = `id.nxtup.queue.Demo` |
| Test Results kosong | Yang dijalankan `QueueEngineTest` (harness biasa). Pakai `QueueEngineJUnitTest` / Alt+F6. |
| Output berisi `?` aneh | Properties → Run → VM Options: `-Dstdout.encoding=UTF-8` |
| Error "release version 17 not supported" | JDK yang dipakai NetBeans lebih tua. Properties → Build → Compile → Java Platform = JDK 17/21. |
| Lambat sekali saat buka pertama | Normal: indexing + unduh dependency. Sekali saja. |

---

## Bagian 2 — apakah ada yang lebih oke dari NetBeans?

Ada. NetBeans tidak salah, tapi bukan pilihan tercepat untuk kerja harian.

### IntelliJ IDEA Community — pilihanku kalau kamu bebas memilih

- **Kenapa**: refactoring dan debugger Java terbaik di kelasnya, autocomplete paling paham konteks,
  Maven/Gradle native, dan 90% proyek Java di industri ditulis di sini. Gratis.
- **Harga yang dibayar**: berat (RAM 2 GB+), indexing awal lama, tampilan ramai di minggu pertama.
- **Langkah**: `Open` → pilih folder `nxtup` (atau `pom.xml`) → **Trust Project** → tunggu indexing →
  klik ikon ▶ di samping `main()` pada `Demo.java`. Test: klik ikon ▶▶ di nama class test.
  Debug: ikon 🐞. Shortcut penting: **Shift+F10** run, **Shift+F9** debug, **Alt+Enter** quick fix,
  **Shift+F6** rename, **Ctrl+Alt+L** format.

### VS Code + Extension Pack for Java — paling cocok kalau kamu sudah pakai VS Code untuk Laravel

- **Kenapa**: satu editor untuk PHP dan Java, ringan, kamu tidak perlu pindah kebiasaan.
  Extension-nya (Microsoft) otomatis mendeteksi `pom.xml`.
- **Harga yang dibayar**: refactoring tidak sedalam IDEA; kadang perlu
  `Java: Clean Java Language Server Workspace` kalau cache-nya ngaco.
- **Langkah**: install **Extension Pack for Java** (berisi Language Support, Debugger, Maven,
  Test Runner, Project Manager) → `File > Open Folder` → folder `nxtup` → tunggu import Maven →
  muncul tombol **Run | Debug** di atas setiap `main()` → panel **Testing** (ikon labu) untuk test.

### NetBeans — tetap masuk akal kalau

kampus/kantor mewajibkan, atau kamu butuh GUI builder Swing (Matisse). Untuk proyek antrean ini
Matisse tidak relevan. Maven sudah bawaan tanpa plugin, jadi tidak ada alasan teknis untuk menolak
NetBeans — hanya kalah nyaman.

### Eclipse

Skip, kecuali dipaksa lingkungan kerja.

**Ringkasnya:** bebas pilih → **IntelliJ IDEA Community**. Mau satu editor dengan kerjaan Laravel →
**VS Code + Extension Pack for Java**. Sudah terlanjur NetBeans / diwajibkan → tetap aman,
proyek ini sudah disiapkan supaya ketiganya langsung jalan.

---

## Bagian 3 — perintah terminal (berlaku di IDE mana pun)

```bash
mvn test          # 16 test JUnit
mvn exec:java     # jalankan simulasi
mvn package       # bikin jar
java -jar target/nxtup-queue-0.1.0-SNAPSHOT.jar

bash run.sh       # jalur offline: compile + 44 pemeriksaan + simulasi, tanpa Maven/JUnit
```

`bash run.sh` sengaja tidak menjalankan `QueueEngineJUnitTest` (file itu butuh JUnit);
sisanya tetap bisa jalan hanya dengan JDK.
