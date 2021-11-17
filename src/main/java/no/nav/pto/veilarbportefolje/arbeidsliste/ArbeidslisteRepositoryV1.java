package no.nav.pto.veilarbportefolje.arbeidsliste;

import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.database.Table;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.AKTOERID;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.ENDRINGSTIDSPUNKT;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.FNR;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.FRIST;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.KATEGORI;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.KOMMENTAR;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.NAV_KONTOR_FOR_ARBEIDSLISTE;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.OVERSKRIFT;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.SIST_ENDRET_AV_VEILEDERIDENT;
import static no.nav.pto.veilarbportefolje.database.Table.ARBEIDSLISTE.TABLE_NAME;
import static no.nav.pto.veilarbportefolje.util.DateUtils.toZonedDateTime;
import static no.nav.sbl.sql.SqlUtils.select;
import static no.nav.sbl.sql.SqlUtils.update;
import static no.nav.sbl.sql.SqlUtils.upsert;

@Slf4j
@Repository
public class ArbeidslisteRepositoryV1 implements ArbeidslisteRepository {

    private final JdbcTemplate db;

    @Autowired
    public ArbeidslisteRepositoryV1(JdbcTemplate db) {
        this.db = db;
    }

    public Optional<String> hentNavKontorForArbeidsliste(AktorId aktoerId) {
        String navKontor = select(db, Table.ARBEIDSLISTE.TABLE_NAME, rs -> rs.getString(NAV_KONTOR_FOR_ARBEIDSLISTE))
                .column(NAV_KONTOR_FOR_ARBEIDSLISTE)
                .where(WhereClause.equals(AKTOERID, aktoerId.toString()))
                .execute();

        return Optional.ofNullable(navKontor);
    }

    public Try<Arbeidsliste> retrieveArbeidsliste(AktorId aktoerId) {
        return Try.of(
                () -> select(db, Table.ARBEIDSLISTE.TABLE_NAME, ArbeidslisteRepositoryV1::arbeidslisteMapper)
                        .column("*")
                        .where(WhereClause.equals(AKTOERID, aktoerId.toString()))
                        .execute()
        );
    }

    public Try<ArbeidslisteDTO> retrieveArbeidslisteDTO(AktorId aktoerId) {
        return Try.of(
                () -> select(db, Table.ARBEIDSLISTE.TABLE_NAME, this::arbeidslisteDtoMapper)
                        .column("*")
                        .where(WhereClause.equals(AKTOERID, aktoerId.toString()))
                        .execute()
        );
    }

    public List<AktorId> hentAlleBrukereMedArbeidsliste() {
        return db.queryForList("SELECT " + AKTOERID + " FROM " + Table.ARBEIDSLISTE.TABLE_NAME, AktorId.class);
    }

    public Try<ArbeidslisteDTO> insertArbeidsliste(ArbeidslisteDTO dto) {
        return Try.of(
                () -> {

                    AktorId aktoerId = Optional
                            .ofNullable(dto.getAktorId())
                            .orElseThrow(() -> new RuntimeException("Fant ikke aktør-ID"));

                    upsert(db, TABLE_NAME)
                            .set(AKTOERID, aktoerId.toString())
                            .set(FNR, dto.getFnr().toString())
                            .set(SIST_ENDRET_AV_VEILEDERIDENT, dto.getVeilederId().toString())
                            .set(ENDRINGSTIDSPUNKT, Timestamp.from(now()))
                            .set(OVERSKRIFT, dto.getOverskrift())
                            .set(KOMMENTAR, dto.getKommentar())
                            .set(FRIST, dto.getFrist())
                            .set(KATEGORI, dto.getKategori().name())
                            .set(NAV_KONTOR_FOR_ARBEIDSLISTE, dto.getNavKontorForArbeidsliste())
                            .where(WhereClause.equals(AKTOERID, aktoerId.toString()))
                            .execute();

                    return dto;
                }
        ).onFailure(e -> log.warn("Kunne ikke inserte arbeidsliste til db", e));
    }


    public Try<ArbeidslisteDTO> updateArbeidsliste(ArbeidslisteDTO data) {
        return Try.of(
                () -> {
                    Timestamp endringsTidspunkt = Timestamp.from(now());
                    update(db, TABLE_NAME)
                            .set("SIST_ENDRET_AV_VEILEDERIDENT", data.getVeilederId().toString())
                            .set("ENDRINGSTIDSPUNKT", endringsTidspunkt)
                            .set("OVERSKRIFT", data.getOverskrift())
                            .set("KOMMENTAR", data.getKommentar())
                            .set("FRIST", data.getFrist())
                            .set("KATEGORI", data.getKategori().name())
                            .whereEquals("AKTOERID", data.getAktorId().toString())
                            .execute();
                    return data.setEndringstidspunkt(endringsTidspunkt);
                }
        ).onFailure(e -> log.warn("Kunne ikke oppdatere arbeidsliste i db", e));
    }

    public int slettArbeidsliste(AktorId aktoerId) {
        return SqlUtils.delete(db, TABLE_NAME)
                .where(WhereClause.equals(AKTOERID, aktoerId.get()))
                .execute();
    }

    @SneakyThrows
    private static Arbeidsliste arbeidslisteMapper(ResultSet rs) {
        return new Arbeidsliste(
                VeilederId.of(rs.getString("SIST_ENDRET_AV_VEILEDERIDENT")),
                toZonedDateTime(rs.getTimestamp("ENDRINGSTIDSPUNKT")),
                rs.getString("OVERSKRIFT"),
                rs.getString("KOMMENTAR"),
                toZonedDateTime(rs.getTimestamp("FRIST")),
                Arbeidsliste.Kategori.valueOf(rs.getString("KATEGORI"))
        );
    }

    @SneakyThrows
    private ArbeidslisteDTO arbeidslisteDtoMapper(ResultSet rs) {
        return new ArbeidslisteDTO(Fnr.of(rs.getString(FNR)))
                .setAktorId(AktorId.of(rs.getString(AKTOERID)))
                .setFrist(rs.getTimestamp(FRIST))
                .setVeilederId(VeilederId.of(rs.getString(SIST_ENDRET_AV_VEILEDERIDENT)))
                .setEndringstidspunkt(rs.getTimestamp(ENDRINGSTIDSPUNKT))
                .setOverskrift(rs.getString(OVERSKRIFT))
                .setKommentar(rs.getString(KOMMENTAR))
                .setKategori(Arbeidsliste.Kategori.valueOf(rs.getString(KATEGORI)))
                .setNavKontorForArbeidsliste(rs.getString(NAV_KONTOR_FOR_ARBEIDSLISTE));
    }

}
