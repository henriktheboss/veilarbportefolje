package no.nav.pto.veilarbportefolje.arenapakafka.ytelser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.arenapakafka.arenaDTO.YtelsesInnhold;
import no.nav.pto.veilarbportefolje.domene.value.PersonId;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static no.nav.pto.veilarbportefolje.database.Table.YTELSER.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class YtelsesRepository {
    private final JdbcTemplate db;

    public void upsertYtelse(AktorId aktorId, TypeKafkaYtelse type, YtelsesInnhold innhold) {
        Timestamp utlopsdato = Optional.ofNullable(innhold.getTilOgMedDato())
                .map(arenaDato -> Timestamp.valueOf(arenaDato.getLocalDate()))
                .orElse(null);
        Timestamp startDato = Optional.ofNullable(innhold.getFraOgMedDato())
                .map(arenaDato -> Timestamp.valueOf(arenaDato.getLocalDate()))
                .orElse(null);

        SqlUtils.upsert(db, TABLE_NAME)
                .set(VEDTAKID, innhold.getVedtakId())
                .set(AKTOERID, aktorId.get())
                .set(PERSONID, innhold.getPersonId())
                .set(YTELSESTYPE, type.toString())
                .set(SAKSID, innhold.getSaksId())
                .set(SAKSTYPEKODE, innhold.getSakstypeKode())
                .set(RETTIGHETSTYPEKODE, innhold.getRettighetstypeKode())
                .set(UTLOPSDATO, utlopsdato)
                .set(STARTDATO, startDato)
                .set(ANTALLUKERIGJEN, innhold.getAntallUkerIgjen())
                .set(ANTALLPERMITTERINGUKER, innhold.getAntallUkerIgjenUnderPermittering())
                .set(ANTALLUKERIGJENUNNTAK, innhold.getAntallDagerIgjenUnntak())
                .where(WhereClause.equals(VEDTAKID, innhold.getVedtakId()))
                .execute();
    }

    public List<YtelseDAO> getYtelser(AktorId aktorId) {
        if (aktorId == null) {
            return new ArrayList<>();
        }

        final String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + AKTOERID + " = ?";

        return db.queryForList(sql, aktorId.get())
                .stream().map(this::mapTilYtelseDOA)
                .collect(toList());
    }

    private YtelseDAO mapTilYtelseDOA(Map<String, Object> row) {
        return new YtelseDAO()
                .setAktorId(AktorId.of((String) row.get(AKTOERID)))
                .setPersonId(PersonId.of((String) row.get(PERSONID)))
                .setSaksId((String) row.get(SAKSID))
                .setType(TypeKafkaYtelse.valueOf((String) row.get(YTELSESTYPE)))
                .setSakstypeKode((String) row.get(SAKSTYPEKODE))
                .setRettighetstypeKode((String) row.get(RETTIGHETSTYPEKODE))
                .setUtlopsDato((Timestamp) row.get(UTLOPSDATO))
                .setStartDato((Timestamp) row.get(STARTDATO))
                .setAntallUkerIgjen((Integer) row.get(ANTALLUKERIGJEN))
                .setAntallUkerIgjenPermittert((Integer) row.get(ANTALLPERMITTERINGUKER))
                .setAntallDagerIgjenUnntak((Integer) row.get(ANTALLUKERIGJENUNNTAK));
    }

    public void slettYtelse(String vedtakId) {
        if(vedtakId == null){
            return;
        }
        log.info("Sletter ytelse: {}", vedtakId);
        SqlUtils.delete(db, TABLE_NAME)
                .where(WhereClause.equals(VEDTAKID, vedtakId))
                .execute();
    }
}