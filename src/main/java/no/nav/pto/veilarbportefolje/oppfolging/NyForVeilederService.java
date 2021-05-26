package no.nav.pto.veilarbportefolje.oppfolging;

import lombok.RequiredArgsConstructor;
import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.json.JsonUtils;
import no.nav.pto.veilarbportefolje.config.FeatureToggle;
import no.nav.pto.veilarbportefolje.elastic.ElasticServiceV2;
import no.nav.pto.veilarbportefolje.kafka.KafkaConsumerService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

import static no.nav.pto.veilarbportefolje.config.FeatureToggle.erPostgresPa;

@Service
@RequiredArgsConstructor
public class NyForVeilederService implements KafkaConsumerService<String> {

    private final OppfolgingRepository oppfolgingRepository;
    private final OppfolgingRepositoryV2 oppfolgingRepositoryV2;
    private final ElasticServiceV2 elasticServiceV2;
    private final UnleashService unleashService;
    private final AtomicBoolean rewind = new AtomicBoolean(false);

    @Override
    public void behandleKafkaMelding(String kafkaMelding) {
        final NyForVeilederDTO dto = JsonUtils.fromJson(kafkaMelding, NyForVeilederDTO.class);

        final boolean brukerIkkeErNyForVeileder = !dto.isNyForVeileder();
        if (brukerIkkeErNyForVeileder) {
            oppfolgingRepository.settNyForVeileder(dto.getAktorId(), false);
            if (erPostgresPa(unleashService)) {
                oppfolgingRepositoryV2.settNyForVeileder(dto.getAktorId(), false);
            }
            elasticServiceV2.oppdaterNyForVeileder(dto.getAktorId(), false);
        } else if (unleashService.isEnabled(FeatureToggle.FIKS_NY_FOR_VEILEDER)) {
            oppfolgingRepository.settNyForVeileder(dto.getAktorId(), true);
            if (erPostgresPa(unleashService)) {
                oppfolgingRepositoryV2.settNyForVeileder(dto.getAktorId(), true);
            }
            elasticServiceV2.oppdaterNyForVeileder(dto.getAktorId(), true);
        }
        // Vi trenger ikke å opppdatere db/indeks når bruker er ny for veileder, siden dette gjøres i VeilederTilordnetService
    }

    @Override
    public boolean shouldRewind() {
        return rewind.get();
    }

    @Override
    public void setRewind(boolean rewind) {
        this.rewind.set(rewind);
    }
}
