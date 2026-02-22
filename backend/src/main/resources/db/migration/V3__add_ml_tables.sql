-- Refund status events (immutable audit + training)
create table refund_status_event (
  id bigserial primary key,
  user_id bigint not null references app_user(id) on delete cascade,
  tax_year int not null,
  from_status varchar(40),
  to_status varchar(40) not null,
  expected_amount numeric(18,2),
  irs_tracking_id varchar(100),
  source varchar(40) not null,              -- IRS|SIMULATION|BACKFILL
  occurred_at timestamptz not null default now()
);

create index ix_rse_user_year_time on refund_status_event(user_id, tax_year, occurred_at desc);
create index ix_rse_year_time on refund_status_event(tax_year, occurred_at desc);

-- Persisted ETA predictions (read model output)
create table refund_eta_prediction (
  id bigserial primary key,
  user_id bigint not null references app_user(id) on delete cascade,
  tax_year int not null,
  status varchar(40) not null,
  eta_days int not null,
  estimated_available_at timestamptz,
  model_name varchar(120) not null,
  model_version varchar(120) not null,
  features jsonb not null,
  created_at timestamptz not null default now(),
  unique(user_id, tax_year, status, model_version)
);

create index ix_eta_user_year_time on refund_eta_prediction(user_id, tax_year, created_at desc);

-- Transactional outbox (drives async compute)
create table outbox_event (
  id bigserial primary key,
  event_type varchar(80) not null,          -- REFUND_STATUS_UPDATED
  aggregate_key varchar(120) not null,      -- userId:taxYear
  payload jsonb not null,
  created_at timestamptz not null default now(),
  processed_at timestamptz,
  attempts int not null default 0,
  last_error text
);

create index ix_outbox_unprocessed on outbox_event(processed_at) where processed_at is null;
create index ix_outbox_created on outbox_event(created_at desc);