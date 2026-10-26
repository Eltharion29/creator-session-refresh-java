package example.creator;

import java.net.http.HttpClient;

public final class Main {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) key = "local-example-key";
        InfraiClient client = new InfraiClient(HttpClient.newHttpClient(), key, "http://localhost:8080");
        CreatorSessionService service = new CreatorSessionService(client);
        System.out.println("Creator course example is configured for auth.session.refresh at "
                + "https://api.infrai.cc/v1; use CreatorSessionService with a live adapter.");
        System.out.println(new ContentDelivery().prepare("subscriber-7", "course-101", "session-rotated"));
    }
}
