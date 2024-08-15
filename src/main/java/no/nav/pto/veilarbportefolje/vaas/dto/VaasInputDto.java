package no.nav.pto.veilarbportefolje.vaas.dto;

import no.nav.common.types.identer.Fnr;

public record VaasInputDto(String handelseId, Fnr fnr, String avsender,
                           String opprettet, String hendelse_navn, String hendelse_lenk,
                           String tiltakstype_kode) {
}
