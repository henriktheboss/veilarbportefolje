package no.nav.pto.veilarbportefolje.oppfolging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.oppfolging.response.SkjermingData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

import static no.nav.pto.veilarbportefolje.database.PostgresTable.NOM_SKJERMING.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkjermingRepository {
    @Qualifier("PostgresJdbc")
    private final JdbcTemplate db;

    public Boolean settSkjermingPeriode(Fnr fnr, Timestamp skjermetFra, Timestamp skjermetTil) {
        try {
            int updatedNum = db.update(String.format("INSERT INTO %s (%s, %s, %s) VALUES (?,?,?, ?)" +
                                    " ON CONFLICT (%s) DO UPDATE SET %s = EXCLUDED.%s, %s = EXCLUDED.%s",
                            TABLE_NAME, FNR, SKJERMET_FRA, SKJERMET_TIL, FNR, SKJERMET_FRA, SKJERMET_FRA, SKJERMET_TIL, SKJERMET_TIL),
                    fnr, skjermetFra, skjermetTil);
            return updatedNum > 0;
        } catch (Exception e) {
            log.error("Can't set skjerming " + e, e);
            return false;
        }
    }

    public Boolean settSkjerming(Fnr fnr, Boolean erSkjermet) {
        try {
            int updatedNum = db.update(String.format("INSERT INTO %s (%s, %s) VALUES (?,?)" +
                                    " ON CONFLICT (%s) DO UPDATE SET %s = EXCLUDED.%s",
                            TABLE_NAME, FNR, ER_SKJERMET, FNR, ER_SKJERMET, ER_SKJERMET),
                    fnr, erSkjermet);
            return updatedNum > 0;
        } catch (Exception e) {
            log.error("Can't set skjerming " + e, e);
            return false;
        }
    }

    public Optional<SkjermingData> hentSkjermingData(Fnr fnr) {
        try {
            return Optional.ofNullable(db.queryForObject(String.format("SELECT %s, %s, %s FROM %s WHERE %s = %s",
                            ER_SKJERMET, SKJERMET_FRA, SKJERMET_TIL, TABLE_NAME, FNR, fnr),
                    (rs, rowNum) -> new SkjermingData(fnr, rs.getBoolean(ER_SKJERMET), rs.getTimestamp(SKJERMET_FRA), rs.getTimestamp(SKJERMET_TIL))));
        } catch (Exception e) {
            log.error("Can't get skjerming data " + e, e);
            return Optional.empty();
        }
    }
}
