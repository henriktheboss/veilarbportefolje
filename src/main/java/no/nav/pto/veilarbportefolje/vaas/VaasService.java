package no.nav.pto.veilarbportefolje.vaas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService;
import no.nav.pto.veilarbportefolje.vaas.dto.VaasInputDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaasService extends KafkaCommonConsumerService<VaasInputDto> {
    private final VaasRepository vaasRepository;

    public void lagreHandelse() {

    }

    @Override
    protected void behandleKafkaMeldingLogikk(VaasInputDto kafkaMelding) {

    }
}
