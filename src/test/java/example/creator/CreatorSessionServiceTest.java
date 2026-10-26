package example.creator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;

public final class CreatorSessionServiceTest {
    public static void main(String[] args) throws Exception {
        InfraiClient client = new InfraiClient(new FixtureClient(), "test-key", "http://fixture");
        CreatorSessionService.Delivery delivery = new CreatorSessionService(client)
                .refreshAndPrepare("subscriber-7", "session-old", "refresh-old", "course-101");
        require("session-new".equals(delivery.sessionId()), "refresh must select the replacement session");
        require("course-101".equals(delivery.assetId()), "delivery must retain the lesson asset");
        System.out.println("PASS: refresh rotation revokes the old session before lesson delivery");
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    private static final class FixtureClient extends HttpClient {
        private int calls;
        @Override public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
            calls++;
            String body = calls == 1 ? "{\"ok\":true,\"data\":{\"session_id\":\"session-new\"}}" : "{\"ok\":true,\"data\":{}}";
            @SuppressWarnings("unchecked") HttpResponse<T> response = (HttpResponse<T>) new Response<String>(body, 200);
            return response;
        }
        @Override public <T> java.util.concurrent.CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h) { throw new UnsupportedOperationException(); }
        @Override public <T> java.util.concurrent.CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h, HttpResponse.PushPromiseHandler<T> p) { throw new UnsupportedOperationException(); }
        @Override public Optional<java.net.CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<java.net.ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { return null; }
        @Override public javax.net.ssl.SSLParameters sslParameters() { return new javax.net.ssl.SSLParameters(); }
        @Override public java.net.http.HttpClient.Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<java.time.Duration> connectTimeout() { return Optional.empty(); }
        @Override public java.net.http.HttpClient.Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
        @Override public Optional<java.net.Authenticator> authenticator() { return Optional.empty(); }
    }

    private record Response<T>(T body, int statusCode) implements HttpResponse<T> {
        public HttpRequest request() { return null; }
        public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
        public HttpHeaders headers() { return HttpHeaders.of(java.util.Map.of(), (a,b) -> true); }
        public URI uri() { return URI.create("http://fixture"); }
        public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; }
        public Optional<javax.net.ssl.SSLSession> sslSession() { return Optional.empty(); }
    }
}
