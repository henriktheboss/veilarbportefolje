CREATE VIEW VW_PORTEFOLJE_INFO AS (
SELECT
  MAP.AKTOERID AS AKTOERID,
  OB.PERSON_ID AS PERSON_ID,  
  OB.FODSELSNR AS FODSELSNR,
  OB.FORNAVN AS FORNAVN,  
  OB.ETTERNAVN AS ETTERNAVN,  
  OB.NAV_KONTOR AS NAV_KONTOR,  
  OB.FORMIDLINGSGRUPPEKODE AS FORMIDLINGSGRUPPEKODE,  
  OB.ISERV_FRA_DATO AS ISERV_FRA_DATO,  
  OB.KVALIFISERINGSGRUPPEKODE AS KVALIFISERINGSGRUPPEKODE,  
  OB.RETTIGHETSGRUPPEKODE AS RETTIGHETSGRUPPEKODE,  
  OB.HOVEDMAALKODE AS HOVEDMAALKODE,  
  OB.SIKKERHETSTILTAK_TYPE_KODE AS SIKKERHETSTILTAK_TYPE_KODE,  
  OB.FR_KODE AS FR_KODE,  
  OB.SPERRET_ANSATT AS SPERRET_ANSATT,  
  OB.ER_DOED AS ER_DOED,  
  OB.DOED_FRA_DATO AS DOED_FRA_DATO,  
  VW_OD.VEILEDERIDENT AS VEILEDERIDENT,  
  VW_OD.NY_FOR_VEILEDER AS NY_FOR_VEILEDER,  
  VW_OD.OPPFOLGING AS OPPFOLGING,  
  DIA.VENTER_PA_BRUKER AS VENTERPASVARFRABRUKER,  
  DIA.VENTER_PA_NAV AS VENTERPASVARFRANAV,  
  BD.YTELSE AS YTELSE,  
  BD.UTLOPSDATO AS UTLOPSDATO,  
  BD.UTLOPSDATOFASETT AS UTLOPSDATOFASETT,  
  BD.DAGPUTLOPUKE AS DAGPUTLOPUKE, 
  BD.DAGPUTLOPUKEFASETT AS DAGPUTLOPUKEFASETT,  
  BD.PERMUTLOPUKE AS PERMUTLOPUKE, 
  BD.PERMUTLOPUKEFASETT AS PERMUTLOPUKEFASETT,  
  BD.AAPMAXTIDUKE AS AAPMAXTIDUKE, 
  BD.AAPMAXTIDUKEFASETT AS AAPMAXTIDUKEFASETT,  
  BD.AAPUNNTAKDAGERIGJEN AS AAPUNNTAKDAGERIGJEN, 
  BD.AAPUNNTAKUKERIGJENFASETT AS AAPUNNTAKUKERIGJENFASETT,  
  BD.NYESTEUTLOPTEAKTIVITET AS NYESTEUTLOPTEAKTIVITET,  
  BD.AKTIVITET_START AS AKTIVITET_START,  
  BD.NESTE_AKTIVITET_START AS NESTE_AKTIVITET_START,  
  BD.FORRIGE_AKTIVITET_START AS FORRIGE_AKTIVITET_START,
  OB.KILDE AS KILDE,
  OB.TIDSSTEMPEL AS OPPDATERT_ARENA,  
  VW_OD.OPPDATERT_PORTEFOLJE AS OPPDATERT_BRUKERDATA,
  DIA.OPPDATERT AS OPPDATERT_DIALOG,
  OB.TIDSSTEMPEL AS TIDSSTEMPEL -- FOR BAKOVERKOMPATIBILITET. ERSTATTES AV "OPPDATERT_ARENA"
FROM
  VW_OPPFOLGINGSBRUKER OB 
LEFT JOIN VW_AKTOERID_TO_PERSONID MAP ON MAP.PERSONID = OB.PERSON_ID 
LEFT JOIN VW_OPPFOLGING_DATA VW_OD ON VW_OD.AKTOERID = MAP.AKTOERID
LEFT JOIN VW_DIALOG DIA ON DIA.AKTOERID = MAP.AKTOERID
LEFT JOIN BRUKER_DATA BD ON BD.PERSONID = OB.PERSON_ID
); 

-- TO REMOVE CHANGES:
--
-- DROP VIEW VW_PORTEFOLJE_INFO;
