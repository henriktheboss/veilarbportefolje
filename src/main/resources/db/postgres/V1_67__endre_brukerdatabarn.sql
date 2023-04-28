ALTER TABLE bruker_data_barn
    ADD COLUMN BARN_PERSONIDENT VARCHAR(30) NOT NULL;
ALTER TABLE bruker_data_barn
    DROP COLUMN id;
ALTER TABLE bruker_data_barn
    ADD PRIMARY KEY (BARN_PERSONIDENT, foresatt_ident);
