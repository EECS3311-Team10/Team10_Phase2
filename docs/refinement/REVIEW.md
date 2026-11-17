# Audit and implementation order

This is a source review, not a claim that the application has been penetration-tested. Paths below refer to the reviewed commit.

| Priority | Evidence | User-visible failure | Required implementation |
|---|---|---|---|
| P0 | `config/SecurityConfig.java`: `anyRequest().permitAll()`, `NoOpPasswordEncoder`; `UserController.login` compares strings | Anyone can call administrative or another user's endpoints; login does not establish server identity | BCrypt, persistent session security context, CSRF, role/owner authorization, safe account DTOs |
| P0 | `users/User.java`: password getter; `UserController` returns entities | Password fields can be serialized in responses | Separate request/response DTOs; never expose password hashes |
| P0 | `model/Booking.java` has no consultant or slot relationship; `request-booking.html` sends a consultant ID that is discarded | Requests cannot reliably belong to a consultant; no dependable reservation | Foreign keys to client, consultant offering, slot, and immutable quote |
| P0 | `BookingController` accepts client, status, price, service name, and times from the request | A client can forge identity or price | Quote endpoint; derive actor from the session and all financial/session values on the server |
| P0 | `PaymentController` writes booking/payment separately; `/bookings/{id}/pay` directly changes status | Duplicate payments, unpaid “paid” bookings, partial writes | One transactional application service; sandbox payment DTO; idempotency and successful-payment uniqueness |
| P1 | `BaseBookingState` methods silently do nothing; transient state is not restored on JPA load | Invalid actions succeed silently; persisted and in-memory state disagree | Enum-backed State factory; invalid transitions produce 409; persist the new status in the same transaction |
| P1 | `Availability` stores date/time strings without a reservation or overlap constraint | Two clients can reserve the same session | Typed UTC intervals, exclusion constraint, slot lock and unique active booking |
| P1 | `PolicyController.GET` creates data; policy values are free text; `PolicyManager` holds separate mutable defaults | Displayed policy differs from executed policy; past bookings change meaning | Immutable typed versions; current-policy pointer; quote freezes the chosen version |
| P1 | Cancellation rejects late cancellation while policy promises a 50% refund | Policy cannot be carried out; refunds do not exist as a ledger | Server-calculated cancellation preview, atomic cancellation/refund record, retryable sandbox refund |
| P1 | `NotificationService` stores only in-memory lists and is not wired as a Spring service | Notifications disappear on restart | Persist notifications and booking events with each transaction |
| P1 | `review-booking-requests.html` fetches all requests; role checks use sessionStorage | Consultants can see or act on unrelated requests | Server-scoped list/detail queries; sessionStorage is never authority |
| P2 | Controllers have ad-hoc errors and raw entities; no test directory | UI cannot distinguish validation, access, conflict, and upstream failure | Bean Validation, Problem Details, pagination, controller/service/database tests |
| P2 | `ChatbotService` concatenates user/system text and returns raw exception messages | Fragile responses and leaked internal details | Separate system/user messages, bounded input, timeout, sanitized 503, public-data-only context |
| P2 | HTML pages repeat fetch/auth logic and interpolate API data into HTML | Inconsistent error/loading behavior; unsafe rendering | Shared API module; `textContent`/escaping; componentized pages and common layout |

## Delivery sequence

1. **Foundation (this change):** explicit rules/access matrix, complete v1 HTTP contract, fresh schema, relational/financial guards, repeatable validation scripts.
2. **Identity and catalog:** Spring Security session login/logout/CSRF, BCrypt bootstrap admin outside source, DTOs, consultant review, services and offerings. Remove or deny legacy write routes before exposing v1.
3. **Booking slice:** quote calculation with BigDecimal, enum-backed State implementations, transactions/locks, reservation, consultant decision, deadlines and expiry worker. Read models support client and consultant dashboards.
4. **Money and history:** idempotent sandbox payment/refund, cancellation preview, event history, persisted notifications, admin audit. Make duplicate/retried requests safe.
5. **UI:** implement the flows in `UI.md`, then visual polish. Wire all pages to v1 and remove insecure legacy behavior.
6. **Completion checks:** integration tests, concurrent booking tests, setup documentation, demo seed, CI, dependency/runtime review and deployment configuration.

Keep the course's State, Strategy, and Builder patterns where they express real behavior. The State implementations govern allowed transitions; Pricing/Refund strategies implement typed policy versions; NotificationBuilder builds persisted notifications. Remove duplicate enum/classes and static mutable policy state during integration. Do not add microservices, Redis, Kafka, or a real payment provider to this course-sized system.

## Acceptance scenarios for runtime integration

- Anonymous calls receive 401; wrong role or ownership never reads another person's record; passwords never appear in JSON or audit payloads.
- Registration creates a client or pending consultant; a request cannot create an administrator. Rejected/suspended consultants cannot create new slots or accept requests.
- Two concurrent clients booking the same slot produce exactly one booking and one 409. Adjacent slots are allowed; overlapping slots for the same consultant are rejected.
- An altered browser price/client/consultant/status cannot affect a booking. An expired quote or changed slot produces 409. Policy/catalog updates do not alter existing quotes/bookings.
- Acceptance, rejection, cancellation, expiry, payment, and completion check state and actor under a booking lock. Payment versus cancellation and acceptance versus expiry races have a single consistent outcome.
- Repeating a payment with the same idempotency key returns the first result; different input with that key produces 409; a different key cannot charge an already paid booking.
- Cancellation at exactly 24 hours refunds 100%; one second inside the boundary refunds 50%; consultant cancellation refunds 100%; no refund exceeds the successful payment. The original payment remains SUCCEEDED for reconciliation.
- Expiry releases the reservation, creates an event/notification, and never expires a paid booking. An overdue hold cannot be paid even if the expiry worker is delayed.
- Completion before the end or without payment returns 409. Terminal states reject unsupported actions rather than silently succeeding.
- Booking, payment/refund, event, idempotency result, and notifications commit together or all roll back.
- Bad input is 400; absent object 404; conflicts 409; AI outage 503. Lists are paginated, scoped, and stably sorted.

## Validation boundary

SQL tests verify database guards, not HTTP authentication or the unimplemented Spring adapters. Contract validation checks internal references, path parameters, operation IDs, request schemas, and response coverage. Full HTTP end-to-end tests must be added when v1 controllers exist. Validation commands and actual results belong in `DATABASE.md` and the PR description.
