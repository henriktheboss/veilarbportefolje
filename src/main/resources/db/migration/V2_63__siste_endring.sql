CREATE TABLE SISTE_ENDRING (
    AKTOERID VARCHAR(20) NOT NULL,
    MAL TIMESTAMP,
    NY_STILLING TIMESTAMP,
    NY_IJOBB TIMESTAMP,
    NY_EGEN TIMESTAMP,
    NY_BEHANDLING TIMESTAMP,

    FULLFORT_STILLING TIMESTAMP,
    FULLFORT_IJOBB TIMESTAMP,
    FULLFORT_EGEN TIMESTAMP,
    FULLFORT_BEHANDLING TIMESTAMP,

    AVBRUTT_STILLING TIMESTAMP,
    AVBRUTT_IJOBB TIMESTAMP,
    AVBRUTT_EGEN TIMESTAMP,
    AVBRUTT_BEHANDLING TIMESTAMP,
    PRIMARY KEY (AKTOERID));