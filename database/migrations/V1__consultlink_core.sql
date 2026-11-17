-- PostgreSQL 16+. Fresh, opt-in schema. Not a migration of the legacy public tables.
BEGIN;
CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA public;
CREATE SCHEMA consultlink_v1;
SET LOCAL search_path = consultlink_v1, public;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login_name VARCHAR(64) NOT NULL CHECK (login_name ~ '^[A-Za-z0-9_.-]{3,64}$'),
    email VARCHAR(254) NOT NULL CHECK (position('@' IN email) > 1),
    display_name VARCHAR(100) NOT NULL CHECK (length(btrim(display_name)) > 0),
    password_hash VARCHAR(100) NOT NULL CHECK (password_hash ~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$'),
    role VARCHAR(16) NOT NULL CHECK (role IN ('CLIENT','CONSULTANT','ADMIN')),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED')),
    timezone VARCHAR(64) NOT NULL DEFAULT 'America/Toronto',
    credential_version INTEGER NOT NULL DEFAULT 0 CHECK (credential_version >= 0),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id, role)
);
CREATE UNIQUE INDEX users_login_ci ON users (lower(login_name));
CREATE UNIQUE INDEX users_email_ci ON users (lower(email));

CREATE TABLE consultant_profiles (
    user_id UUID PRIMARY KEY,
    role VARCHAR(16) NOT NULL DEFAULT 'CONSULTANT' CHECK (role = 'CONSULTANT'),
    headline VARCHAR(160) NOT NULL DEFAULT '',
    biography VARCHAR(4000) NOT NULL DEFAULT '',
    approval_status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
        CHECK (approval_status IN ('PENDING','APPROVED','REJECTED')),
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    review_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    FOREIGN KEY (user_id, role) REFERENCES users(id, role),
    CHECK ((approval_status = 'PENDING' AND reviewed_by IS NULL AND reviewed_at IS NULL)
        OR (approval_status <> 'PENDING' AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL)),
    CHECK (approval_status <> 'REJECTED' OR length(btrim(review_reason)) > 0 AND review_reason IS NOT NULL)
);

CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL CHECK (length(btrim(name)) > 0),
    description VARCHAR(4000) NOT NULL DEFAULT '',
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 15 AND 480),
    base_amount NUMERIC(12,2) NOT NULL CHECK (base_amount > 0),
    currency CHAR(3) NOT NULL DEFAULT 'CAD' CHECK (currency ~ '^[A-Z]{3}$'),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE consultant_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultant_id UUID NOT NULL REFERENCES consultant_profiles(user_id),
    service_id UUID NOT NULL REFERENCES services(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    UNIQUE (consultant_id, service_id),
    UNIQUE (id, consultant_id)
);

CREATE TABLE policy_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_number INTEGER NOT NULL UNIQUE CHECK (version_number > 0),
    full_refund_hours INTEGER NOT NULL DEFAULT 24 CHECK (full_refund_hours BETWEEN 0 AND 720),
    late_refund_percent NUMERIC(5,2) NOT NULL DEFAULT 50 CHECK (late_refund_percent BETWEEN 0 AND 100),
    urgency_hours INTEGER NOT NULL DEFAULT 6 CHECK (urgency_hours BETWEEN 0 AND 720),
    urgency_percent NUMERIC(5,2) NOT NULL DEFAULT 15 CHECK (urgency_percent BETWEEN 0 AND 100),
    discount_percent NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (discount_percent BETWEEN 0 AND 99),
    request_hold_minutes INTEGER NOT NULL DEFAULT 1440 CHECK (request_hold_minutes BETWEEN 1 AND 10080),
    payment_window_minutes INTEGER NOT NULL DEFAULT 30 CHECK (payment_window_minutes BETWEEN 1 AND 1440),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason VARCHAR(1000) NOT NULL CHECK (length(btrim(change_reason)) > 0)
);
CREATE TABLE platform_settings (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    current_policy_id UUID NOT NULL REFERENCES policy_versions(id),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0)
);

CREATE TABLE availability_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultant_id UUID NOT NULL REFERENCES consultant_profiles(user_id),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','BLOCKED','ARCHIVED')),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (ends_at > starts_at AND ends_at <= starts_at + INTERVAL '8 hours'),
    UNIQUE (id, consultant_id),
    CONSTRAINT slots_no_overlap EXCLUDE USING gist
        (consultant_id WITH =, tstzrange(starts_at, ends_at, '[)') WITH &&)
        WHERE (status <> 'ARCHIVED')
);
CREATE INDEX slots_consultant_time ON availability_slots(consultant_id, starts_at, id);

CREATE TABLE booking_quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    client_role VARCHAR(16) NOT NULL DEFAULT 'CLIENT' CHECK (client_role = 'CLIENT'),
    consultant_id UUID NOT NULL REFERENCES consultant_profiles(user_id),
    offering_id UUID NOT NULL,
    slot_id UUID NOT NULL,
    policy_id UUID NOT NULL REFERENCES policy_versions(id),
    service_name VARCHAR(120) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    base_amount NUMERIC(12,2) NOT NULL CHECK (base_amount > 0),
    urgency_amount NUMERIC(12,2) NOT NULL CHECK (urgency_amount >= 0),
    discount_amount NUMERIC(12,2) NOT NULL CHECK (discount_amount >= 0 AND discount_amount < base_amount),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount > 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (client_id, client_role) REFERENCES users(id, role),
    FOREIGN KEY (offering_id, consultant_id) REFERENCES consultant_services(id, consultant_id),
    FOREIGN KEY (slot_id, consultant_id) REFERENCES availability_slots(id, consultant_id),
    CHECK (client_id <> consultant_id),
    CHECK (ends_at > starts_at AND created_at < starts_at),
    CHECK (expires_at > created_at AND expires_at <= created_at + INTERVAL '15 minutes' AND expires_at <= starts_at),
    CHECK (total_amount = base_amount + urgency_amount - discount_amount),
    UNIQUE (id, client_id, consultant_id, offering_id, slot_id)
);
CREATE INDEX quotes_client_created ON booking_quotes(client_id, created_at DESC, id);

CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id UUID NOT NULL UNIQUE,
    client_id UUID NOT NULL,
    consultant_id UUID NOT NULL,
    offering_id UUID NOT NULL,
    slot_id UUID NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'REQUESTED'
        CHECK (status IN ('REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED','REJECTED','CANCELLED','EXPIRED')),
    notes VARCHAR(2000) NOT NULL DEFAULT '',
    request_expires_at TIMESTAMPTZ NOT NULL,
    payment_due_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quote_id, client_id, consultant_id, offering_id, slot_id)
        REFERENCES booking_quotes(id, client_id, consultant_id, offering_id, slot_id),
    UNIQUE (id, client_id),
    CHECK (request_expires_at > created_at),
    CHECK (status NOT IN ('CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED') OR payment_due_at IS NOT NULL)
);
CREATE UNIQUE INDEX bookings_one_reservation_per_slot ON bookings(slot_id)
    WHERE status IN ('REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED');
CREATE INDEX bookings_client_created ON bookings(client_id, created_at DESC, id);
CREATE INDEX bookings_consultant_status ON bookings(consultant_id, status, created_at DESC, id);
CREATE INDEX bookings_request_expiry ON bookings(request_expires_at) WHERE status = 'REQUESTED';
CREATE INDEX bookings_payment_expiry ON bookings(payment_due_at) WHERE status = 'CONFIRMED';

CREATE TABLE booking_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id),
    actor_id UUID REFERENCES users(id), -- NULL means system worker
    action VARCHAR(40) NOT NULL CHECK (length(btrim(action)) > 0),
    from_status VARCHAR(24),
    to_status VARCHAR(24) NOT NULL CHECK (to_status IN ('REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED','REJECTED','CANCELLED','EXPIRED')),
    reason VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id, booking_id)
);
CREATE INDEX events_booking_created ON booking_events(booking_id, created_at, id);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL,
    client_id UUID NOT NULL,
    method VARCHAR(16) NOT NULL CHECK (method IN ('CREDIT_CARD','DEBIT_CARD','PAYPAL','BANK_TRANSFER')),
    provider VARCHAR(16) NOT NULL DEFAULT 'SANDBOX' CHECK (provider = 'SANDBOX'),
    status VARCHAR(16) NOT NULL CHECK (status IN ('SUCCEEDED','FAILED')),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    failure_code VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id, client_id) REFERENCES bookings(id, client_id),
    CHECK ((status = 'FAILED' AND failure_code IS NOT NULL) OR (status = 'SUCCEEDED' AND failure_code IS NULL)),
    UNIQUE (id, booking_id)
);
CREATE UNIQUE INDEX payments_one_success ON payments(booking_id) WHERE status = 'SUCCEEDED';
CREATE INDEX payments_client_created ON payments(client_id, created_at DESC, id);

CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    booking_id UUID NOT NULL,
    cancellation_event_id UUID NOT NULL UNIQUE,
    requested_by UUID NOT NULL REFERENCES users(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    status VARCHAR(16) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED','SUCCEEDED','FAILED')),
    failure_code VARCHAR(64),
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id, booking_id) REFERENCES payments(id, booking_id),
    FOREIGN KEY (cancellation_event_id, booking_id) REFERENCES booking_events(id, booking_id),
    CHECK ((status = 'FAILED' AND failure_code IS NOT NULL) OR (status <> 'FAILED' AND failure_code IS NULL))
);
CREATE INDEX refunds_booking_created ON refunds(booking_id, created_at DESC, id);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES users(id),
    booking_event_id UUID REFERENCES booking_events(id),
    type VARCHAR(40) NOT NULL,
    message VARCHAR(2000) NOT NULL CHECK (length(btrim(message)) > 0),
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (recipient_id, booking_event_id, type)
);
CREATE INDEX notifications_unread ON notifications(recipient_id, created_at DESC, id) WHERE read_at IS NULL;
CREATE INDEX notifications_recipient_created ON notifications(recipient_id, created_at DESC, id);

CREATE TABLE idempotency_records (
    actor_id UUID NOT NULL REFERENCES users(id),
    scope VARCHAR(160) NOT NULL,
    key VARCHAR(128) NOT NULL CHECK (length(key) >= 8),
    request_hash CHAR(64) NOT NULL CHECK (request_hash ~ '^[a-f0-9]{64}$'),
    state VARCHAR(16) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (state IN ('IN_PROGRESS','COMPLETED')),
    response_status SMALLINT CHECK (response_status BETWEEN 200 AND 499),
    response_body JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (actor_id, scope, key),
    CHECK (expires_at >= created_at + INTERVAL '7 days'),
    CHECK ((state = 'IN_PROGRESS' AND response_status IS NULL AND response_body IS NULL)
        OR (state = 'COMPLETED' AND response_status IS NOT NULL AND response_body IS NOT NULL))
);
CREATE INDEX idempotency_expiry ON idempotency_records(expires_at);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID REFERENCES users(id),
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    resource_id UUID,
    summary JSONB NOT NULL DEFAULT '{}'::jsonb CHECK (jsonb_typeof(summary) = 'object'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX audit_created ON audit_events(created_at DESC, id);
CREATE INDEX audit_resource ON audit_events(resource_type, resource_id, created_at);

-- Immutable financial snapshots and append-only history.
CREATE FUNCTION reject_mutation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION '% is append-only', TG_TABLE_NAME USING ERRCODE = '23514';
END;
$$;
CREATE TRIGGER policies_immutable BEFORE UPDATE OR DELETE ON policy_versions FOR EACH ROW EXECUTE FUNCTION reject_mutation();
CREATE TRIGGER quotes_immutable BEFORE UPDATE OR DELETE ON booking_quotes FOR EACH ROW EXECUTE FUNCTION reject_mutation();
CREATE TRIGGER payments_immutable BEFORE UPDATE OR DELETE ON payments FOR EACH ROW EXECUTE FUNCTION reject_mutation();
CREATE TRIGGER events_immutable BEFORE UPDATE OR DELETE ON booking_events FOR EACH ROW EXECUTE FUNCTION reject_mutation();
CREATE TRIGGER audit_immutable BEFORE UPDATE OR DELETE ON audit_events FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE FUNCTION guard_quote() RETURNS TRIGGER LANGUAGE plpgsql
SET search_path = consultlink_v1, pg_temp AS $$
DECLARE
    slot availability_slots%ROWTYPE;
    svc services%ROWTYPE;
    pol policy_versions%ROWTYPE;
    extra NUMERIC(12,2);
BEGIN
    SELECT * INTO STRICT slot FROM availability_slots WHERE id = NEW.slot_id;
    SELECT s.* INTO STRICT svc FROM services s JOIN consultant_services o ON o.service_id = s.id
        WHERE o.id = NEW.offering_id AND o.consultant_id = NEW.consultant_id AND o.active AND s.active;
    SELECT p.* INTO STRICT pol FROM policy_versions p JOIN platform_settings x ON x.current_policy_id = p.id
        WHERE p.id = NEW.policy_id;
    IF NOT EXISTS (SELECT 1 FROM consultant_profiles c JOIN users u ON u.id = c.user_id
        WHERE c.user_id = NEW.consultant_id AND c.approval_status = 'APPROVED' AND u.status = 'ACTIVE')
       OR NOT EXISTS (SELECT 1 FROM users WHERE id = NEW.client_id AND status = 'ACTIVE' AND role = 'CLIENT') THEN
        RAISE EXCEPTION 'quote participants are not eligible' USING ERRCODE = '23514';
    END IF;
    IF slot.status <> 'OPEN' OR slot.consultant_id <> NEW.consultant_id
        OR slot.starts_at <> NEW.starts_at OR slot.ends_at <> NEW.ends_at
        OR NEW.starts_at <= clock_timestamp()
        OR NEW.ends_at - NEW.starts_at <> make_interval(mins => svc.duration_minutes)
        OR NEW.service_name <> svc.name OR NEW.currency <> svc.currency OR NEW.base_amount <> svc.base_amount THEN
        RAISE EXCEPTION 'quote does not match an eligible session' USING ERRCODE = '23514';
    END IF;
    extra := CASE WHEN NEW.starts_at - NEW.created_at < make_interval(hours => pol.urgency_hours)
        THEN round(svc.base_amount * pol.urgency_percent / 100, 2) ELSE 0 END;
    IF NEW.urgency_amount <> extra OR NEW.discount_amount <> round(svc.base_amount * pol.discount_percent / 100, 2) THEN
        RAISE EXCEPTION 'quote price does not match policy' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER quotes_validate BEFORE INSERT ON booking_quotes FOR EACH ROW EXECUTE FUNCTION guard_quote();

CREATE FUNCTION guard_booking() RETURNS TRIGGER LANGUAGE plpgsql
SET search_path = consultlink_v1, pg_temp AS $$
DECLARE q booking_quotes%ROWTYPE;
BEGIN
    SELECT * INTO STRICT q FROM booking_quotes WHERE id = NEW.quote_id;
    IF TG_OP = 'INSERT' THEN
        -- Slot lock plus partial unique index serialize competing reservations.
        PERFORM 1 FROM availability_slots WHERE id = NEW.slot_id AND status = 'OPEN' FOR UPDATE;
        IF NOT FOUND OR NEW.status <> 'REQUESTED' OR q.expires_at <= clock_timestamp()
            OR q.starts_at <= clock_timestamp() OR NEW.request_expires_at > q.starts_at
            OR NOT EXISTS (SELECT 1 FROM consultant_services o JOIN services s ON s.id = o.service_id
                WHERE o.id = q.offering_id AND o.active AND s.active)
            OR NOT EXISTS (SELECT 1 FROM consultant_profiles c JOIN users u ON u.id = c.user_id
                WHERE c.user_id = q.consultant_id AND c.approval_status = 'APPROVED' AND u.status = 'ACTIVE')
            OR NOT EXISTS (SELECT 1 FROM users WHERE id = q.client_id AND status = 'ACTIVE') THEN
            RAISE EXCEPTION 'booking cannot reserve this quote/slot' USING ERRCODE = '23514';
        END IF;
    ELSE
        IF (NEW.quote_id, NEW.client_id, NEW.consultant_id, NEW.offering_id, NEW.slot_id, NEW.created_at, NEW.request_expires_at)
            IS DISTINCT FROM (OLD.quote_id, OLD.client_id, OLD.consultant_id, OLD.offering_id, OLD.slot_id, OLD.created_at, OLD.request_expires_at) THEN
            RAISE EXCEPTION 'booking context is immutable' USING ERRCODE = '23514';
        END IF;
        IF NEW.status <> OLD.status AND NOT (
            (OLD.status = 'REQUESTED' AND NEW.status IN ('CONFIRMED','REJECTED','CANCELLED','EXPIRED')) OR
            (OLD.status = 'CONFIRMED' AND NEW.status IN ('PENDING_PAYMENT','CANCELLED','EXPIRED')) OR
            (OLD.status = 'PENDING_PAYMENT' AND NEW.status IN ('PAID','CONFIRMED','CANCELLED')) OR
            (OLD.status = 'PAID' AND NEW.status IN ('COMPLETED','CANCELLED'))) THEN
            RAISE EXCEPTION 'invalid booking transition % -> %', OLD.status, NEW.status USING ERRCODE = '23514';
        END IF;
        IF NEW.status <> OLD.status AND NEW.status = 'CONFIRMED' AND OLD.status = 'REQUESTED'
            AND OLD.request_expires_at <= clock_timestamp() THEN
            RAISE EXCEPTION 'request has expired' USING ERRCODE = '23514';
        END IF;
        IF NEW.status = 'PENDING_PAYMENT' AND NEW.payment_due_at <= clock_timestamp() THEN
            RAISE EXCEPTION 'payment deadline has passed' USING ERRCODE = '23514';
        END IF;
        IF NEW.status <> OLD.status AND NEW.status = 'CANCELLED' AND q.starts_at <= clock_timestamp() THEN
            RAISE EXCEPTION 'session has already started' USING ERRCODE = '23514';
        END IF;
    END IF;
    IF NEW.payment_due_at IS NOT NULL AND (NEW.payment_due_at > q.starts_at OR NEW.payment_due_at <= NEW.created_at) THEN
        RAISE EXCEPTION 'invalid payment deadline' USING ERRCODE = '23514';
    END IF;
    IF NEW.status IN ('PAID','COMPLETED') AND NOT EXISTS
        (SELECT 1 FROM payments WHERE booking_id = NEW.id AND status = 'SUCCEEDED') THEN
        RAISE EXCEPTION 'successful payment required' USING ERRCODE = '23514';
    END IF;
    IF NEW.status = 'COMPLETED' AND q.ends_at > clock_timestamp() THEN
        RAISE EXCEPTION 'session has not ended' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER bookings_validate BEFORE INSERT OR UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION guard_booking();
CREATE TRIGGER bookings_no_delete BEFORE DELETE ON bookings FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE FUNCTION guard_slot_update() RETURNS TRIGGER LANGUAGE plpgsql
SET search_path = consultlink_v1, pg_temp AS $$
BEGIN
    IF (NEW.consultant_id, NEW.starts_at, NEW.ends_at) IS DISTINCT FROM (OLD.consultant_id, OLD.starts_at, OLD.ends_at)
        AND EXISTS (SELECT 1 FROM booking_quotes WHERE slot_id = OLD.id) THEN
        RAISE EXCEPTION 'quoted slot interval is immutable; create a replacement slot' USING ERRCODE = '23514';
    END IF;
    IF NEW.status <> 'OPEN' AND EXISTS (SELECT 1 FROM bookings WHERE slot_id = OLD.id
        AND status IN ('REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID')) THEN
        RAISE EXCEPTION 'reserved slot cannot be blocked or archived' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER slots_protect BEFORE UPDATE ON availability_slots FOR EACH ROW EXECUTE FUNCTION guard_slot_update();

CREATE FUNCTION guard_payment() RETURNS TRIGGER LANGUAGE plpgsql
SET search_path = consultlink_v1, pg_temp AS $$
DECLARE b bookings%ROWTYPE; q booking_quotes%ROWTYPE;
BEGIN
    SELECT * INTO STRICT b FROM bookings WHERE id = NEW.booking_id FOR UPDATE;
    SELECT * INTO STRICT q FROM booking_quotes WHERE id = b.quote_id;
    IF b.status <> 'PENDING_PAYMENT' OR b.payment_due_at <= clock_timestamp()
        OR NEW.amount <> q.total_amount OR NEW.currency <> q.currency OR NEW.client_id <> b.client_id THEN
        RAISE EXCEPTION 'payment does not match a payable booking' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER payments_validate BEFORE INSERT ON payments FOR EACH ROW EXECUTE FUNCTION guard_payment();

CREATE FUNCTION guard_refund() RETURNS TRIGGER LANGUAGE plpgsql
SET search_path = consultlink_v1, pg_temp AS $$
DECLARE paid payments%ROWTYPE; reserved NUMERIC(12,2);
BEGIN
    -- Lock parent first in application code too; this protects aggregate amount across concurrent refunds.
    PERFORM 1 FROM bookings WHERE id = NEW.booking_id FOR UPDATE;
    SELECT * INTO STRICT paid FROM payments WHERE id = NEW.payment_id FOR UPDATE;
    IF TG_OP = 'UPDATE' AND ((NEW.payment_id, NEW.booking_id, NEW.cancellation_event_id, NEW.requested_by, NEW.amount, NEW.currency)
        IS DISTINCT FROM (OLD.payment_id, OLD.booking_id, OLD.cancellation_event_id, OLD.requested_by, OLD.amount, OLD.currency)
        OR OLD.status = 'SUCCEEDED' AND NEW IS DISTINCT FROM OLD) THEN
        RAISE EXCEPTION 'refund financial context/success is immutable' USING ERRCODE = '23514';
    END IF;
    SELECT coalesce(sum(amount),0) INTO reserved FROM refunds
        WHERE payment_id = NEW.payment_id AND id <> NEW.id;
    IF paid.status <> 'SUCCEEDED' OR NEW.currency <> paid.currency OR NEW.amount + reserved > paid.amount
        OR NOT EXISTS (SELECT 1 FROM booking_events WHERE id = NEW.cancellation_event_id
            AND booking_id = NEW.booking_id AND action = 'CANCEL' AND to_status = 'CANCELLED') THEN
        RAISE EXCEPTION 'refund exceeds/mismatches successful payment or cancellation' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER refunds_validate BEFORE INSERT OR UPDATE ON refunds FOR EACH ROW EXECUTE FUNCTION guard_refund();
CREATE TRIGGER refunds_no_delete BEFORE DELETE ON refunds FOR EACH ROW EXECUTE FUNCTION reject_mutation();

-- Synchronous sandbox payment may pass through PENDING_PAYMENT inside a transaction,
-- but cannot commit an orphan successful payment or an unfinished intermediate state.
CREATE FUNCTION check_payment_consistency() RETURNS TRIGGER LANGUAGE plpgsql
SET search_path = consultlink_v1, pg_temp AS $$
DECLARE booking_key UUID; current_status TEXT; has_payment BOOLEAN;
BEGIN
    IF TG_TABLE_NAME = 'payments' THEN booking_key := NEW.booking_id;
    ELSE booking_key := NEW.id; END IF;
    SELECT status INTO current_status FROM bookings WHERE id = booking_key;
    SELECT EXISTS (SELECT 1 FROM payments WHERE booking_id = booking_key AND status = 'SUCCEEDED') INTO has_payment;
    IF current_status = 'PENDING_PAYMENT'
        OR has_payment AND current_status NOT IN ('PAID','COMPLETED','CANCELLED')
        OR current_status IN ('PAID','COMPLETED') AND NOT has_payment THEN
        RAISE EXCEPTION 'booking and payment must commit consistently' USING ERRCODE = '23514';
    END IF;
    RETURN NULL;
END;
$$;
CREATE CONSTRAINT TRIGGER booking_payment_consistent AFTER INSERT OR UPDATE ON bookings
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_payment_consistency();
CREATE CONSTRAINT TRIGGER payment_booking_consistent AFTER INSERT ON payments
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_payment_consistency();
COMMIT;
