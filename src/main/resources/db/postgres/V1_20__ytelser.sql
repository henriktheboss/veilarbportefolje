CREATE TABLE YTELSE_STATUS_FOR_BRUKER
(
    AKTOERID            VARCHAR(20) NOT NULL,
    UTLOPSDATO          TIMESTAMP,
    DAGPUTLOPUKE        integer,
    PERMUTLOPUKE        integer,
    AAPMAXTIDUKE        integer,
    AAPUNNTAKDAGERIGJEN integer,
    PRIMARY KEY (AKTOERID)
);