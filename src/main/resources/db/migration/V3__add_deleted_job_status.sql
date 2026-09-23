ALTER TABLE jobs
DROP CONSTRAINT chk_jobs_status;

ALTER TABLE jobs
ADD CONSTRAINT chk_jobs_status
CHECK (status IN ('ACTIVE', 'PAUSED', 'DELETED'));