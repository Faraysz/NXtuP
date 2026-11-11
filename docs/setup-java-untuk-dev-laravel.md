# Setup Java untuk dev Laravel

Target: dari nol Java sampai bisa menjalankan prototipe `nxtup` dan siap masuk Spring Boot.

## 1. Install

**Windows / macOS / Linux (paling rapi: SDKMAN)**

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-tem      # JDK 21 Temurin
sdk install maven            # build tool (pengganti composer)
java -version && mvn -v
```

**Windows tanpa SDKMAN:** download installer Temurin 21 (adoptium.net), lalu IntelliJ IDEA
Community — IDEA bisa mengunduh JDK sendiri lewat *Project Structure → SDK → Download JDK*.

**Debian/Ubuntu cepat (yang dipakai di sandbox ini):**

```bash
apt-get update && apt-get install -y openjdk-17-jdk-headless
```

IDE: **IntelliJ IDEA Community**. Untuk dev PHP ini bukan opsional — autocomplete, refactor, dan
debugger Java di IDEA jauh di atas editor biasa, dan 90% proyek Java ditulis di situ.

## 2. Peta konsep Laravel → Java

| Laravel / PHP | Java | Catatan |
|---|---|---|
| `composer.json` | `pom.xml` (Maven) / `build.gradle` | dependency + build script jadi satu |
| `composer require` | tambah `<dependency>` di `pom.xml` | versi ditulis eksplisit |
| `php artisan serve` | `mvn spring-boot:run` | |
| `php artisan tinker` | `jshell` | REPL bawaan JDK |
| `artisan make:model` | tidak ada; IDEA generate / Spring Initializr | |
| Eloquent ORM | JPA + Hibernate (`@Entity`, `Repository`) | tidak ada active record; entity ≠ query builder |
| migration | Flyway / Liquibase | versi SQL, bukan PHP closure |
| `.env` | `application.yml` + `@ConfigurationProperties` | |
| service container / DI | Spring `@Service` + constructor injection | konsepnya sama, tapi compile-time |
| `Queue::push()` / `queue:work` | `@Async`, `ThreadPoolTaskExecutor`, atau RabbitMQ/Kafka | |
| `Task::schedule()` | `@Scheduled` | |
| PHPUnit / Pest | JUnit 5 + AssertJ | |
| `dd()` / `dump()` | debugger + SLF4J `log.info()` | budaya Java: breakpoint, bukan dump |
| namespace `App\Models` | package `id.nxtup.queue` | folder wajib cocok dengan package |
| array asosiatif | `Map<String,Object>` atau **record** | pilih record, jangan map |
| `?->` nullsafe | `Optional<T>` | |
| Blade | Thymeleaf, atau frontend terpisah | |

## 3. Enam hal yang bikin kaget dev PHP

1. **Ada langkah compile.** Simpan file ≠ perubahan langsung berlaku. `javac` / `mvn compile` dulu. IDE melakukan
   ini otomatis, tapi error muncul sebelum jalan, bukan saat request masuk.
2. **Tipe statis ketat.** `String`, `int`, `long`, `Instant` tidak saling tukar. Ini justru
   mempercepat kerja: salah tipe ketahuan sebelum jalan.
3. **`record` = value object immutable.** `record QueueConfig(int slots, Duration callTimeout)`
   sudah punya constructor, getter, `equals`, `toString`. Pakai ini, bukan getter/setter manual.
4. **Waktu itu objek, bukan angka.** `Instant`, `Duration`, `Clock`. Jangan `System.currentTimeMillis()`
   langsung di logika — inject `Clock` supaya bisa dites (lihat `SettableClock` di prototipe).
5. **Concurrency nyata.** PHP mulai bersih tiap request; Java prosesnya long-lived. State bersama
   bisa diakses banyak thread. Antrean streamer wajib punya strategi: lock per antrean atau
   single-threaded per antrean.
6. **Package = folder.** Salah letak folder = compile error, bukan sekadar autoload warning.

## 4. Perintah harian

```bash
# prototipe ini (nol dependency)
bash run.sh

# manual, kalau mau paham apa yang terjadi
mkdir -p build/classes
find core/src -name '*.java' > build/sources.txt
javac -encoding UTF-8 -d build/classes @build/sources.txt
java -cp build/classes id.nxtup.queue.QueueEngineTest
java -cp build/classes id.nxtup.queue.Demo

# nanti kalau sudah Maven / Spring Boot
mvn test
mvn spring-boot:run
mvn -q dependency:tree     # mirip composer show
```

## 5. Urutan belajar 3 hari (cukup untuk kontribusi nyata)

- **Hari 1** — Java dasar untuk orang yang sudah bisa ngoding: class/record/enum, `List`/`Map`,
  `Optional`, generics, exception. Lewati tutorial "apa itu variabel".
- **Hari 2** — baca `QueueEngine.java` sampai bisa jelaskan tiap method, lalu ubah satu aturan
  (mis. bobot subscriber jadi 2.0) dan lihat test mana yang merah. Ini latihan paling cepat paham.
- **Hari 3** — Spring Initializr (`start.spring.io`): pilih Web + Validation, bikin satu endpoint
  `POST /queue/join` yang memanggil `QueueEngine`. Belum perlu database.

Setelah itu baru: JPA + Flyway, WebSocket/STOMP untuk overlay, lalu integrasi platform streaming.
