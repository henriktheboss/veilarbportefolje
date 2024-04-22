package no.nav.pto.veilarbportefolje.arbeidssoker

import no.nav.common.types.identer.Fnr
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService
import java.util.*
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode as PeriodeEkstern

class ArbeidssokerPeriodeService(
    private val arbeidssokerPeriodeRepository: ArbeidssokerPeriodeRepository,
    private val oppslagArbeidssoekerregisteretClient: OppslagArbeidssoekerregisteretClient
) : KafkaCommonConsumerService<PeriodeEkstern>() {

    override fun behandleKafkaMeldingLogikk(kafkaMelding: PeriodeEkstern) {
        arbeidssokerPeriodeRepository.upsert(SistePeriode.of(kafkaMelding))
    }

    fun hentOgLagreArbeidssokerPeriodeData(fnr: Fnr) {
        oppslagArbeidssoekerregisteretClient.hentArbeidssokerPerioder(fnr)
    }
}

data class SistePeriode(
    val id: UUID,
    val fnr: Fnr,
) {
    companion object {
        fun of(periodeEkstern: PeriodeEkstern): SistePeriode {
            return SistePeriode(
                id = periodeEkstern.id,
                fnr = Fnr.ofValidFnr(periodeEkstern.identitetsnummer.toString()), // TODO Sjekk denne
            )
        }
    }
}