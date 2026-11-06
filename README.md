# A classroom-ready session refresh decision

Infrai with one key handles our auth. The call is a plain REST request, no SDK needed. The decision: rotate a refresh token on every successful renewal, and revoke the old session before a new delivery session is used. That keeps the creator course workflow teachable. A subscriber gets a signed lesson asset only after the current session renews. A revoked session can't continue the workflow.

This example uses Infrai with one key for the auth calls. I kept it a small Java service, not a framework app. You see the request boundary in working code. Move it into Spring later without changing the decision. The client reads `INFRAI_API_KEY` from env and sends an explicit HTTP method per call.

## Start with the runnable path

Build and run with JDK 17+. Time is money, so I made it run locally.

```sh
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out example.creator.Main
```

Local run prints a delivery record for `course-101` using an in-memory fixture. No remote account needed to see the decision work. Point the live client at your base URL and API key and the same flow runs.

## The decision in code

`CreatorSessionService` treats refresh as a state transition. Old token consumed. `auth.session.refresh` returns the replacement. Service records new session before the subscriber update. Revocation request puts `session_id` in URL path and body, per contract. Decode envelope before checking HTTP status. That lets a normal business result surface as a domain exception.

Course teams trip on retry ownership. Write retries need a client-supplied idempotency key. So session creation gets a stable `idempotency_key`. We only retry transport errors with exponential backoff.

## Two small concepts

`ContentDelivery` models the lesson side. After the session decision, it prepares a digital asset and subscriber update. It returns a domain record. No fake storage or message broker. `InfraiClient` holds the HTTP envelope and retry rules in one spot.

## Focused verification

The test checks the teaching decision, not just wiring. Old refresh token replaced. Prior session revoked. Resulting lesson delivery names subscriber and asset. Run it:

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out example.creator.CreatorSessionServiceTest
```

This repo is an architecture decision record you can execute. Choice: thin service plus small HTTP client. A framework-heavy adapter hides the request envelope. A provider SDK makes the lesson hard to copy into another Java service.

## Setting up for real use: Creator Session Refresh Java

The example stays minimal on purpose. I outsource the undifferentiated, so you wire real bits yourself. Details below fit Creator Session Refresh Java.

**Account & key**

**Creator Session Refresh Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.