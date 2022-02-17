package no.nav.pto.veilarbportefolje.postgres.opensearch.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.pto.veilarbportefolje.aktiviteter.AktivitetsType;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class AktivitetEntity {
    AktivitetsType aktivitetsType;
    String muligTiltaksNavn; // Er kun satt for aktiviteter lagret i tiltaks tabellen
    Timestamp utlop;
    Timestamp start;
}