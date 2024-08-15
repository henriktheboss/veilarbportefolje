package no.nav.pto.veilarbportefolje.vaas.dto;

import java.time.LocalDateTime;

public record VaasOutputDto(String hendelse_navn, String hendelse_lenk, String tiltakstype_kode,
                            LocalDateTime opprettet) {
}
