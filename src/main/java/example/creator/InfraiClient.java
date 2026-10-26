package example.creator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiClient {
    private final HttpClient http;
    private final String apiKey;
    private final String baseUrl;

    public InfraiClient(HttpClient http, String apiKey, String baseUrl) {
        this.http = http;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String refreshSession(String refreshToken) throws IOException, InterruptedException {
        String body = "{\"refresh_token\":\"" + escape(refreshToken) + "\"}";
        return post("/v1/auth/session/refresh", body, false);
    }

    public String revokeSession(String sessionId) throws IOException, InterruptedException {
        String body = "{\"session_id\":\"" + escape(sessionId) + "\"}";
        return post("/v1/auth/session/revoke/" + escape(sessionId), body, true);
    }

    private String post(String path, String body, boolean write) throws IOException, InterruptedException {
        int attempt = 0;
        while (true) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            Envelope envelope = Envelope.read(response.body());
            if (!envelope.ok) {
                throw new InfraiException(envelope.error);
            }
            if (response.statusCode() == 429 && attempt < 3) {
                long delay = retryAfter(response, attempt);
                Thread.sleep(delay);
                attempt++;
                continue;
            }
            if (response.statusCode() >= 500) {
                throw new IOException("remote service returned " + response.statusCode());
            }
            return response.body();
        }
    }

    private long retryAfter(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(value -> parseSeconds(value, attempt)).orElse(100L * (1L << attempt));
    }

    private long parseSeconds(String value, int attempt) {
        try { return Math.max(100L, Long.parseLong(value) * 1000L); }
        catch (NumberFormatException ignored) { return 100L * (1L << attempt); }
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }

    private record Envelope(boolean ok, String error) {
        private static Envelope read(String json) {
            boolean ok = json.contains("\"ok\":true") || json.contains("\"ok\": true");
            Matcher matcher = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
            return new Envelope(ok, matcher.find() ? matcher.group(1) : "Infrai request rejected");
        }
    }

    public static final class InfraiException extends IOException {
        public InfraiException(String message) { super(message); }
    }
}
