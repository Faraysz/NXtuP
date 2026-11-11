package id.nxtup.queue;

import java.time.Instant;

/**
 * Jejak kejadian. Sengaja dibuat terpisah dari logika: nanti tinggal dilempar
 * ke WebSocket/SSE untuk update overlay streamer secara realtime.
 */
public record Event(Instant at, String type, String userId, String detail) {
    @Override
    public String toString() {
        return type + " " + userId + (detail == null || detail.isBlank() ? "" : " - " + detail);
    }
}
