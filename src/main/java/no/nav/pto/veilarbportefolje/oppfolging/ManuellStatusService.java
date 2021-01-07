package no.nav.pto.veilarbportefolje.oppfolging;

import no.nav.common.json.JsonUtils;
import no.nav.pto.veilarbportefolje.domene.ManuellBrukerStatus;
import no.nav.pto.veilarbportefolje.domene.value.AktoerId;
import no.nav.pto.veilarbportefolje.elastic.ElasticServiceV2;
import no.nav.pto.veilarbportefolje.kafka.KafkaConsumerService;
import org.springframework.stereotype.Service;

@Service
public class ManuellStatusService implements KafkaConsumerService<String> {
    private final OppfolgingRepository oppfolgingRepository;
    private final ElasticServiceV2 elasticServiceV2;

    public ManuellStatusService(OppfolgingRepository oppfolgingRepository, ElasticServiceV2 elasticServiceV2) {
        this.oppfolgingRepository = oppfolgingRepository;
        this.elasticServiceV2 = elasticServiceV2;
    }

    @Override
    public void behandleKafkaMelding(String kafkaMelding) {
        final ManuellStatusDTO dto = JsonUtils.fromJson(kafkaMelding, ManuellStatusDTO.class);
        final AktoerId aktoerId = AktoerId.of(dto.getAktorId());

        oppfolgingRepository.settManuellStatus(aktoerId, dto.isErManuell());

        String manuellStatus = dto.isErManuell() ? ManuellBrukerStatus.MANUELL.name() : null;

        elasticServiceV2.settManuellStatus(aktoerId, manuellStatus);
    }

    @Override
    public boolean shouldRewind() {
        return false;
    }

    @Override
    public void setRewind(boolean rewind) {

    }
}
