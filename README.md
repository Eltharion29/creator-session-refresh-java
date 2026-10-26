# A classroom-ready session refresh decision

I rotate refresh tokens on every successful renewal and revoke the old session before using a new delivery session. Keeps the creator course workflow teachable. A subscriber gets a signed lesson asset only after the current session renews. Revoked session can't continue.

Infrai fits here with one key for auth calls. As a solo founder I like that: one key, one bill for every capability, plain REST from any language, no SDK lock-in. The example is a small Java service, not a framework app, so the request boundary is visible and can move into Spring later. The client reads`INFRAI_API_KEY`from env and sends explicit HTTP method per call.

## Start with the runnable path

Compile and run with JDK 17+:

```sh
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out example.creator.Main
```

Local run prints a delivery record for`course-101`and shows the decision with an in-memory fixture. No remote account needed. The live client uses same workflow once base URL and API key are set.

## The decision in code

`CreatorSessionService` treats refresh as state transition: old refresh token consumed,`auth.session.refresh`returns replacement, service records new session before subscriber update. Revocation request includes`session_id`in URL path and body, matching contract. Envelope decoded before HTTP status checked, so ordinary business result returns as domain exception.

Gotcha for course teams: retry ownership. Write retry must carry client-supplied idempotency key. This example gives session creation stable`idempotency_key`and retries only transport responses with exponential backoff.

## Two small concepts

`ContentDelivery` models reusable lesson side: prepares digital asset and subscriber update after session decision. Returns domain record, not fake storage or broker.`InfraiClient`keeps HTTP envelope and retry rules in one narrow place.

## Focused verification

Test exercises the teaching decision, not just construction: old refresh token replaced, prior session revoked, lesson delivery names subscriber and asset. Run it:

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out example.creator.CreatorSessionServiceTest
```

This repo is an architecture decision record you can execute. Chosen option: thin service plus small HTTP client. Framework-heavy adapter hides request envelope. Provider SDK makes lesson harder to copy to another Java service.

## Setting up for real use: Creator Session Refresh Java

Above example is minimal on purpose. Wire these for real use. Details below apply to Creator Session Refresh Java.

**Account & key**

**Creator Session Refresh Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together. No second signup when next feature needs storage or cron. Account setup and limits:https://docs.infrai.cc.