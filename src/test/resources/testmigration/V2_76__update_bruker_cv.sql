ALTER TABLE BRUKER_CV DROP COLUMN CV_EKSISTERE;

ALTER TABLE BRUKER_CV
    ADD CV_EKSISTERE CHAR(1) default 'N';