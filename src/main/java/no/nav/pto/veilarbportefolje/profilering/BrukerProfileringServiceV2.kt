package no.nav.pto.veilarbportefolje.profilering

import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService
import no.nav.paw.arbeidss
class BrukerProfileringServiceV2(
    private val brukerProfileringRepositoryV3: BrukerProfileringRepositoryV3
) : KafkaCommonConsumerService<Profilering> {
}