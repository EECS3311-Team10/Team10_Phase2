# Domain and application contract

The machine-readable endpoint and DTO definitions are in `openapi/consultlink-v1.json`. This document defines behavior that field schemas alone cannot express.

## Identity and access

Use a same-origin Spring Security session with an HttpOnly `JSESSIONID` cookie, `SameSite=Lax`, and Secure in HTTPS environments. Obtain a CSRF token from `GET /api/v1/auth/csrf` and send it in `X-CSRF-TOKEN` on all writes, including login/register/logout. Re-fetch the token after login/logout. JSON login must explicitly save the authenticated SecurityContext in the configured SecurityContextRepository. Passwords use BCrypt. Rate-limit login and chatbot requests. Do not trust browser role, price, or user-ID fields.

| Resource/action | Public | Client | Consultant | Admin |
|---|---|---|---|---|
| Active services, approved consultants, open slots, current public policy | Read | Read | Read | Read |
| Register | CLIENT or CONSULTANT only | — | — | — |
| Own profile / session | — | Own | Own | Own |
| Consultant application review / account suspension | — | — | — | Manage with reason and audit |
| Service catalog / policy versions | — | — | Read | Manage |
| Consultant offerings and availability | — | — | Own, only while approved and active | Read |
| Quote / create booking | — | Own | — | — |
| Booking list/detail/events | — | Own client bookings | Own assigned bookings | Read all |
| Accept / reject | — | — | Own REQUESTED bookings | — |
| Cancellation preview / cancel | — | Own | Own | No override in v1 |
| Pay / payment list | — | Own | — | Read all |
| Refund list / retry failed sandbox refund | — | Own read | — | Read / retry |
| Complete session | — | — | Own PAID session after end | — |
| Notifications / mark read | — | Own | Own | Own |
| Audit history | — | — | — | Read |
| Policy chatbot | — | Read public context | Read public context | Read public context |

An authenticated request for somebody else's private object returns 404 to avoid disclosing its existence. Role-disallowed operations return 403. Consultant approval is checked in the service layer for each operation, not just at login. Suspending an account invalidates its sessions and blocks new actions; an administrator must resolve its future bookings operationally. Existing bookings remain intact for review/refund. Never physically delete referenced accounts/catalog entries; deactivate them.

## Booking state and commands

| Current state | Command | Actor / precondition | Next state |
|---|---|---|---|
| — | Create from unexpired quote | Client; approved consultant; offering active; slot OPEN and future | REQUESTED |
| REQUESTED | Accept | Assigned consultant; hold not expired | CONFIRMED |
| REQUESTED | Reject | Assigned consultant, reason required | REJECTED |
| REQUESTED | Expire | Worker, request deadline reached | EXPIRED |
| CONFIRMED | Start sandbox payment | Client owner, payment deadline not reached | PENDING_PAYMENT |
| PENDING_PAYMENT | Successful payment | Transactional payment service | PAID |
| PENDING_PAYMENT | Failed payment | Transactional payment service | CONFIRMED |
| CONFIRMED | Expire | Worker, payment deadline reached | EXPIRED |
| REQUESTED / CONFIRMED / PENDING_PAYMENT / PAID | Cancel | Owner client/consultant; before session start; serialize against payment | CANCELLED |
| PAID | Complete | Assigned consultant, now >= session end | COMPLETED |

All other transitions return `409 INVALID_BOOKING_STATE`. The State factory reconstructs the appropriate State from the persisted enum. No transition defaults to a silent no-op. `PENDING_PAYMENT` is an internal step within the synchronous simulation transaction; failure returns to CONFIRMED. A future asynchronous provider would need a recovery/reconciliation worker before that implementation can ship.

There is at most one reserving booking per slot. REQUESTED, CONFIRMED, PENDING_PAYMENT, PAID, and COMPLETED occupy a slot. REJECTED, CANCELLED, and EXPIRED release it. Completed history continues to prevent reuse of a past slot. A slot is exactly one service session: its duration must match the selected service at quote creation. Different service durations require appropriate slots; arbitrary interval fitting is outside v1.

## Pricing and cancellation

Use `BigDecimal` and `NUMERIC(12,2)`. Monetary response fields are decimal strings, never binary floating-point numbers. Use HALF_UP once per adjustment, then `total = base + urgency - discount`. Urgency applies when `0 < startsAt - quoteCreatedAt < urgencyWindow`; the exact six-hour boundary has no surcharge. Discount is a configurable percentage of base price (zero by default). Quote creation records service title, time interval, currency, all price components, and the immutable policy version. Booking creation consumes that quote once. A quote guarantees its price until expiry but does not reserve capacity.

Cancellation preview returns the server-calculated refundable amount, fee, actor rule, policy version, and currency. The mutation recalculates under lock; preview is not a promise that time will stop. Unpaid bookings have refund and fee zero. Paid client cancellations at or outside the full-refund boundary refund 100%; later cancellations before the start refund `paid * lateRefundPercent / 100`. Paid consultant cancellations refund 100%. Round HALF_UP to currency precision. No self-service cancellation after the start; disputes are handled by admin contact in this version. No arbitrary “refund amount” is accepted from the browser.

## Transactions and concurrency

1. **Quote:** read approved/active consultant, service, offering, slot and current policy consistently; verify duration/future time; store immutable quote. Does not reserve.
2. **Create booking:** claim the idempotency key; lock slot; load quote and validate owner/expiry/current slot eligibility; insert REQUESTED booking with deadline `min(now + requestHold, startsAt)`; append event and both notifications; store response under key; commit. Unique slot index arbitrates competing clients even if the application lock is omitted accidentally.
3. **Accept/reject/expire:** lock booking, check actor/state/deadline; acceptance sets `min(now + paymentWindow, startsAt)` as payment deadline; append event, notifications and audit; commit. Service commands also reject overdue holds if the worker is behind.
4. **Pay:** claim idempotency key; lock booking; verify client/state/deadline; set PENDING_PAYMENT; execute the deterministic sandbox simulator without external network I/O; insert SUCCEEDED or FAILED payment; set PAID or CONFIRMED; append events/notifications; save idempotent response; commit. A decline returns a recorded FAILED payment with a business decline code. Transport/validation errors do not masquerade as a payment result.
5. **Cancel:** claim key; lock booking then successful payment; check actor/state/start; compute refund using the booking's policy; create cancellation event and refund record before setting CANCELLED; create notifications; save result; commit. Sandbox refund outcome is synchronous; FAILED refunds remain visible and retryable by admin. Cancellation remains CANCELLED even when a refund fails. Reusing the same refund row for retry avoids duplicate refund creation.
6. **Complete:** lock booking; verify assigned consultant, successful payment, and end time; persist COMPLETED plus event/notifications; commit.

Use a consistent lock order: idempotency record -> slot (creation only) -> booking -> payment -> refund. Commands operating on an existing booking do not acquire a slot lock. Hibernate `@Version` detects stale admin/availability changes; the HTTP `version` fields are mandatory for those edits. Map serialization/deadlock conflicts to a retryable 409, never a partial success.

Idempotency is required for booking creation, sandbox payments, and cancellation. Unique scope is `(actor, operation scope, key)`, where scope includes target ID for object commands. Store a canonical validated-request SHA-256 hash and the response in the same transaction. Same key/hash replays the response; same key/different hash returns 409. Concurrent duplicates wait for the first transaction or return a retryable conflict. Retain results at least seven days; successful payment uniqueness and quote consumption still protect against duplicates after key expiry. Do not persist cookies, passwords, CSRF tokens or payment secrets in the result store.

The expiry worker locks overdue REQUESTED/CONFIRMED rows with `FOR UPDATE SKIP LOCKED`, applies the same State service, and commits in small batches. Indexes support those queries. A public slot list excludes reservations and past slots; expiry is also checked lazily by create commands before reporting availability. All list queries have stable `(created_at, id)` ordering and enforced ownership predicates.

## HTTP conventions

- Prefix `/api/v1`; timestamps RFC3339 with offset, stored as `TIMESTAMPTZ`; render in the user's IANA timezone. Reject timestamps without offsets.
- UUIDs identify domain objects; page starts at 1, default size 20, max 100; sort order is endpoint-defined. Paginated responses include items/page/size/totalElements/totalPages.
- DTOs reject unknown writable fields. Bean Validation bounds strings and validates enums, decimal amounts, and date ranges. Never bind request bodies directly to JPA entities.
- Errors use `application/problem+json` with status, code, title, detail, instance and optional fieldErrors; never include SQL, stack traces, provider credentials or other people's details.
- GET never creates data. Policy versions append; switching the current pointer is explicit. No destructive deletes of booking/payment/refund/audit history.
- Audit records contain actor, action, object identity and a small allowlisted JSON summary; exclude authentication/payment/chat message contents. Retention/export/delete policy needs a deployment-specific decision before real-user use.

## Architecture

Controllers perform HTTP mapping and validation; application services authorize and coordinate transactions; State/Strategy/Builder classes implement behavior; repositories enforce scoped reads and locking. Database guards protect invariants if a caller bypasses the service, but do not establish authenticated identity. The database owner used for migrations must differ from the limited runtime role; SQL owner access can always bypass application authorization.
