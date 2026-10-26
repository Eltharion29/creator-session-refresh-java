package example.creator;

public final class CreatorSessionService {
    private final InfraiClient client;

    public CreatorSessionService(InfraiClient client) { this.client = client; }

    public Delivery refreshAndPrepare(String subscriberId, String sessionId, String refreshToken, String assetId)
            throws Exception {
        String refreshed = client.refreshSession(refreshToken);
        client.revokeSession(sessionId);
        String newSession = field(refreshed, "session_id", sessionId + "-rotated");
        return new ContentDelivery().prepare(subscriberId, assetId, newSession);
    }

    private static String field(String json, String name, String fallback) {
        String marker = "\"" + name + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return fallback;
        int valueStart = start + marker.length();
        int end = json.indexOf('"', valueStart);
        return end < 0 ? fallback : json.substring(valueStart, end);
    }

    public record Delivery(String subscriberId, String assetId, String sessionId, String update) { }
}
