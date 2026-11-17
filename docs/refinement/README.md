# ConsultLink refinement baseline

Status: API/database design and isolated database implementation. The existing Spring controllers and HTML pages still use the legacy schema and endpoints. These files do not silently change that application.

Reviewed commit: `9ef0c4454df764a99ba68c2f2e8d7f909c00d734`.

## Deliverables

- [Code audit and implementation order](REVIEW.md)
- [Domain rules, transactions, and access model](WORKFLOWS.md)
- [Database structure and setup](DATABASE.md)
- [OpenAPI contract](../../openapi/consultlink-v1.json)
- [Fresh PostgreSQL schema](../../database/migrations/V1__consultlink_core.sql)
- [Database constraint tests](../../database/tests/constraints.sql)
- [UI requirements](UI.md)

The old demo tables do not need to be preserved. The new schema is named `consultlink_v1`, so it can be inspected and tested without deleting the old `public` tables. Nothing here runs automatically through Hibernate or Flyway yet. The runtime cutover must replace the legacy mappings and disable `ddl-auto=update` first.

## Product scope

Clients find an approved consultant, choose a service and available session, receive a server-calculated quote, request a booking, pay after acceptance, and see cancellation/refund progress. Consultants manage their offered services and slots, review only their own requests, and complete paid sessions after they finish. Administrators review consultant applications, manage the catalog and versioned policies, and inspect audit history.

Payments remain a simulation for the course project. No real card number, CVV, bank credentials, or provider transfer is part of the redesigned API. The AI assistant answers questions from public catalog/policy data; it cannot perform account or payment actions.

## Decisions retained from the project

- A consultant must be approved before accepting bookings.
- Administrators set service prices.
- Requests need consultant acceptance before payment.
- Urgent sessions (less than six hours away) have a 15% surcharge.
- Client cancellation at least 24 hours before the session refunds 100%; later cancellation before the session refunds 50%.
- A consultant cancellation refunds 100%.
- Completion requires a successful payment and a finished session.

The original policy text says both “more than 24 hours” and “within 24 hours.” This baseline explicitly assigns the exact 24-hour boundary to the full refund. All boundaries are covered in the implementation checklist.

## Proposed defaults

These are design choices, not requirements recovered from the old implementation: CAD; per-service session durations; UTC storage with an IANA display timezone; 15-minute quotes; 24-hour request holds; 30-minute payment windows after acceptance; no self-service rescheduling in v1. Holds are capped at the session start. All durations and percentages except quote expiry are versioned policy fields. Currency is attached to each service and frozen on each quote. Display timezone is a user preference. Rescheduling can later be an atomic slot swap with a fresh quote.

## Runtime cutover acceptance

Do not treat the redesign as complete until the new Java services/controllers, authentication, migration configuration, expiry job, and pages use this contract and pass the scenarios in `REVIEW.md`. Merely creating these tables does not repair the currently open legacy APIs.
