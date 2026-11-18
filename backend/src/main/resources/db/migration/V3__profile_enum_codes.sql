-- ============================================================================
-- V3__profile_enum_codes.sql
-- V1 used codes like '22_23' and '1_2', which are not valid Java identifiers,
-- so they cannot be enum constant names with EnumType.STRING.
-- Renaming the three affected sets to valid, still-readable identifiers.
-- V1 is left untouched: applied migrations are never edited.
-- ============================================================================

ALTER TABLE profiles DROP CHECK ck_prof_sleep;
ALTER TABLE profiles ADD CONSTRAINT ck_prof_sleep
    CHECK (sleep_time IN ('BEFORE_2200', 'H2200_2300', 'H2300_0000', 'AFTER_MIDNIGHT'));

ALTER TABLE profiles DROP CHECK ck_prof_wake;
ALTER TABLE profiles ADD CONSTRAINT ck_prof_wake
    CHECK (wake_time IN ('BEFORE_0600', 'H0600_0800', 'H0800_1000', 'AFTER_1000'));

ALTER TABLE profiles DROP CHECK ck_prof_wfh;
ALTER TABLE profiles ADD CONSTRAINT ck_prof_wfh
    CHECK (work_from_home IN ('NEVER', 'ONE_TWO_DAYS', 'THREE_FOUR_DAYS', 'ALWAYS'));
