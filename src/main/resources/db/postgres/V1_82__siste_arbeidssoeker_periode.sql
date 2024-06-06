CREATE TABLE SISTE_ARBEIDSSOEKER_PERIODE
(
    ARBEIDSSOKER_PERIODE_ID UUID UNIQUE,
    FNR                     VARCHAR(11) NOT NULL,

    PRIMARY KEY (FNR)
);

ALTER TABLE OPPLYSNINGER_OM_ARBEIDSSOEKER
    ADD CONSTRAINT fk_arbeidssoeker_periode_id FOREIGN KEY (PERIODE_ID) REFERENCES SISTE_ARBEIDSSOEKER_PERIODE (ARBEIDSSOKER_PERIODE_ID) ON
        DELETE CASCADE;