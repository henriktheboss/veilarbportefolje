package no.nav.fo.database;

import javaslang.control.Try;
import lombok.SneakyThrows;
import no.nav.fo.domene.AktoerId;
import no.nav.fo.domene.Arbeidsliste;
import no.nav.fo.provider.rest.arbeidsliste.ArbeidslisteData;
import no.nav.fo.util.sql.SelectQuery;
import no.nav.fo.util.sql.where.WhereClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static no.nav.fo.util.sql.SqlUtils.update;
import static no.nav.fo.util.sql.SqlUtils.upsert;

public class ArbeidslisteRepository {
    private static Logger LOG = LoggerFactory.getLogger(ArbeidslisteRepository.class);

    @Inject
    private JdbcTemplate db;

    private static final String ARBEIDSLISTE = "ARBEIDSLISTE";

    public Try<Arbeidsliste> retrieveArbeidsliste(AktoerId aktoerId) {
        return Try.of(
                () -> new SelectQuery<Arbeidsliste>(db, ARBEIDSLISTE)
                        .column("*")
                        .whereEquals("AKTOERID", aktoerId.toString())
                        .usingMapper(this::arbeidslisteMapper)
                        .execute()
        ).onFailure(e -> LOG.warn("Kunne ikke hente ut arbeidsliste fra db: {}", getCauseString(e)));
    }

    public Try<AktoerId> insertArbeidsliste(ArbeidslisteData data) {
        return Try.of(
                () -> {
                    String aktoerId = data.getAktoerId().toString();
                    upsert(db, ARBEIDSLISTE)
                            .set("AKTOERID", aktoerId)
                            .set("VEILEDERIDENT", data.getVeilederId())
                            .set("KOMMENTAR", data.getKommentar())
                            .set("FRIST", data.getFrist())
                            .set("ENDRINGSTIDSPUNKT", Timestamp.from(Instant.now()))
                            .where(WhereClause.equals("AKTOERID", aktoerId))
                            .execute();
                    return data.getAktoerId();
                }
        ).onFailure(e -> LOG.warn("Kunne ikke inserte arbeidsliste til db: {}", getCauseString(e)));
    }


    public Try<AktoerId> updateArbeidsliste(ArbeidslisteData data) {
        return Try.of(
                () -> {
                    update(db, ARBEIDSLISTE)
                            .set("VEILEDERIDENT", data.getVeilederId())
                            .set("KOMMENTAR", data.getKommentar())
                            .set("FRIST", data.getFrist())
                            .set("ENDRINGSTIDSPUNKT", Timestamp.from(Instant.now()))
                            .whereEquals("AKTOERID", data.getAktoerId().toString())
                            .execute();
                    return data.getAktoerId();
                }
        ).onFailure(e -> LOG.warn("Kunne ikke oppdatere arbeidsliste i db: {}", getCauseString(e)));
    }

    public Try<AktoerId> deleteArbeidsliste(AktoerId aktoerID) {
        int update = db.update("DELETE FROM ARBEIDSLISTE WHERE AKTOERID = ?", aktoerID.toString());

        if (update == 0) {
            return Try.failure(new RuntimeException("Kunne ikke slette rad fra database"));
        } else {
            return Try.success(aktoerID);
        }
    }

    @SneakyThrows
    private Arbeidsliste arbeidslisteMapper(ResultSet rs) {
        return new Arbeidsliste(
                rs.getString("VEILEDERIDENT"),
                rs.getTimestamp("ENDRINGSTIDSPUNKT"),
                rs.getString("KOMMENTAR"),
                rs.getTimestamp("FRIST")
        );
    }

    private static String getCauseString(Throwable e) {
        return e.getCause().getCause().toString();
    }
}
