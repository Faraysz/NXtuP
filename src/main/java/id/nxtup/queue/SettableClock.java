package id.nxtup.queue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Jam yang bisa digeser manual. Ini kunci supaya aturan timeout/no-show bisa dites
 * tanpa Thread.sleep() — test tetap deterministik dan cepat.
 */
public final class SettableClock extends Clock {

    private Instant now;

    public SettableClock(Instant start) {
        this.now = start;
    }

    public static SettableClock at(String isoInstant) {
        return new SettableClock(Instant.parse(isoInstant));
    }

    public void advance(Duration d) {
        this.now = this.now.plus(d);
    }

    public void advanceSeconds(long s) {
        advance(Duration.ofSeconds(s));
    }

    @Override public ZoneId getZone() { return ZoneOffset.UTC; }

    @Override public Clock withZone(ZoneId zone) { return this; }

    @Override public Instant instant() { return now; }
}
