/* Regenerate the committed contract with: node scripts/build-openapi.cjs */
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const ref = name => ({ $ref: `#/components/schemas/${name}` });
const string = (maxLength = 1000, extra = {}) => ({ type: 'string', maxLength, ...extra });
const uuid = { type: 'string', format: 'uuid' };
const instant = { type: 'string', format: 'date-time', description: 'RFC3339 timestamp including an offset.' };
const integer = (minimum = 0, maximum = 2147483647) => ({ type: 'integer', minimum, maximum });
const bool = { type: 'boolean' };
const money = { type: 'string', pattern: '^[0-9]+\\.[0-9]{2}$', example: '100.00', description: 'Exact decimal amount; never a JSON floating-point number.' };
const currency = { type: 'string', pattern: '^[A-Z]{3}$', example: 'CAD' };
const values = (...items) => ({ type: 'string', enum: items });
const array = items => ({ type: 'array', items });
const object = (properties, required = Object.keys(properties)) => ({ type: 'object', additionalProperties: false, properties, required });
const nullable = schema => ({ ...schema, nullable: true });
const version = integer();
const schemas = {};
const add = (name, properties, required) => schemas[name] = object(properties, required);
const statuses = ['REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED','REJECTED','CANCELLED','EXPIRED'];
const roles = ['CLIENT','CONSULTANT','ADMIN'];
const approval = values('PENDING','APPROVED','REJECTED');
const reason = string(2000, { minLength: 1 });
const password = string(72, { minLength: 12, writeOnly: true, description: '12+ characters; backend also enforces a maximum of 72 UTF-8 bytes for BCrypt. Never returned or logged.' });
const profileProperties = {
  id: uuid, loginName: string(64), email: string(254, { format: 'email' }), displayName: string(100),
  role: values(...roles), status: values('ACTIVE','SUSPENDED'), timezone: string(64), version,
  approvalStatus: nullable(approval)
};
add('User', profileProperties);
add('CsrfToken', { headerName: values('X-CSRF-TOKEN'), token: string(512) });
add('RegisterRequest', {
  loginName: string(64, { pattern: '^[A-Za-z0-9_.-]{3,64}$' }), email: string(254, { format: 'email' }),
  password, displayName: string(100, { minLength: 1 }), role: values('CLIENT','CONSULTANT'),
  timezone: string(64, { example: 'America/Toronto', description: 'Valid IANA ZoneId.' }),
  headline: string(160), biography: string(4000)
}, ['loginName','email','password','displayName','role','timezone']);
add('LoginRequest', { loginName: string(64, { minLength: 3 }), password: string(72, { minLength: 1, writeOnly: true }) });
add('UpdateProfileRequest', { displayName: string(100, { minLength: 1 }), timezone: string(64), version });
add('ChangePasswordRequest', { currentPassword: string(72, { minLength: 1, writeOnly: true }), newPassword: password });
add('Consultant', { id: uuid, displayName: string(100), headline: string(160), biography: string(4000), offerings: array(ref('PublicOffering')) });
add('Service', { id: uuid, name: string(120), description: string(4000), durationMinutes: integer(15,480), baseAmount: money, currency, active: bool, version });
const serviceInput = { name: string(120, { minLength: 1 }), description: string(4000), durationMinutes: integer(15,480), baseAmount: money, currency, active: bool };
add('CreateServiceRequest', serviceInput);
add('UpdateServiceRequest', { ...serviceInput, version });
add('Offering', { id: uuid, consultantId: uuid, service: ref('Service'), active: bool, version });
add('PublicOffering', { id: uuid, service: ref('Service') });
add('SetOfferingRequest', { active: bool, version: integer(-1), }, ['active','version']);
schemas.SetOfferingRequest.properties.version.description = 'Use -1 only when creating this service offering for the first time; otherwise use its current version.';
add('Slot', { id: uuid, consultantId: uuid, startsAt: instant, endsAt: instant, status: values('OPEN','BLOCKED','ARCHIVED'), available: bool, version });
add('CreateSlotRequest', { startsAt: instant, endsAt: instant });
add('UpdateSlotRequest', { status: values('OPEN','BLOCKED','ARCHIVED'), version });
const policyFields = {
  fullRefundHours: integer(0,720), lateRefundPercent: { type: 'number', minimum: 0, maximum: 100, multipleOf: 0.01 },
  urgencyHours: integer(0,720), urgencyPercent: { type: 'number', minimum: 0, maximum: 100, multipleOf: 0.01 },
  discountPercent: { type: 'number', minimum: 0, maximum: 99, multipleOf: 0.01 },
  requestHoldMinutes: integer(1,10080), paymentWindowMinutes: integer(1,1440)
};
add('Policy', { id: uuid, versionNumber: integer(1), ...policyFields, createdAt: instant });
add('CreatePolicyRequest', { ...policyFields, changeReason: string(1000, { minLength: 1 }) });
add('CurrentPolicy', { policy: ref('Policy'), settingsVersion: version });
add('ActivatePolicyRequest', { policyId: uuid, settingsVersion: version });
add('ConsultantApplication', { user: ref('User'), headline: string(160), biography: string(4000), approvalStatus: approval, reviewReason: nullable(string(1000)), reviewedAt: nullable(instant), version });
add('ReviewApplicationRequest', { decision: values('APPROVED','REJECTED'), reason: string(1000, { minLength: 1 }), version });
add('SetUserStatusRequest', { status: values('ACTIVE','SUSPENDED'), reason: string(1000, { minLength: 1 }), version });
add('QuoteRequest', { offeringId: uuid, slotId: uuid });
add('Quote', {
  id: uuid, consultantId: uuid, offeringId: uuid, slotId: uuid, serviceName: string(120), startsAt: instant, endsAt: instant,
  baseAmount: money, urgencyAmount: money, discountAmount: money, totalAmount: money, currency,
  policyId: uuid, policyVersionNumber: integer(1), createdAt: instant, expiresAt: instant
});
add('CreateBookingRequest', { quoteId: uuid, notes: string(2000) }, ['quoteId']);
const actions = ['ACCEPT','REJECT','PAY','CANCEL','COMPLETE'];
add('Booking', {
  id: uuid, clientId: uuid, clientDisplayName: string(100), consultantId: uuid, consultantDisplayName: string(100),
  quote: ref('Quote'), status: values(...statuses), notes: string(2000), requestExpiresAt: instant,
  paymentDueAt: nullable(instant), allowedActions: array(values(...actions)), version, createdAt: instant, updatedAt: instant
});
add('VersionRequest', { version });
add('ReasonVersionRequest', { reason, version });
add('BookingEvent', { id: uuid, bookingId: uuid, actorId: nullable(uuid), action: string(40), fromStatus: nullable(values(...statuses)), toStatus: values(...statuses), reason: nullable(string(2000)), createdAt: instant });
add('CancellationPreview', {
  bookingId: uuid, bookingVersion: version, paidAmount: money, refundableAmount: money, feeAmount: money, currency,
  rule: values('UNPAID','CLIENT_FULL','CLIENT_LATE','CONSULTANT_FULL'), policyId: uuid, calculatedAt: instant
});
add('PaymentRequest', {
  method: values('CREDIT_CARD','DEBIT_CARD','PAYPAL','BANK_TRANSFER'),
  sandboxOutcome: values('SUCCESS','DECLINED')
});
schemas.PaymentRequest.description = 'Course-project simulation only. No card number, CVV, bank details, arbitrary amount or client ID is accepted.';
add('Payment', { id: uuid, bookingId: uuid, clientId: uuid, provider: values('SANDBOX'), method: schemas.PaymentRequest.properties.method, status: values('SUCCEEDED','FAILED'), amount: money, currency, failureCode: nullable(string(64)), createdAt: instant });
add('PaymentResult', { payment: ref('Payment'), booking: ref('Booking') });
add('Refund', { id: uuid, paymentId: uuid, bookingId: uuid, cancellationEventId: uuid, amount: money, currency, status: values('REQUESTED','SUCCEEDED','FAILED'), failureCode: nullable(string(64)), attemptCount: integer(), createdAt: instant, updatedAt: instant });
add('CancellationResult', { booking: ref('Booking'), refund: { ...schemas.Refund, nullable: true, description: 'Null when there was no successful payment or refundable amount is zero.' } });
add('RetryRefundRequest', { attemptCount: integer() });
add('Notification', { id: uuid, type: string(40), message: string(2000), bookingId: nullable(uuid), readAt: nullable(instant), createdAt: instant });
add('MarkReadRequest', { read: { type: 'boolean', enum: [true] } });
add('AuditEvent', { id: uuid, actorId: nullable(uuid), action: string(80), resourceType: string(40), resourceId: nullable(uuid), summary: { type: 'object', additionalProperties: true, description: 'Allowlisted metadata only; no credentials, payment details or message bodies.' }, createdAt: instant });
add('AssistantRequest', { message: string(2000, { minLength: 1 }) });
add('AssistantResponse', { answer: string(12000), policyId: uuid, policyVersionNumber: integer(1), informationalOnly: { type: 'boolean', enum: [true] } });
add('FieldError', { field: string(100), message: string(500) });
add('Problem', {
  type: string(500, { format: 'uri' }), title: string(200), status: integer(400,599), code: string(80),
  detail: string(1000), instance: string(500), fieldErrors: array(ref('FieldError'))
}, ['type','title','status','code','detail','instance']);
for (const name of ['User','Consultant','Service','Offering','Slot','Policy','ConsultantApplication','Booking','BookingEvent','Payment','Refund','Notification','AuditEvent']) {
  add(`${name}Page`, { items: array(ref(name)), page: integer(1), size: integer(1,100), totalElements: integer(), totalPages: integer() });
}

const paths = {};
const response = (description, schema) => ({ description, ...(schema ? { content: { 'application/json': { schema } } } : {}) });
const problem = description => ({ description, content: { 'application/problem+json': { schema: ref('Problem') } } });
const query = (name, schema, description, required = false) => ({ name, in: 'query', required, schema, description });
const paged = [query('page', { ...integer(1), default: 1 }, 'One-based page.'), query('size', { ...integer(1,100), default: 20 }, 'Page size.')];
const cs = { $ref: '#/components/parameters/Csrf' };
const idem = { $ref: '#/components/parameters/IdempotencyKey' };
function op(method, route, id, tag, summary, output, options = {}) {
  const status = options.status || (method === 'post' ? 201 : 200);
  const parameters = [...(options.parameters || [])];
  for (const match of route.matchAll(/\{(\w+)\}/g)) {
    parameters.push({ name: match[1], in: 'path', required: true, schema: uuid });
  }
  if (!['get','head','options'].includes(method)) parameters.push(cs);
  if (options.idempotent) parameters.push(idem);
  const responses = {
    [status]: response(options.success || 'Success.', output ? ref(output) : null),
    400: problem('Invalid request or validation failure.'),
    ...(options.public ? {} : { 401: problem('An authenticated session is required.') }),
    403: problem('Role is disallowed, consultant is not approved, account is suspended, or CSRF token is invalid.'),
    404: problem('Resource is absent or is outside the authenticated user\'s scope.'),
    409: problem('State, capacity, deadline, duplicate identity, version or idempotency conflict.'),
    ...(options.errors || {})
  };
  const operation = {
    operationId: id, tags: [tag], summary,
    description: options.description || summary,
    security: options.public ? [] : [{ sessionCookie: [] }],
    parameters, responses, 'x-roles': options.roles || (options.public ? ['PUBLIC'] : roles)
  };
  if (options.body) operation.requestBody = { required: true, content: { 'application/json': { schema: ref(options.body) } } };
  (paths[route] ||= {})[method] = operation;
}
const admin = { roles: ['ADMIN'] };
const consultant = { roles: ['CONSULTANT'] };
const client = { roles: ['CLIENT'] };
const own = 'Private responses are scoped to the authenticated actor. Access to another user\'s object returns 404. ';
op('get','/auth/csrf','getCsrf','Auth','Obtain a CSRF token','CsrfToken',{ public: true, description: 'Creates/uses an anonymous session. Re-fetch after login/logout. Send the returned token in X-CSRF-TOKEN on all writes.' });
op('post','/auth/register','register','Auth','Register a client or pending consultant','User',{ public: true, body: 'RegisterRequest', description: 'Creates an account but does not log in. CLIENT ignores consultant biography fields; CONSULTANT creates a PENDING application. ADMIN is never a public registration role.' });
op('post','/auth/login','login','Auth','Establish an authenticated session','User',{ public: true, body: 'LoginRequest', status: 200, errors: { 401: problem('Invalid credentials.'), 429: problem('Too many attempts.') }, description: 'Authenticate using BCrypt; save SecurityContext and rotate the session ID. Pending consultants may sign in to view their application but cannot manage bookings. Suspended accounts are denied. Password is never returned.' });
op('post','/auth/logout','logout','Auth','Invalidate the current session',null,{ status: 204 });
op('get','/auth/me','getMe','Auth','Get the current server identity','User');
op('patch','/me','updateMe','Accounts','Update own display name and timezone','User',{ body: 'UpdateProfileRequest' });
op('post','/me/password','changePassword','Accounts','Change own password and invalidate all sessions',null,{ body: 'ChangePasswordRequest', status: 204, description: 'Verify current password, BCrypt-hash new password, increment credentialVersion, audit only the action, and invalidate all user sessions including the current one. The client then fetches a fresh CSRF token and signs in again.' });

op('get','/services','listServices','Catalog','List active services','ServicePage',{ public: true, parameters: paged });
op('get','/consultants','listConsultants','Catalog','List approved active consultants','ConsultantPage',{ public: true, parameters: [...paged, query('serviceId',uuid,'Filter consultants offering this active service.')] });
op('get','/consultants/{consultantId}','getConsultant','Catalog','Get an approved consultant profile','Consultant',{ public: true });
op('get','/consultants/{consultantId}/slots','listOpenSlots','Catalog','List future unreserved OPEN slots','SlotPage',{ public: true, parameters: [...paged,query('from',instant,'Inclusive range start.',true),query('to',instant,'Exclusive range end; range must not exceed 31 days.',true),query('serviceId',uuid,'Only slots matching this offered service duration.',true)], description: 'Only approved active consultants and active offerings are returned. Exclude blocked/archived, past, and reserved slots. A displayed slot can still be taken before quote/booking creation.' });
op('get','/policies/current','getCurrentPolicy','Policies','Get current published policy','CurrentPolicy',{ public: true });

op('get','/me/offerings','listMyOfferings','Consultant','List own service offerings','OfferingPage',{ ...consultant, parameters: paged });
op('put','/me/offerings/{serviceId}','setMyOffering','Consultant','Offer or stop offering an active catalog service','Offering',{ ...consultant, body: 'SetOfferingRequest', description: 'Requires active approved consultant. Disabling an offering prevents new requests; it does not alter existing bookings or quote prices. Use version -1 for creation, current version for updates.' });
op('get','/me/slots','listMySlots','Consultant','List own slots including blocked and reserved slots','SlotPage',{ ...consultant, parameters: [...paged, query('from',instant,'Inclusive range start.',true),query('to',instant,'Exclusive range end; max 31 days.',true)] });
op('post','/me/slots','createSlot','Consultant','Create a future non-overlapping slot','Slot',{ ...consultant, body: 'CreateSlotRequest', description: 'Requires active approved consultant. Duration 15 minutes to 8 hours. Adjacent intervals are allowed; overlap with another non-archived slot is 409.' });
op('patch','/me/slots/{slotId}','updateSlot','Consultant','Block, reopen or archive an unreserved slot','Slot',{ ...consultant, body: 'UpdateSlotRequest', description: own + 'Requires active approved consultant. Reserved slots cannot be blocked or archived. Quoted start/end times are immutable; archive an unreserved old slot and create a new one.' });

op('post','/quotes','createQuote','Bookings','Calculate and freeze a server-priced quote','Quote',{ ...client, body: 'QuoteRequest', description: 'Client identity comes from session. Consultant/service/price/time/policy come from the selected offering and slot. Expiry is min(now + 15 minutes, session start). Quotes do not reserve slots.' });
op('post','/bookings','createBooking','Bookings','Reserve a session from an unexpired quote','Booking',{ ...client, body: 'CreateBookingRequest', idempotent: true, description: 'Atomically consume the current client\'s quote, reserve its slot, create REQUESTED booking/event/notifications. Reject expired, consumed, inactive, unavailable or mismatched quotes with 409. Ignore no client-controlled financial fields: reject unknown input instead.' });
op('get','/bookings','listBookings','Bookings','List bookings visible to the current actor','BookingPage',{ parameters: [...paged,query('status',values(...statuses),'Optional status filter.'),query('from',instant,'Optional session start bound.'),query('to',instant,'Optional session end bound.')], description: own + 'Client sees own bookings; consultant sees assigned bookings; admin sees all. Sort createdAt descending, id ascending.' });
op('get','/bookings/{bookingId}','getBooking','Bookings','Get booking details and available actions','Booking',{ description: own + 'allowedActions derives from current state, actor, approval and deadlines. It is a UI hint; mutations independently authorize again.' });
op('get','/bookings/{bookingId}/events','listBookingEvents','Bookings','Get a booking timeline','BookingEventPage',{ parameters: paged, description: own + 'Sort createdAt ascending, id ascending.' });
op('post','/bookings/{bookingId}/accept','acceptBooking','Bookings','Accept an assigned request','Booking',{ ...consultant, body: 'VersionRequest', status: 200, description: own + 'REQUESTED -> CONFIRMED before the request deadline; set payment deadline from frozen policy. Require active approved consultant.' });
op('post','/bookings/{bookingId}/reject','rejectBooking','Bookings','Reject an assigned request with a reason','Booking',{ ...consultant, body: 'ReasonVersionRequest', status: 200, description: own + 'REQUESTED -> REJECTED; release reservation and notify client. Require active approved consultant.' });
op('get','/bookings/{bookingId}/cancellation-preview','previewCancellation','Bookings','Preview cancellation fee and refund','CancellationPreview',{ roles: ['CLIENT','CONSULTANT'], description: own + 'Only before session start and in a cancellable state. Uses the booking\'s frozen policy. Mutation recalculates under lock.' });
op('post','/bookings/{bookingId}/cancel','cancelBooking','Bookings','Cancel and record any sandbox refund','CancellationResult',{ roles: ['CLIENT','CONSULTANT'], body: 'ReasonVersionRequest', idempotent: true, status: 200, description: own + 'Lock booking/payment. Before session start, cancel eligible booking; insert refund for server-calculated positive amount, event and notifications in one transaction. A failed refund stays visible for admin retry. Client full refund at >=24h; late refund 50%; consultant refund 100%, according to the frozen policy.' });
op('post','/bookings/{bookingId}/complete','completeBooking','Bookings','Complete an assigned paid session after its end','Booking',{ ...consultant, body: 'VersionRequest', status: 200, description: own + 'PAID -> COMPLETED only at/after endsAt, with a recorded successful payment.' });

op('post','/bookings/{bookingId}/payments','payBooking','Payments','Run an idempotent simulated payment','PaymentResult',{ ...client, body: 'PaymentRequest', idempotent: true, description: own + 'CONFIRMED -> PENDING_PAYMENT -> PAID on sandbox success, or back to CONFIRMED on decline. Payment amount/currency derive from quote; deadline is enforced even if expiry worker is delayed. Decline is a recorded FAILED payment, not an HTTP transport failure. All writes and the replay response are atomic.' });
op('get','/payments','listPayments','Payments','List own payments or all payments for admin','PaymentPage',{ roles: ['CLIENT','ADMIN'], parameters: [...paged,query('bookingId',uuid,'Optional booking filter.')], description: own + 'A failed attempt and its later successful retry are separate immutable rows. One successful payment per booking.' });
op('get','/refunds','listRefunds','Payments','List own refunds or all refunds for admin','RefundPage',{ roles: ['CLIENT','ADMIN'], parameters: [...paged,query('bookingId',uuid,'Optional booking filter.')] });
op('post','/admin/refunds/{refundId}/retry','retryRefund','Admin','Retry a failed sandbox refund','Refund',{ ...admin, body: 'RetryRefundRequest', status: 200, description: 'Lock the booking/payment/refund; require FAILED and matching attemptCount, increment once and retry the same refund record. Sandbox retry succeeds deterministically. Concurrent stale retries return 409. Append an audit event. Never create a second refund for the same cancellation.' });

op('get','/notifications','listNotifications','Notifications','List own persisted notifications','NotificationPage',{ parameters: [...paged,query('unreadOnly',bool,'Only unread notifications.')] });
op('patch','/notifications/{notificationId}','markNotificationRead','Notifications','Mark own notification read','Notification',{ body: 'MarkReadRequest', description: own + 'Repeated mark-read requests are naturally idempotent; preserve the first read timestamp.' });
op('post','/assistant/messages','askAssistant','Assistant','Ask about public policies or services','AssistantResponse',{ body: 'AssistantRequest', status: 200, errors: { 429: problem('Message rate limit exceeded.'), 503: problem('Assistant unavailable or timed out.') }, description: 'Use separate system/user messages and current public policy/catalog as context. No private account context, tool actions, bookings, payment credentials or raw exceptions. Model output is untrusted text. Return the policy version used.' });

op('get','/admin/users','listUsers','Admin','List safe account summaries','UserPage',{ ...admin, parameters: [...paged,query('role',values(...roles),'Role filter.'),query('status',values('ACTIVE','SUSPENDED'),'Account status filter.')] });
op('patch','/admin/users/{userId}/status','setUserStatus','Admin','Suspend or reactivate an account','User',{ ...admin, body: 'SetUserStatusRequest', description: 'Reason and optimistic version required. Never allow self-suspension or removal of the last active admin. Invalidate suspended user sessions; leave existing bookings and financial history intact. Audit the decision.' });
op('get','/admin/consultant-applications','listApplications','Admin','List consultant applications','ConsultantApplicationPage',{ ...admin, parameters: [...paged,query('status',approval,'Approval status filter.')] });
op('post','/admin/consultant-applications/{consultantId}/review','reviewApplication','Admin','Approve or reject a pending consultant application','ConsultantApplication',{ ...admin, body: 'ReviewApplicationRequest', status: 200, description: 'PENDING -> APPROVED or REJECTED. Reason and matching version required. Record reviewer/time, notification and audit event. No physical account deletion.' });
op('get','/admin/services','listAdminServices','Admin','List active and inactive services','ServicePage',{ ...admin, parameters: paged });
op('post','/admin/services','createService','Admin','Create a catalog service','Service',{ ...admin, body: 'CreateServiceRequest' });
op('put','/admin/services/{serviceId}','updateService','Admin','Update a catalog service using its version','Service',{ ...admin, body: 'UpdateServiceRequest', description: 'Update catalog data for future quotes. Existing immutable quotes/bookings retain their title, duration, currency and price. Deactivation blocks new requests. No delete route.' });
op('get','/admin/policies','listPolicyVersions','Admin','List immutable policy versions','PolicyPage',{ ...admin, parameters: paged });
op('post','/admin/policies','createPolicyVersion','Admin','Create a typed immutable policy version','Policy',{ ...admin, body: 'CreatePolicyRequest', description: 'Allocate versionNumber on the server. New versions are not active until explicitly selected. Record change reason and creator in audit/database; do not expose internal reason in public policy.' });
op('put','/admin/policies/current','activatePolicy','Admin','Select the current policy version','CurrentPolicy',{ ...admin, body: 'ActivatePolicyRequest', description: 'Update singleton current-policy pointer using settingsVersion. Existing quotes and bookings retain their original policy. Bootstrap setup creates the first version and pointer before accepting requests; GET never creates it.' });
op('get','/admin/audit-events','listAuditEvents','Admin','List append-only audit metadata','AuditEventPage',{ ...admin, parameters: [...paged,query('resourceType',string(40),'Resource type filter.'),query('resourceId',uuid,'Resource ID filter.')] });

const spec = {
  openapi: '3.0.3',
  info: { title: 'ConsultLink v1 API', version: '1.0.0-design', description: 'Target contract, not a claim that v1 controllers already exist. Read docs/refinement/WORKFLOWS.md for transaction/state semantics. Same-origin session authentication, CSRF on writes, sandbox-only payments, UTC instants, immutable pricing/policy snapshots.' },
  servers: [{ url: '/api/v1', description: 'Same origin; use HTTPS outside local development.' }],
  tags: ['Auth','Accounts','Catalog','Consultant','Bookings','Payments','Policies','Notifications','Assistant','Admin'].map(name => ({ name })),
  security: [{ sessionCookie: [] }], paths,
  components: {
    securitySchemes: { sessionCookie: { type: 'apiKey', in: 'cookie', name: 'JSESSIONID', description: 'HttpOnly session cookie established by login. Never supplied from sessionStorage.' } },
    parameters: {
      Csrf: { name: 'X-CSRF-TOKEN', in: 'header', required: true, schema: string(512), description: 'Token from GET /auth/csrf; required on every mutation including login/register/logout.' },
      IdempotencyKey: { name: 'Idempotency-Key', in: 'header', required: true, schema: string(128, { minLength: 8 }), description: 'Stable random key per logical command. Same key/input replays the first response; different input conflicts. Scope includes actor and operation/target; retained >=7 days.' }
    },
    schemas
  }
};
fs.mkdirSync(path.join(root, 'openapi'), { recursive: true });
fs.writeFileSync(path.join(root, 'openapi/consultlink-v1.json'), JSON.stringify(spec, null, 2) + '\n');
console.log(`Built ${Object.values(paths).reduce((n, p) => n + Object.keys(p).length, 0)} operations, ${Object.keys(schemas).length} schemas.`);
