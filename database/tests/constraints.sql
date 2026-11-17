-- Run after V1 in a disposable PostgreSQL database. All fixture data rolls back.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path = consultlink_v1, public;
CREATE TEMP TABLE test_results (label TEXT PRIMARY KEY);
CREATE FUNCTION pg_temp.must_fail(command TEXT, expected_state TEXT, label TEXT) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    BEGIN
        EXECUTE command;
        RAISE EXCEPTION 'Expected failure but statement succeeded: %', label;
    EXCEPTION WHEN OTHERS THEN
        IF SQLSTATE <> expected_state THEN
            RAISE EXCEPTION 'Unexpected SQLSTATE % (expected %) for %: %', SQLSTATE, expected_state, label, SQLERRM;
        END IF;
    END;
    INSERT INTO test_results VALUES (label);
END;
$$;

DO $$
DECLARE
    admin_id UUID := gen_random_uuid();
    client1 UUID := gen_random_uuid();
    client2 UUID := gen_random_uuid();
    consultant1 UUID := gen_random_uuid();
    consultant2 UUID := gen_random_uuid();
    svc UUID := gen_random_uuid();
    offering UUID := gen_random_uuid();
    pol UUID := gen_random_uuid();
    slot1 UUID := gen_random_uuid();
    slot2 UUID := gen_random_uuid();
    quote1 UUID := gen_random_uuid();
    quote2 UUID := gen_random_uuid();
    booking1 UUID := gen_random_uuid();
    payment1 UUID := gen_random_uuid();
    event1 UUID := gen_random_uuid();
    refund1 UUID := gen_random_uuid();
    t TIMESTAMPTZ := CURRENT_TIMESTAMP + INTERVAL '2 days';
    demo_hash TEXT := '$2a$10$' || repeat('a',53); -- syntactic fixture only, not a usable seeded credential
BEGIN
    INSERT INTO users(id,login_name,email,display_name,password_hash,role) VALUES
        (admin_id,'test_admin','admin@example.invalid','Admin',demo_hash,'ADMIN'),
        (client1,'test_client1','client1@example.invalid','Client 1',demo_hash,'CLIENT'),
        (client2,'test_client2','client2@example.invalid','Client 2',demo_hash,'CLIENT'),
        (consultant1,'test_consultant1','consultant1@example.invalid','Consultant 1',demo_hash,'CONSULTANT'),
        (consultant2,'test_consultant2','consultant2@example.invalid','Consultant 2',demo_hash,'CONSULTANT');
    PERFORM pg_temp.must_fail(format('INSERT INTO users(login_name,email,display_name,password_hash,role) VALUES (%L,%L,%L,%L,%L)',
        'TEST_CLIENT1','other@example.invalid','Duplicate',demo_hash,'CLIENT'),'23505','case-insensitive login uniqueness');
    PERFORM pg_temp.must_fail(format('INSERT INTO consultant_profiles(user_id) VALUES (%L)',client1),'23503','consultant profile requires consultant role');
    INSERT INTO consultant_profiles(user_id,approval_status,reviewed_by,reviewed_at) VALUES
        (consultant1,'APPROVED',admin_id,CURRENT_TIMESTAMP),(consultant2,'APPROVED',admin_id,CURRENT_TIMESTAMP);
    INSERT INTO services(id,name,duration_minutes,base_amount) VALUES (svc,'Consultation',60,100.00);
    INSERT INTO consultant_services(id,consultant_id,service_id) VALUES (offering,consultant1,svc);
    INSERT INTO policy_versions(id,version_number,created_by,change_reason) VALUES (pol,1,admin_id,'Constraint test');
    INSERT INTO platform_settings VALUES (1,pol,0);
    INSERT INTO availability_slots(id,consultant_id,starts_at,ends_at) VALUES
        (slot1,consultant1,t,t + INTERVAL '1 hour'),
        (slot2,consultant1,t + INTERVAL '1 hour',t + INTERVAL '2 hours');
    INSERT INTO test_results VALUES ('adjacent slots accepted');
    INSERT INTO availability_slots(consultant_id,starts_at,ends_at) VALUES (consultant2,t,t + INTERVAL '1 hour');
    INSERT INTO test_results VALUES ('different consultants may overlap');
    PERFORM pg_temp.must_fail(format('INSERT INTO availability_slots(consultant_id,starts_at,ends_at) VALUES (%L,%L,%L)',
        consultant1,t + INTERVAL '30 minutes',t + INTERVAL '90 minutes'),'23P01','same consultant cannot overlap');
    PERFORM pg_temp.must_fail(format('INSERT INTO availability_slots(consultant_id,starts_at,ends_at) VALUES (%L,%L,%L)',
        consultant1,t + INTERVAL '3 hours',t + INTERVAL '2 hours'),'23514','slot must end after start');
    INSERT INTO booking_quotes(id,client_id,consultant_id,offering_id,slot_id,policy_id,service_name,starts_at,ends_at,
        base_amount,urgency_amount,discount_amount,total_amount,currency,expires_at) VALUES
        (quote1,client1,consultant1,offering,slot1,pol,'Consultation',t,t + INTERVAL '1 hour',100,0,0,100,'CAD',CURRENT_TIMESTAMP + INTERVAL '15 minutes'),
        (quote2,client2,consultant1,offering,slot1,pol,'Consultation',t,t + INTERVAL '1 hour',100,0,0,100,'CAD',CURRENT_TIMESTAMP + INTERVAL '15 minutes');
    PERFORM pg_temp.must_fail(format('UPDATE booking_quotes SET total_amount = 1 WHERE id = %L',quote1),'23514','quote financial snapshot immutable');
    PERFORM pg_temp.must_fail(format('UPDATE policy_versions SET late_refund_percent = 0 WHERE id = %L',pol),'23514','policy version immutable');
    PERFORM pg_temp.must_fail(format('UPDATE availability_slots SET starts_at = starts_at - interval ''1 minute'' WHERE id = %L',slot1),'23514','quoted slot interval immutable');
    INSERT INTO bookings(id,quote_id,client_id,consultant_id,offering_id,slot_id,request_expires_at)
        VALUES (booking1,quote1,client1,consultant1,offering,slot1,CURRENT_TIMESTAMP + INTERVAL '24 hours');
    PERFORM pg_temp.must_fail(format('INSERT INTO bookings(quote_id,client_id,consultant_id,offering_id,slot_id,request_expires_at) VALUES (%L,%L,%L,%L,%L,CURRENT_TIMESTAMP + interval ''24 hours'')',
        quote2,client2,consultant1,offering,slot1),'23505','only one reserving booking per slot');
    PERFORM pg_temp.must_fail(format('UPDATE bookings SET client_id = %L WHERE id = %L',client2,booking1),'23514','booking owner immutable');
    PERFORM pg_temp.must_fail(format('UPDATE bookings SET status = ''COMPLETED'' WHERE id = %L',booking1),'23514','cannot skip booking states');
    PERFORM pg_temp.must_fail(format('UPDATE availability_slots SET status = ''BLOCKED'' WHERE id = %L',slot1),'23514','reserved slot cannot be blocked');
    UPDATE bookings SET status = 'CONFIRMED',payment_due_at = CURRENT_TIMESTAMP + INTERVAL '30 minutes' WHERE id = booking1;
    UPDATE bookings SET status = 'PENDING_PAYMENT' WHERE id = booking1;
    PERFORM pg_temp.must_fail(format('UPDATE bookings SET status = ''PAID'' WHERE id = %L',booking1),'23514','paid state requires successful payment');
    PERFORM pg_temp.must_fail(format('INSERT INTO payments(booking_id,client_id,method,status,amount,currency) VALUES (%L,%L,''CREDIT_CARD'',''SUCCEEDED'',1,''CAD'')',
        booking1,client1),'23514','payment amount must match frozen quote');
    INSERT INTO payments(id,booking_id,client_id,method,status,amount,currency)
        VALUES (payment1,booking1,client1,'CREDIT_CARD','SUCCEEDED',100,'CAD');
    PERFORM pg_temp.must_fail(format('INSERT INTO payments(booking_id,client_id,method,status,amount,currency) VALUES (%L,%L,''PAYPAL'',''SUCCEEDED'',100,''CAD'')',
        booking1,client1),'23505','only one successful payment per booking');
    UPDATE bookings SET status = 'PAID' WHERE id = booking1;
    PERFORM pg_temp.must_fail(format('UPDATE payments SET amount = 1 WHERE id = %L',payment1),'23514','payment ledger immutable');
    PERFORM pg_temp.must_fail(format('UPDATE bookings SET status = ''COMPLETED'' WHERE id = %L',booking1),'23514','cannot complete before session ends');
    INSERT INTO booking_events(id,booking_id,actor_id,action,from_status,to_status,reason)
        VALUES(event1,booking1,client1,'CANCEL','PAID','CANCELLED','Test cancellation');
    PERFORM pg_temp.must_fail(format('INSERT INTO refunds(payment_id,booking_id,cancellation_event_id,requested_by,amount,currency) VALUES (%L,%L,%L,%L,101,''CAD'')',
        payment1,booking1,event1,client1),'23514','refund cannot exceed payment');
    INSERT INTO refunds(id,payment_id,booking_id,cancellation_event_id,requested_by,amount,currency)
        VALUES(refund1,payment1,booking1,event1,client1,100,'CAD');
    UPDATE refunds SET status = 'SUCCEEDED',attempt_count = 1 WHERE id = refund1;
    PERFORM pg_temp.must_fail(format('UPDATE refunds SET status = ''FAILED'',failure_code = ''DECLINED'' WHERE id = %L',refund1),'23514','successful refund cannot be reversed');
    UPDATE bookings SET status = 'CANCELLED' WHERE id = booking1;
    INSERT INTO bookings(quote_id,client_id,consultant_id,offering_id,slot_id,request_expires_at)
        VALUES (quote2,client2,consultant1,offering,slot1,CURRENT_TIMESTAMP + INTERVAL '24 hours');
    INSERT INTO test_results VALUES ('cancellation releases slot for another client');
    PERFORM pg_temp.must_fail(format('UPDATE bookings SET status = ''REQUESTED'' WHERE id = %L',booking1),'23514','terminal booking cannot reopen');
    PERFORM pg_temp.must_fail(format('DELETE FROM booking_events WHERE id = %L',event1),'23514','booking history append-only');
    PERFORM pg_temp.must_fail(format('DELETE FROM bookings WHERE id = %L',booking1),'23514','bookings cannot be deleted');
    INSERT INTO idempotency_records(actor_id,scope,key,request_hash,expires_at)
        VALUES(client1,'booking:create','test-key-001',repeat('a',64),CURRENT_TIMESTAMP + INTERVAL '7 days');
    PERFORM pg_temp.must_fail(format('INSERT INTO idempotency_records(actor_id,scope,key,request_hash,expires_at) VALUES (%L,''booking:create'',''test-key-001'',%L,CURRENT_TIMESTAMP + interval ''7 days'')',
        client1,repeat('a',64)),'23505','idempotency actor/scope/key unique');
    UPDATE idempotency_records SET state='COMPLETED',response_status=201,response_body='{}'::jsonb
        WHERE actor_id=client1 AND scope='booking:create' AND key='test-key-001';
    INSERT INTO test_results VALUES ('idempotency claim can complete with persisted response');
END;
$$;
SET CONSTRAINTS ALL IMMEDIATE;
SELECT label AS passed FROM test_results ORDER BY label;
SELECT count(*) AS passed_count FROM test_results;
ROLLBACK;
