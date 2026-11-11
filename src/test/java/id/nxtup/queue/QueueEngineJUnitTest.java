package id.nxtup.queue;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Adapter JUnit 5 — supaya tombol "Test Project" di NetBeans / IntelliJ / `mvn test`
 * menampilkan hasil hijau-merah seperti proyek Java normal.
 *
 * Assertion-nya TIDAK diduplikasi: setiap @Test memanggil grup pemeriksaan yang sama
 * dengan yang dipakai `bash run.sh` (jalur offline tanpa dependency).
 * Satu sumber kebenaran, dua cara menjalankan.
 */
class QueueEngineJUnitTest {

    private static void grup(Runnable g) {
        List<String> gagal = QueueEngineTest.runGroup(g);
        assertTrue(gagal.isEmpty(), () -> "pemeriksaan gagal: " + String.join(", ", gagal));
    }

    @Test @DisplayName("tier sama: yang join dulu dipanggil dulu")
    void fifo() { grup(QueueEngineTest::fifoUntukTierSama); }

    @Test @DisplayName("wait sama: bobot subscriber menang")
    void bobotTier() { grup(QueueEngineTest::subscriberMenangSaatWaitSama); }

    @Test @DisplayName("aging: yang dilewati naik mengalahkan subscriber baru")
    void aging() { grup(QueueEngineTest::agingMengalahkanSubscriberBaru); }

    @Test @DisplayName("batas keras anti-starvation")
    void antiStarvation() { grup(QueueEngineTest::batasKerasAntiStarvation); }

    @Test @DisplayName("join ganda tidak membuat entri baru")
    void joinIdempoten() { grup(QueueEngineTest::joinGandaTidakBikinEntriBaru); }

    @Test @DisplayName("slot penuh menolak panggilan")
    void slotPenuh() { grup(QueueEngineTest::slotPenuhMenolakPanggilan); }

    @Test @DisplayName("timeout: requeue dengan penalti")
    void timeout() { grup(QueueEngineTest::timeoutRequeueDenganPenalti); }

    @Test @DisplayName("attempt habis: peserta dikeluarkan")
    void attemptHabis() { grup(QueueEngineTest::attemptHabisDikeluarkan); }

    @Test @DisplayName("finish membebaskan slot")
    void finish() { grup(QueueEngineTest::finishMembebaskanSlot); }

    @Test @DisplayName("leave saat main membebaskan slot")
    void leave() { grup(QueueEngineTest::leaveSaatMainMembebaskanSlot); }

    @Test @DisplayName("skip oleh streamer")
    void skip() { grup(QueueEngineTest::skipOlehStreamer); }

    @Test @DisplayName("posisi konsisten dengan urutan antrean")
    void posisi() { grup(QueueEngineTest::posisiKonsistenDenganUrutan); }

    @Test @DisplayName("skor seri dipecahkan urutan join")
    void tieBreak() { grup(QueueEngineTest::seriDipecahkanUrutanJoin); }

    @Test @DisplayName("penalti no-show tidak menghukum selamanya")
    void penaltiSementara() { grup(QueueEngineTest::requeueTidakMenghukumSelamanya); }

    @Test @DisplayName("jejak event tercatat dan immutable")
    void event() { grup(QueueEngineTest::eventTercatat); }

    @Test @DisplayName("config tidak valid ditolak")
    void validasiConfig() { grup(QueueEngineTest::configDivalidasi); }
}
