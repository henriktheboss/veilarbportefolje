package no.nav.fo.domene;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.fo.util.SqlUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Brukerdata {
    private String aktoerid;
    private String veileder;
    private String personid;
    private LocalDateTime tildeltTidspunkt;
    private YtelseMapping ytelse;
    private LocalDateTime utlopsdato;
    private ManedMapping utlopsdatoFasett;
    private LocalDateTime aapMaxtid;
    private KvartalMapping aapMaxtidFasett;

    public SqlUtils.UpdateQuery toUpdateQuery(JdbcTemplate db) {
        return SqlUtils.UpdateQuery.update(db, "bruker_data")
                .set("VEILEDERIDENT", veileder)
                .set("TILDELT_TIDSPUNKT", toTimestamp(tildeltTidspunkt))
                .set("AKTOERID", aktoerid)
                .set("YTELSE", ytelse != null ? ytelse.toString() : null)
                .set("UTLOPSDATO", toTimestamp(utlopsdato))
                .set("UTLOPSDATOFASETT", utlopsdatoFasett != null ? utlopsdatoFasett.toString() : null)
                .set("AAPMAXTID", toTimestamp(aapMaxtid))
                .set("AAPMAXTIDFASETT", aapMaxtidFasett != null ? aapMaxtidFasett.toString() : null)
                .whereEquals("PERSONID", personid);
    }

    public SqlUtils.InsertQuery toInsertQuery(JdbcTemplate db) {
        return SqlUtils.InsertQuery.insert(db, "bruker_data")
                .value("VEILEDERIDENT", veileder)
                .value("TILDELT_TIDSPUNKT", toTimestamp(tildeltTidspunkt))
                .value("AKTOERID", aktoerid)
                .value("YTELSE", ytelse != null ? ytelse.toString() : null)
                .value("UTLOPSDATO", toTimestamp(utlopsdato))
                .value("UTLOPSDATOFASETT", utlopsdatoFasett != null ? utlopsdatoFasett.toString() : null)
                .value("AAPMAXTID", toTimestamp(aapMaxtid))
                .value("AAPMAXTIDFASETT", aapMaxtidFasett != null ? aapMaxtidFasett.toString() : null)
                .value("PERSONID", personid);

    }

    private Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

}
