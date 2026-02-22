-- Add filing_state for ML
alter table refund_status_event add column filing_state varchar(2);
create index ix_rse_state on refund_status_event(filing_state);