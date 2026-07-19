-- Служители, абонамент за услуги, затваряне на обект по ден, time-off + заявки.

CREATE TABLE staff_members (
    id            BIGSERIAL PRIMARY KEY,
    salon_id      BIGINT       NOT NULL REFERENCES salons (id) ON DELETE CASCADE,
    user_id       BIGINT       NOT NULL,
    display_name  VARCHAR(200) NOT NULL,
    title         VARCHAR(120),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_staff_salon_user UNIQUE (salon_id, user_id)
);

CREATE INDEX idx_staff_members_salon ON staff_members (salon_id);
CREATE INDEX idx_staff_members_user ON staff_members (user_id);

CREATE TABLE staff_services (
    staff_id   BIGINT NOT NULL REFERENCES staff_members (id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES salon_services (id) ON DELETE CASCADE,
    PRIMARY KEY (staff_id, service_id)
);

CREATE INDEX idx_staff_services_service ON staff_services (service_id);

-- Уникален график на служител (salon-level вече е в V8 partial index).
CREATE UNIQUE INDEX uq_working_hours_staff_day
    ON working_hours (salon_id, staff_id, day_of_week)
    WHERE staff_id IS NOT NULL;

CREATE TABLE salon_closures (
    id          BIGSERIAL PRIMARY KEY,
    salon_id    BIGINT      NOT NULL REFERENCES salons (id) ON DELETE CASCADE,
    closed_on   DATE        NOT NULL,
    reason      VARCHAR(500),
    created_by  BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_salon_closures_day UNIQUE (salon_id, closed_on)
);

CREATE INDEX idx_salon_closures_salon ON salon_closures (salon_id);

CREATE TABLE time_off (
    id          BIGSERIAL PRIMARY KEY,
    staff_id    BIGINT      NOT NULL REFERENCES staff_members (id) ON DELETE CASCADE,
    starts_at   TIMESTAMPTZ NOT NULL,
    ends_at     TIMESTAMPTZ NOT NULL,
    reason      VARCHAR(500),
    source      VARCHAR(20) NOT NULL CHECK (source IN ('OWNER', 'REQUEST')),
    created_by  BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_time_off_range CHECK (ends_at > starts_at)
);

CREATE INDEX idx_time_off_staff_starts ON time_off (staff_id, starts_at);

CREATE TABLE time_off_requests (
    id           BIGSERIAL PRIMARY KEY,
    staff_id     BIGINT      NOT NULL REFERENCES staff_members (id) ON DELETE CASCADE,
    starts_at    TIMESTAMPTZ NOT NULL,
    ends_at      TIMESTAMPTZ NOT NULL,
    reason       VARCHAR(500),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                 CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_by  BIGINT,
    reviewed_at  TIMESTAMPTZ,
    CONSTRAINT chk_time_off_requests_range CHECK (ends_at > starts_at)
);

CREATE INDEX idx_time_off_requests_staff ON time_off_requests (staff_id, status);
