ALTER TABLE BRUKER_DATA
    ADD bydelsnummer VARCHAR(10);
ALTER TABLE BRUKER_DATA
    ADD kommunenummer VARCHAR(10);
ALTER TABLE BRUKER_DATA
    ADD utenlandskAdresse VARCHAR(10);
ALTER TABLE BRUKER_DATA
    ADD COLUMN bostedSistOppdatert DATE;