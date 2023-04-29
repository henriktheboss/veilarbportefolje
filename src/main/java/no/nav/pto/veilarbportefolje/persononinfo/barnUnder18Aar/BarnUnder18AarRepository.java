package no.nav.pto.veilarbportefolje.persononinfo.barnUnder18Aar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.opensearch.domene.BarnUnder18AarData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.pto.veilarbportefolje.postgres.PostgresUtils.queryForObjectOrNull;
import static no.nav.pto.veilarbportefolje.util.DateUtils.toLocalDateOrNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BarnUnder18AarRepository {

    @Qualifier("PostgresJdbcReadOnly")
    private final JdbcTemplate dbReadOnly;

    private final JdbcTemplate db;

    public List<BarnUnder18AarData> hentBarnUnder18Aar(String fnr) {
        List<BarnUnder18AarData> barn = dbReadOnly.queryForList("""
                            SELECT * FROM BRUKER_DATA_BARN WHERE FORESATT_IDENT = ?
                        """, fnr).stream()
                .map(this::mapTilBarnUnder18)
                .toList();

        return barn;
    }

    //TODO Legg til fnr_barn i tabellen. Slett "id".
    //TODO Ta inn PDLPerson her og hent ut data derfra
    public void upsert(Fnr fnrBarn, Fnr fnrForesatt, Boolean borMedForesatt, LocalDate barnFoedselsdato, String diskresjonskode) {
        db.update("""
                        INSERT INTO bruker_data_barn (id, foresatt_ident, bor_med_foresatt, barn_foedselsdato, barn_diskresjonkode)
                        VALUES(?,?,?,?,?) ON CONFLICT (id, foresatt_ident) DO UPDATE SET
                         (bor_med_foresatt, barn_foedselsdato, barn_diskresjonkode) =
                         (excluded.bor_med_foresatt, excluded.barn_foedselsdato, excluded.barn_diskresjonkode)
                         """,
                fnrBarn.get(), fnrForesatt.get(), borMedForesatt, barnFoedselsdato, diskresjonskode);
    }

    public boolean erEndringForBarnAvBrukerUnderOppfolging(List<Fnr> fnrs) {
        String identerParam = fnrs.stream().map(Fnr::get).collect(Collectors.joining(",", "{", "}"));
        return Optional.ofNullable(
                queryForObjectOrNull(() -> db.queryForObject("""
                        SELECT COUNT(*) as barnAntall FROM BRUKER_DATA_BARN WHERE id = any (?::varchar[])
                        """, (rs, row) -> rs.getInt("barnAntall") > 0, identerParam))
        ).orElse(false);
    }


    private BarnUnder18AarData mapTilBarnUnder18(Map<String, Object> rs) {
        return new BarnUnder18AarData(
                alderFraFodselsdato(toLocalDateOrNull((java.sql.Date) rs.get("BARN_FOEDSELSDATO")),
                        LocalDate.now()), (boolean) rs.get("BOR_MED_FORESATT"),
                (String) rs.get("BARN_DISKRESJONKODE"));
    }

    public static Long alderFraFodselsdato(LocalDate date, LocalDate now) {
        Integer age = Period.between(date, now).getYears();
        return age.longValue();
    }
}


