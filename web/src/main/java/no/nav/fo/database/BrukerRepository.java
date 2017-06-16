package no.nav.fo.database;

import javaslang.Tuple;
import javaslang.Tuple2;
import no.nav.fo.domene.*;
import no.nav.fo.domene.Aktivitet.AktivitetDTO;
import no.nav.fo.domene.Aktivitet.AktivitetData;
import no.nav.fo.domene.Aktivitet.AktivitetTyper;
import no.nav.fo.domene.feed.AktivitetDataFraFeed;
import no.nav.fo.util.sql.SqlUtils;
import no.nav.fo.util.sql.UpsertQuery;
import no.nav.fo.util.sql.where.WhereClause;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static no.nav.fo.util.AktivitetUtils.applyAktivitetStatuser;
import static no.nav.fo.util.DateUtils.timestampFromISO8601;
import static no.nav.fo.util.DbUtils.mapResultSetTilDokument;
import static no.nav.fo.util.DbUtils.parseJaNei;
import static no.nav.fo.util.MetricsUtils.timed;
import static no.nav.fo.util.StreamUtils.batchProcess;
import static org.slf4j.LoggerFactory.getLogger;

public class BrukerRepository {

    static final private Logger LOG = getLogger(BrukerRepository.class);
    private static final String IARBS = "IARBS";

    @Inject
    private JdbcTemplate db;

    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void prosesserBrukere(Predicate<SolrInputDocument> filter, Consumer<SolrInputDocument> prosess) {
        prosesserBrukere(10000, filter, prosess);
    }

    public void prosesserBrukere(int fetchSize, Predicate<SolrInputDocument> filter, Consumer<SolrInputDocument> prosess) {
        db.setFetchSize(fetchSize);
        db.query(retrieveBrukereSQL(), rs -> {
            SolrInputDocument bruker = mapResultSetTilDokument(rs);
            if (filter.test(bruker)) {
                applyAktivitetStatuser(bruker, this);
                prosess.accept(bruker);
            }
        });
    }

    public List<SolrInputDocument> retrieveOppdaterteBrukere() {
        List<SolrInputDocument> brukere = new ArrayList<>();
        db.setFetchSize(10000);
        db.query(retrieveOppdaterteBrukereSQL(), rs -> {
            brukere.add(mapResultSetTilDokument(rs));
        });
        return brukere.stream().filter(BrukerRepository::erOppfolgingsBruker).collect(toList());
    }

    public List<Map<String, Object>> retrieveBrukermedBrukerdata(String personId) {
        return db.queryForList(retrieveBrukerMedBrukerdataSQL(), personId);
    }

    public List<Brukerdata> retrieveBrukerdata(List<String> personIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("fnrs", personIds);
        return namedParameterJdbcTemplate.queryForList(retrieveBrukerdataSQL(), params)
                .stream()
                .map(data -> new Brukerdata()
                        .setAktoerid((String) data.get("AKTOERID"))
                        .setVeileder((String) data.get("VEILEDERIDENT"))
                        .setPersonid((String) data.get("PERSONID"))
                        .setTildeltTidspunkt((Timestamp) data.get("TILDELT_TIDSPUNKT"))
                        .setUtlopsdato(toLocalDateTime((Timestamp) data.get("UTLOPSDATO")))
                        .setYtelse(ytelsemappingOrNull((String) data.get("YTELSE")))
                        .setAapMaxtid(toLocalDateTime((Timestamp) data.get("AAPMAXTID")))
                        .setAapMaxtidFasett(kvartalmappingOrNull((String) data.get("AAPMAXTIDFASETT")))
                        .setUtlopsdatoFasett(manedmappingOrNull((String) data.get("UTLOPSDATOFASETT")))
                        .setOppfolging(parseJaNei((String) data.get("OPPFOLGING"), "OPPFOLGING")))
                .collect(toList());
    }

    public int updateTidsstempel(Timestamp tidsstempel) {
        return db.update(updateTidsstempelSQL(), tidsstempel);
    }

    public java.util.List<Map<String, Object>> retrieveBruker(String aktoerId) {
        return db.queryForList(retrieveBrukerSQL(), aktoerId);
    }

    public java.util.List<Map<String, Object>> retrievePersonid(String aktoerId) {
        return db.queryForList(getPersonidFromAktoeridSQL(), aktoerId);
    }

    public Optional<String> retrievePersonIdFromAktoerId(String aktoerId) {
        List<Map<String, Object>> list = retrieveBruker(aktoerId);
        if (list.size() != 1) {
            LOG.warn(format("Fikk %d antall rader for bruker med aktoerId %s", list.size(), aktoerId));
            return empty();
        }
        return Optional.of((String)list.get(0).get("PERSON_ID"));
    }

    public Optional<BigDecimal> retrievePersonidFromFnr(String fnr) {
        List<Map<String, Object>> list = db.queryForList(getPersonIdFromFnrSQL(), fnr);
        if (list.size() != 1) {
            LOG.warn(format("Fikk %d antall rader for bruker med fnr %s", list.size(), fnr));
            return empty();
        }
        BigDecimal personId = (BigDecimal) list.get(0).get("PERSON_ID");
        return Optional.ofNullable(personId);
    }

    public Map<String, Optional<String>> retrievePersonidFromFnrs(Collection<String> fnrs) {
        Map<String, Optional<String>> brukere = new HashMap<>(fnrs.size());

        batchProcess(1000, fnrs, timed("GR199.brukersjekk.batch", (fnrBatch) -> {
            Map<String, Object> params = new HashMap<>();
            params.put("fnrs", fnrBatch);

            Map<String, Optional<String>> fnrPersonIdMap = namedParameterJdbcTemplate.queryForList(
                    getPersonIdsFromFnrsSQL(),
                    params)
                    .stream()
                    .map((rs) -> Tuple.of(
                            (String) rs.get("FODSELSNR"),
                            rs.get("PERSON_ID").toString())
                    )
                    .collect(Collectors.toMap(Tuple2::_1, personData -> Optional.of(personData._2())));

            brukere.putAll(fnrPersonIdMap);
        }));

        fnrs.stream()
                .filter(not(brukere::containsKey))
                .forEach((ikkeFunnetBruker) -> brukere.put(ikkeFunnetBruker, empty()));

        return brukere;
    }

    public Timestamp getAktiviteterSistOppdatert() {
        return (Timestamp) db.queryForList("SELECT aktiviteter_sist_oppdatert from METADATA").get(0).get("aktiviteter_sist_oppdatert");
    }

    public List<AktivitetDTO> getAktiviteterForAktoerid(String aktoerid) {
        return db.queryForList(getAktiviteterForAktoeridSql(), aktoerid)
                .stream()
                .map(BrukerRepository::mapToAktivitetDTO)
                .filter(aktivitet -> AktivitetTyper.contains(aktivitet.getAktivitetType()))
                .collect(toList());
    }

    private static AktivitetDTO mapToAktivitetDTO(Map<String, Object> map) {
        return new AktivitetDTO()
                .setAktivitetType((String) map.get("AKTIVITETTYPE"))
                .setStatus((String) map.get("STATUS"))
                .setFraDato((Timestamp) map.get("FRADATO"))
                .setTilDato((Timestamp) map.get("TILDATO"));
    }

    public void setAktiviteterSistOppdatert(String sistOppdatert) {
        db.update("UPDATE METADATA SET aktiviteter_sist_oppdatert = ?", timestampFromISO8601(sistOppdatert));
    }

   public void upsertAktivitet(AktivitetDataFraFeed aktivitet) {
     getAktivitetUpsertQuery(this.db,aktivitet).execute();
   }

   public void upsertAktivitetStatuserForBruker(Map<String, Boolean> aktivitetstatus, String aktoerid, String personid) {
        aktivitetstatus.forEach( (aktivitettype, status) -> upsertAktivitetStatuserForBruker(aktivitettype, status, aktoerid, personid) );
   }

   public void upsertAktivitetStatuserForBruker(String aktivitettype, boolean status, String aktoerid, String personid) {
        getUpsertAktivitetStatuserForBrukerQuery(aktivitettype, this.db, status, aktoerid, personid).execute();
   }

    public Map<String,Timestamp> getAktivitetStatusMap(String personid) {
        Map<String, Boolean> statusMap = new HashMap<>();
        Map<String, Timestamp> statusMapTimestamp = new HashMap<>();

        List<Map<String, Object>> statuserFraDb = db.queryForList("SELECT * FROM BRUKERSTATUS_AKTIVITETER where PERSONID=?",personid);

        AktivitetData.aktivitetTyperList.forEach( (type) ->  statusMap.put(type.toString(), kanskjeVerdi(statuserFraDb, type.toString())));

        //Lagrer mapping til Date for å håndtere dato på aktiviteter i fremtiden.
        statusMap.forEach( (key, value) -> statusMapTimestamp.put(key, value ? new Timestamp(Instant.now().toEpochMilli()) : null));

        return statusMapTimestamp;
    }

    private <T> Predicate<T> not(Predicate<T> predicate) {
        return (T t) -> !predicate.test(t);
    }

    public void insertOrUpdateBrukerdata(List<Brukerdata> brukerdata, Collection<String> finnesIDb) {
        Map<Boolean, List<Brukerdata>> eksisterendeBrukere = brukerdata
                .stream()
                .collect(groupingBy((data) -> finnesIDb.contains(data.getPersonid())));


        Brukerdata.batchUpdate(db, eksisterendeBrukere.getOrDefault(true, emptyList()));

        eksisterendeBrukere
                .getOrDefault(false, emptyList())
                .forEach(this::upsertBrukerdata);
    }

    void upsertBrukerdata(Brukerdata brukerdata) {
        brukerdata.toUpsertQuery(db).execute();
    }

    public void insertAktoeridToPersonidMapping(String aktoerId, String personId) {
        try {
            db.update(insertPersonidAktoeridMappingSQL(), aktoerId, personId);
        } catch (DuplicateKeyException e) {
            LOG.info("Aktoerid {} personId {} mapping finnes i databasen", aktoerId, personId);
        }
    }

    public void slettYtelsesdata() {
        SqlUtils.update(db, "bruker_data")
                .set("ytelse", null)
                .set("utlopsdato", null)
                .set("utlopsdatoFasett", null)
                .set("aapMaxtid", null)
                .set("aapMaxtidFasett", null)
                .execute();
    }

    static UpsertQuery getAktivitetUpsertQuery(JdbcTemplate db, AktivitetDataFraFeed aktivitet) {
        return SqlUtils.upsert(db, "AKTIVITETER")
                .where( WhereClause.equals("AKTIVITETID", aktivitet.getAktivitetId()))
                .set("AKTOERID", aktivitet.getAktorId())
                .set("AKTIVITETTYPE", aktivitet.getAktivitetType().toLowerCase())
                .set("AVTALT", aktivitet.isAvtalt())
                .set("FRADATO", aktivitet.getFraDato())
                .set("TILDATO", aktivitet.getTilDato())
                .set("OPPDATERTDATO", aktivitet.getEndretDato())
                .set("STATUS", aktivitet.getStatus().toLowerCase())
                .set("AKTIVITETID", aktivitet.getAktivitetId());
    }

    static UpsertQuery getUpsertAktivitetStatuserForBrukerQuery(String aktivitetstype, JdbcTemplate db, boolean status, String aktoerid, String personid) {
        return SqlUtils.upsert(db, "BRUKERSTATUS_AKTIVITETER" )
                .where(WhereClause.equals("PERSONID", personid).and(WhereClause.equals("AKTIVITETTYPE", aktivitetstype)))
                .set("STATUS", status)
                .set("PERSONID", personid)
                .set("AKTIVITETTYPE", aktivitetstype)
                .set("AKTOERID", aktoerid);
    }

    String retrieveBrukereSQL() {
        return
                "SELECT " +
                        "person_id, " +
                        "fodselsnr, " +
                        "fornavn, " +
                        "etternavn, " +
                        "nav_kontor, " +
                        "formidlingsgruppekode, " +
                        "iserv_fra_dato, " +
                        "kvalifiseringsgruppekode, " +
                        "rettighetsgruppekode, " +
                        "hovedmaalkode, " +
                        "sikkerhetstiltak_type_kode, " +
                        "fr_kode, " +
                        "sperret_ansatt, " +
                        "er_doed, " +
                        "doed_fra_dato, " +
                        "tidsstempel, " +
                        "veilederident, " +
                        "ytelse, " +
                        "utlopsdato, " +
                        "utlopsdatofasett, " +
                        "aapmaxtid, " +
                        "aapmaxtidfasett, " +
                        "oppfolging, " +
                        "venterpasvarfrabruker, " +
                        "venterpasvarfranav, " +
                        "nyesteutlopteaktivitet, " +
                        "iavtaltaktivitet "+
                        "FROM " +
                        "oppfolgingsbruker " +
                        "LEFT JOIN bruker_data " +
                        "ON " +
                        "bruker_data.personid = oppfolgingsbruker.person_id";

    }

    String retrieveBrukerMedBrukerdataSQL() {
        return
                "SELECT " +
                        "person_id, " +
                        "fodselsnr, " +
                        "fornavn, " +
                        "etternavn, " +
                        "nav_kontor, " +
                        "formidlingsgruppekode, " +
                        "iserv_fra_dato, " +
                        "kvalifiseringsgruppekode, " +
                        "rettighetsgruppekode, " +
                        "hovedmaalkode, " +
                        "sikkerhetstiltak_type_kode, " +
                        "fr_kode, " +
                        "sperret_ansatt, " +
                        "er_doed, " +
                        "doed_fra_dato, " +
                        "tidsstempel, " +
                        "veilederident, " +
                        "ytelse," +
                        "utlopsdato, " +
                        "utlopsdatofasett, " +
                        "aapmaxtid, " +
                        "aapmaxtidfasett, " +
                        "oppfolging, " +
                        "venterpasvarfrabruker, " +
                        "venterpasvarfranav, " +
                        "nyesteutlopteaktivitet, " +
                        "iavtaltaktivitet "+
                        "FROM " +
                        "oppfolgingsbruker " +
                        "LEFT JOIN bruker_data " +
                        "ON " +
                        "bruker_data.personid = oppfolgingsbruker.person_id " +
                        "WHERE " +
                        "person_id = ? ";
    }

    String retrieveOppdaterteBrukereSQL() {
        return
                "SELECT " +
                        "person_id, " +
                        "fodselsnr, " +
                        "fornavn, " +
                        "etternavn, " +
                        "nav_kontor, " +
                        "formidlingsgruppekode, " +
                        "iserv_fra_dato, " +
                        "kvalifiseringsgruppekode, " +
                        "rettighetsgruppekode, " +
                        "hovedmaalkode, " +
                        "sikkerhetstiltak_type_kode, " +
                        "fr_kode, " +
                        "sperret_ansatt, " +
                        "er_doed, " +
                        "doed_fra_dato, " +
                        "tidsstempel, " +
                        "veilederident," +
                        "ytelse, " +
                        "utlopsdato, " +
                        "utlopsdatofasett, " +
                        "aapmaxtid, " +
                        "aapmaxtidfasett, " +
                        "oppfolging, " +
                        "venterpasvarfrabruker, " +
                        "venterpasvarfranav, " +
                        "nyesteutlopteaktivitet, " +
                        "iavtaltaktivitet "+
                        "FROM " +
                        "oppfolgingsbruker " +
                        "LEFT JOIN bruker_data " +
                        "ON " +
                        "bruker_data.personid = oppfolgingsbruker.person_id " +
                        "WHERE " +
                        "tidsstempel > (" + retrieveSistIndeksertSQL() + ")";
    }

    String retrieveSistIndeksertSQL() {
        return "SELECT SIST_INDEKSERT FROM METADATA";
    }

    String updateTidsstempelSQL() {
        return
                "UPDATE METADATA SET SIST_INDEKSERT = ?";
    }

    String getPersonidFromAktoeridSQL() {
        return "SELECT PERSONID FROM AKTOERID_TO_PERSONID WHERE AKTOERID = ?";
    }

    String getPersonIdFromFnrSQL() {
        return "SELECT PERSON_ID FROM OPPFOLGINGSBRUKER WHERE FODSELSNR= ?";
    }

    String getPersonIdsFromFnrsSQL() {
        return
                "SELECT " +
                        "person_id, " +
                        "fodselsnr " +
                        "FROM " +
                        "oppfolgingsbruker " +
                        "WHERE " +
                        "fodselsnr in (:fnrs)";
    }

    String insertPersonidAktoeridMappingSQL() {
        return "INSERT INTO AKTOERID_TO_PERSONID VALUES (?,?)";
    }


    String retrieveBrukerSQL() {
        return "SELECT * FROM BRUKER_DATA WHERE AKTOERID=?";
    }

    String retrieveBrukerdataSQL() {
        return "SELECT * FROM BRUKER_DATA WHERE PERSONID in (:fnrs)";
    }

    String getAktiviteterForAktoeridSql() { return "SELECT AKTIVITETTYPE, STATUS, FRADATO, TILDATO FROM AKTIVITETER where aktoerid=?"; }

    public static boolean erOppfolgingsBruker(SolrInputDocument bruker) {
        if(oppfolgingsFlaggSatt(bruker)) {
            return true;
        }
        String innsatsgruppe = (String) bruker.get("kvalifiseringsgruppekode").getValue();

        return !(bruker.get("formidlingsgruppekode").getValue().equals("ISERV") ||
                (bruker.get("formidlingsgruppekode").getValue().equals("IARBS") && (innsatsgruppe.equals("BKART")
                        || innsatsgruppe.equals("IVURD") || innsatsgruppe.equals("KAP11")
                        || innsatsgruppe.equals("VARIG") || innsatsgruppe.equals("VURDI"))));
    }

    static boolean oppfolgingsFlaggSatt(SolrInputDocument bruker) {
        return (Boolean) bruker.get("oppfolging").getValue();
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private ManedMapping manedmappingOrNull(String string) {
        return string != null ? ManedMapping.valueOf(string) : null;
    }

    private YtelseMapping ytelsemappingOrNull(String string) {
        return string != null ? YtelseMapping.valueOf(string) : null;
    }

    private KvartalMapping kvartalmappingOrNull(String string) {
        return string != null ? KvartalMapping.valueOf(string) : null;
    }

    private Boolean kanskjeVerdi(List<Map<String, Object>> statuserFraDb, String type) {
        for(Map<String, Object> rad : statuserFraDb) {
            String aktivitetType = (String) rad.get("AKTIVITETTYPE");
            if(type.equals(aktivitetType)) {
                //med hsql driveren settes det inn false/true og med oracle settes det inn 0/1.
                return  Boolean.valueOf( (String) rad.get("STATUS")) || "1".equals(rad.get("STATUS"));
            }
        }
        return false;
    }
}
