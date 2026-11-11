package id.nxtup.queue;

import java.time.Instant;

/** Satu peserta antrean. Mutable, tapi hanya boleh diubah oleh QueueEngine (setter package-private). */
public final class Entry {

    private final long seq;
    private final String userId;
    private final String displayName;
    private final Tier tier;
    private final Instant joinedAt;

    /** Titik acuan perhitungan lama menunggu. Berubah kalau kena penalti no-show. */
    private Instant effectiveSince;
    private EntryState state = EntryState.WAITING;
    private Instant calledAt;
    private int callAttempts;
    private int skips;
    private int noShows;

    Entry(long seq, String userId, String displayName, Tier tier, Instant joinedAt) {
        this.seq = seq;
        this.userId = userId;
        this.displayName = displayName;
        this.tier = tier;
        this.joinedAt = joinedAt;
        this.effectiveSince = joinedAt;
    }

    public long seq() { return seq; }
    public String userId() { return userId; }
    public String displayName() { return displayName; }
    public Tier tier() { return tier; }
    public Instant joinedAt() { return joinedAt; }
    public Instant effectiveSince() { return effectiveSince; }
    public EntryState state() { return state; }
    public Instant calledAt() { return calledAt; }
    public int callAttempts() { return callAttempts; }
    public int skips() { return skips; }
    public int noShows() { return noShows; }

    void state(EntryState s) { this.state = s; }
    void calledAt(Instant t) { this.calledAt = t; }
    void effectiveSince(Instant t) { this.effectiveSince = t; }
    void incCallAttempts() { this.callAttempts++; }
    void incSkips() { this.skips++; }
    void resetSkips() { this.skips = 0; }
    void incNoShows() { this.noShows++; }

    @Override
    public String toString() {
        return displayName + "(" + tier + "," + state + ")";
    }
}
