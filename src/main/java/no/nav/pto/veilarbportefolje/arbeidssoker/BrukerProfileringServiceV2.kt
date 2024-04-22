package no.nav.pto.veilarbportefolje.arbeidssoker

import no.nav.paw.arbeidssokerregisteret.api.v1.ProfilertTil
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Profilering as ProfileringEkstern
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService
import java.util.*

class BrukerProfileringServiceV2(
    private val brukerProfileringRepositoryV3: BrukerProfileringRepositoryV3
) : KafkaCommonConsumerService<ProfileringEkstern>() {

    override fun behandleKafkaMeldingLogikk(kafkaMelding: ProfileringEkstern) {
        val profilering = Profilering.of(kafkaMelding)
        brukerProfileringRepositoryV3.upsertBrukerProfilering(profilering)
    }
}

data class Profilering(
    val id: UUID,
    val periodeId: UUID,
    val opplysningerOmArbeidssokerId: UUID,
    val sendtInnAv: Metadata,
    val profilertTil: ProfilertTil,
    val jobbetSammenhengendeSeksAvTolvSisteMnd: Boolean,
    val alder: Int,
) {
    companion object {
        fun of(profileringEkstern: ProfileringEkstern): Profilering {
            return Profilering(
                id = profileringEkstern.id,
                periodeId = profileringEkstern.periodeId,
                opplysningerOmArbeidssokerId = profileringEkstern.opplysningerOmArbeidssokerId,
                sendtInnAv = profileringEkstern.sendtInnAv,
                profilertTil = profileringEkstern.profilertTil,
                jobbetSammenhengendeSeksAvTolvSisteMnd = profileringEkstern.jobbetSammenhengendeSeksAvTolvSisteMnd,
                alder = profileringEkstern.alder,
            )
        }
    }
}