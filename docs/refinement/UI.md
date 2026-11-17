# UI completion requirements

First connect the existing HTML pages to working v1 services. A Vue conversion is optional after the workflows are stable; the project currently uses static HTML, CSS and JavaScript.

| Area | Required flow | Information and states |
|---|---|---|
| Shared shell | Server-backed session, role navigation, profile, logout | Name/role, approval status, notification count; loading, empty, offline, validation and conflict states |
| Client discovery | Service -> approved consultant -> session -> quote -> request | Service duration and CAD price, display timezone, open slots, itemized surcharge/discount, quote expiry; conflict refreshes slots |
| Client bookings | Tabs for action needed / upcoming / history; details and timeline | Consultant, service, local date/time, persisted status, deadline, quoted total, pay/cancel controls derived from server allowedActions |
| Payment | Sandbox method and explicit demo outcome | “Simulated payment”; no card/CVV inputs; disable double submit, preserve idempotency key during retry, display recorded outcome |
| Cancellation | Preview -> reason -> submit -> result | Exact refund/fee, policy boundary, refund pending/succeeded/failed; refresh when time changes |
| Consultant onboarding | Submit application -> pending -> approved/rejected | Clear approval status and review reason; no availability/booking actions before approval |
| Consultant calendar | Own offerings and dated slots | Offset-aware times, service duration, overlap errors, reserved slots protected from editing; no free-text date/time fields |
| Consultant inbox | Requests -> accept/reject -> upcoming -> complete | Only assigned bookings; deadline, client display name, notes; complete appears after paid session ends |
| Admin | Approval queue, service catalog, policies, audit | Search/pagination, reasons on decisions, version-conflict handling, new immutable policy versions; no arbitrary status editing |
| Notifications | List, unread filter, mark read, related booking | Persisted read status, timestamps and navigation; safe text rendering |
| Assistant | Public policy/catalog Q&A | Source policy version, bounded input, timeout/retry state, visible informational scope |

Create one `api.js` module for credentials, CSRF refresh, JSON/error mapping, idempotency keys, and 401 handling. One session module loads `/auth/me`; a browser storage value is never evidence of authorization. Use `textContent` or framework escaping for names, notes, service descriptions and model output. Avoid `innerHTML` with API values.

Use a consistent content width, spacing scale, status chips and form controls. Keep the next action and deadline visible on booking cards. Use explicit empty messages and a small action to recover from errors. Every form needs labels, keyboard access, inline field errors, a submitting state, and preserved values after failure. Display both timezone abbreviation and full timezone name in scheduling confirmation. Test desktop and narrow mobile widths before visual polish is called complete.

Do not mix legacy and v1 payment/booking mutations in a page. During cutover, migrate an entire workflow together, then remove its legacy endpoint and duplicated JavaScript.
