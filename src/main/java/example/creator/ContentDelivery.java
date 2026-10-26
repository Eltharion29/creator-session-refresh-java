package example.creator;

public final class ContentDelivery {
    public CreatorSessionService.Delivery prepare(String subscriberId, String assetId, String sessionId) {
        return new CreatorSessionService.Delivery(subscriberId, assetId, sessionId,
                "Lesson update queued for " + subscriberId);
    }
}
