package no.nav.pto.veilarbportefolje.vaas;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.util.DateUtils;
import no.nav.pto.veilarbportefolje.vaas.dto.VaasInputDto;
import no.nav.pto.veilarbportefolje.vaas.dto.VaasOutputDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.pto.veilarbportefolje.database.PostgresTable.VAAS.*;
import static no.nav.pto.veilarbportefolje.util.SecureLog.secureLog;


@Repository
@RequiredArgsConstructor
@Slf4j
public class VaasRepository {
    private final JdbcTemplate db;

    @SneakyThrows
    public void lagreVaasHendelse(VaasInputDto vaasInputDto) {
        db.update(String.format("""
                                INSERT INTO %s(%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)
                                ON CONFLICT(%s) DO UPDATE SET (%s, %s, %s, %s, %s, %s) = (
                                excluded.%s, excluded.%s, excluded.%s, excluded.%s, excluded.%s, excluded.%s)
                                """
                        ,
                        TABLE_NAME, HENDELSE_ID, IDENT, HENDELSE_NAVN, HENDELSE_LENK, TILTAKSTYPE_KODE, AVSENDER,
                        HENDELSE_ID, IDENT, HENDELSE_NAVN, HENDELSE_LENK, TILTAKSTYPE_KODE, AVSENDER, OPPRETTET,
                        IDENT, HENDELSE_NAVN, HENDELSE_LENK, TILTAKSTYPE_KODE, AVSENDER, OPPRETTET),
                vaasInputDto.handelseId(), vaasInputDto.fnr(), vaasInputDto.hendelse_navn(), vaasInputDto.hendelse_lenk(),
                vaasInputDto.tiltakstype_kode(), vaasInputDto.avsender()
        );
    }

    public Map<Fnr, VaasOutputDto> hentVaasHendelserForBrukere(List<Fnr> fnrs) {
        Map<Fnr, VaasOutputDto> result = new HashMap<>();

        String fnrsCondition = fnrs.stream().map(Fnr::toString).collect(Collectors.joining(",", "{", "}"));
        db.query(String.format("""
                        SELECT %s, %s, %s, %s, %s  FROM %s WHERE FODSELSNR = ANY (?::varchar[])
                        """, IDENT, HENDELSE_NAVN, HENDELSE_LENK, TILTAKSTYPE_KODE, OPPRETTET, TABLE_NAME),
                ps -> ps.setString(1, fnrsCondition),
                (ResultSet rs) -> {
                    result.put(Fnr.of(rs.getString(IDENT)), new VaasOutputDto(
                            rs.getString(HENDELSE_NAVN),
                            rs.getString(HENDELSE_LENK),
                            rs.getString(TILTAKSTYPE_KODE),
                            DateUtils.toLocalDateTimeOrNull(rs.getTimestamp(OPPRETTET))
                    ));
                }
        );

        return result;
    }

    @SneakyThrows
    public void sletteVaasHendelse(UUID hendelseId) {
        secureLog.info("Sletter handelse med id {}", hendelseId);
        db.update(String.format("DELETE FROM %s WHERE %s = ?", TABLE_NAME, HENDELSE_ID), hendelseId);
    }
}
