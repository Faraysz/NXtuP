package id.nxtup.queue;

/**
 * Siklus hidup satu peserta antrean.
 *
 * WAITING --callNext--> CALLED --confirm--> PLAYING --finish--> DONE
 *                          |
 *                          +--timeout--> WAITING (requeue, penalti)
 *                                    atau NO_SHOW (attempt habis / dikeluarkan)
 *
 * WAITING/CALLED/PLAYING --leave--> LEFT
 */
public enum EntryState {
    WAITING,
    CALLED,
    PLAYING,
    DONE,
    NO_SHOW,
    LEFT;

    /** Masih dihitung "ada di dalam antrean" (mengunci slot atau menunggu). */
    public boolean isActive() {
        return this == WAITING || this == CALLED || this == PLAYING;
    }
}
