CREATE TABLE INDEKSERING_LOGG
(
  SIST_INDEKSERT TIMESTAMP NOT NULL
);

CREATE MATERIALIZED VIEW OPPFOLGINGSBRUKER
BUILD IMMEDIATE
REFRESH FAST
ON DEMAND NEXT sysdate + 60/86400
AS
  SELECT *
  FROM OPPFOLGINGSBRUKER@aret04;


CREATE MATERIALIZED VIEW SIKKERHETSTILTAK_TYPE
BUILD IMMEDIATE
REFRESH FAST
ON DEMAND NEXT sysdate + 60/86400
AS
  SELECT *
  FROM SIKKERHETSTILTAK_TYPE@aret04;


CREATE MATERIALIZED VIEW HOVEDMAAL
BUILD IMMEDIATE
REFRESH FAST
ON DEMAND NEXT sysdate + 60/86400
AS
  SELECT *
  FROM HOVEDMAAL@aret04;

CREATE MATERIALIZED VIEW RETTIGHETSGRUPPETYPE
BUILD IMMEDIATE
REFRESH FAST
ON DEMAND NEXT sysdate + 60/86400
AS
  SELECT *
  FROM RETTIGHETSGRUPPETYPE@aret04;


CREATE MATERIALIZED VIEW KVALIFISERINGSGRUPPETYPE
BUILD IMMEDIATE
REFRESH FAST
ON DEMAND NEXT sysdate + 60/86400
AS
  SELECT *
  FROM KVALIFISERINGSGRUPPETYPE@aret04;

CREATE MATERIALIZED VIEW FORMIDLINGSGRUPPETYPE
BUILD IMMEDIATE
REFRESH FAST
ON DEMAND NEXT sysdate + 60/86400
AS
  SELECT *
  FROM FORMIDLINGSGRUPPETYPE@aret04;