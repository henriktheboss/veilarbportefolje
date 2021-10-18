package no.nav.pto.veilarbportefolje.aktiviteter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbportefolje.database.BrukerDataService;
import no.nav.pto.veilarbportefolje.service.UnleashService;
import no.nav.pto.veilarbportefolje.database.PersistentOppdatering;
import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService;
import no.nav.pto.veilarbportefolje.elastic.ElasticServiceV2;
import no.nav.pto.veilarbportefolje.kafka.KafkaConsumerService;
import no.nav.pto.veilarbportefolje.oppfolging.OppfolgingRepository;
import no.nav.pto.veilarbportefolje.service.BrukerService;
import no.nav.pto.veilarbportefolje.sisteendring.SisteEndringService;
import no.nav.pto.veilarbportefolje.util.BatchConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.pto.veilarbportefolje.config.FeatureToggle.erGR202PaKafka;
import static no.nav.pto.veilarbportefolje.util.BatchConsumer.batchConsumer;

@Slf4j
@Service
public class AktivitetService extends KafkaCommonConsumerService<KafkaAktivitetMelding> implements KafkaConsumerService<String> {

    private final BrukerService brukerService;
    private final AktivitetDAO aktivitetDAO;
    private final AktiviteterRepositoryV2 aktiviteterRepositoryV2;
    private final BrukerDataService brukerDataService;
    private final AktivitetStatusRepositoryV2 prossesertAktivitetRepositoryV2;
    private final PersistentOppdatering persistentOppdatering;
    private final AtomicBoolean rewind;
    private final SisteEndringService sisteEndringService;
    @Getter
    private final UnleashService unleashService;
    private final OppfolgingRepository oppfolgingRepository;
    private final ElasticServiceV2 elasticServiceV2;

    @Autowired
    public AktivitetService(AktivitetDAO aktivitetDAO, AktiviteterRepositoryV2 aktiviteterRepositoryV2, AktivitetStatusRepositoryV2 prossesertAktivitetRepositoryV2, PersistentOppdatering persistentOppdatering, BrukerService brukerService, BrukerDataService brukerDataService, SisteEndringService sisteEndringService, OppfolgingRepository oppfolgingRepository, ElasticServiceV2 elasticServiceV2, UnleashService unleashService) {
        this.aktivitetDAO = aktivitetDAO;
        this.aktiviteterRepositoryV2 = aktiviteterRepositoryV2;
        this.prossesertAktivitetRepositoryV2 = prossesertAktivitetRepositoryV2;
        this.brukerService = brukerService;
        this.persistentOppdatering = persistentOppdatering;
        this.brukerDataService = brukerDataService;
        this.sisteEndringService = sisteEndringService;
        this.unleashService = unleashService;
        this.oppfolgingRepository = oppfolgingRepository;
        this.elasticServiceV2 = elasticServiceV2;
        this.rewind = new AtomicBoolean();
    }

    @Override
    public void behandleKafkaMelding(String kafkaMelding) {
        KafkaAktivitetMelding aktivitetData = fromJson(kafkaMelding, KafkaAktivitetMelding.class);
        behandleKafkaMeldingLogikk(aktivitetData);
    }

    protected void behandleKafkaMeldingLogikk(KafkaAktivitetMelding aktivitetData) {
        log.info(
                "Behandler kafka-aktivtet-melding på aktorId: {} med aktivtetId: {}, version: {}",
                aktivitetData.getAktorId(),
                aktivitetData.getAktivitetId(),
                aktivitetData.getVersion()
        );

        sisteEndringService.behandleAktivitet(aktivitetData);
        if (skallIkkeOppdatereAktivitet(aktivitetData)) {
            return;
        }

        aktivitetDAO.tryLagreAktivitetData(aktivitetData);
        utledOgIndekserAktivitetstatuserForAktoerid(AktorId.of(aktivitetData.getAktorId()));

        //POSTGRES
        lagreOgProsseseserAktiviteter(aktivitetData);
        if (!oppfolgingRepository.erUnderoppfolging(AktorId.of(aktivitetData.getAktorId()))) {
            elasticServiceV2.deleteIfPresent(AktorId.of(aktivitetData.getAktorId()),
                    String.format("(AktivitetService) Sletter aktorId da brukeren ikke lengre er under oppfolging %s", aktivitetData.getAktorId()));
        }
    }

    public void lagreOgProsseseserAktiviteter(KafkaAktivitetMelding aktivitetData) {
        aktiviteterRepositoryV2.tryLagreAktivitetData(aktivitetData);

        AktivitetStatus status = aktiviteterRepositoryV2.getAktivitetStatus(AktorId.of(aktivitetData.getAktorId()), aktivitetData.getAktivitetType());

        prossesertAktivitetRepositoryV2.upsertAktivitetTypeStatus(status, aktivitetData.getAktivitetType().name());
        brukerDataService.oppdaterAktivitetBrukerDataPostgres(AktorId.of(aktivitetData.getAktorId()));
    }

    public void utledOgIndekserAktivitetstatuserForAktoerid(AktorId aktoerId) {
        AktivitetBrukerOppdatering aktivitetBrukerOppdateringer = AktivitetUtils.hentAktivitetBrukerOppdateringer(aktoerId, brukerService, aktivitetDAO, erGR202PaKafka(unleashService));
        Optional.ofNullable(aktivitetBrukerOppdateringer)
                .ifPresent(oppdatering -> persistentOppdatering.lagreBrukeroppdateringerIDBogIndekser(oppdatering, aktoerId));
    }

    public void slettOgIndekserUtdanningsAktivitet(String aktivitetid, AktorId aktorId) {
        //ORACLE
        aktivitetDAO.deleteById(aktivitetid);
        utledOgIndekserAktivitetstatuserForAktoerid(aktorId);

        //POSTGRES
        aktiviteterRepositoryV2.deleteById(aktivitetid);
        AktivitetStatus status = aktiviteterRepositoryV2.getAktivitetStatus(aktorId, KafkaAktivitetMelding.AktivitetTypeData.UTDANNINGAKTIVITET);
        prossesertAktivitetRepositoryV2.upsertAktivitetTypeStatus(status, AktivitetTyper.utdanningaktivitet.name());
        brukerDataService.oppdaterAktivitetBrukerDataPostgres(aktorId);
    }

    public void upsertOgIndekserAktiviteter(KafkaAktivitetMelding melding) {
        //ORACLE
        aktivitetDAO.upsertAktivitet(melding);
        utledOgIndekserAktivitetstatuserForAktoerid(AktorId.of(melding.getAktorId()));

        //POSTGRES
        lagreOgProsseseserAktiviteter(melding);
    }

    @Override
    public boolean shouldRewind() {
        return rewind.get();
    }

    @Override
    public void setRewind(boolean rewind) {
        this.rewind.set(rewind);
    }

    private boolean skallIkkeOppdatereAktivitet(KafkaAktivitetMelding aktivitetData) {
        return !aktivitetData.isAvtalt();
    }

    public void deaktiverUtgatteUtdanningsAktivteter(AktorId aktorId) {
        AktoerAktiviteter utdanningsAktiviteter = aktivitetDAO.getAvtalteAktiviteterForAktoerid(aktorId);
        utdanningsAktiviteter.getAktiviteter()
                .stream()
                .filter(aktivitetDTO -> AktivitetTyperFraKafka.utdanningaktivitet.name().equals(aktivitetDTO.getAktivitetType()))
                .filter(aktivitetDTO -> aktivitetDTO.getTilDato().toLocalDateTime().isBefore(LocalDateTime.now()))
                .forEach(aktivitetDTO -> {
                            log.info("Deaktiverer utdaningsaktivitet: {}, med utløpsdato: {}, på aktorId: {}", aktivitetDTO.getAktivitetID(), aktivitetDTO.getTilDato(), aktorId);
                            aktivitetDAO.setAvtalt(aktivitetDTO.getAktivitetID(), false);
                        }
                );
    }
}
