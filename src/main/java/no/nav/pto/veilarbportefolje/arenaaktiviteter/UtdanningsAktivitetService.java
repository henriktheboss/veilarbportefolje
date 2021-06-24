package no.nav.pto.veilarbportefolje.arenaaktiviteter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.aktiviteter.*;
import no.nav.pto.veilarbportefolje.arenaaktiviteter.arenaDTO.UtdanningsAktivitetDTO;
import no.nav.pto.veilarbportefolje.arenaaktiviteter.arenaDTO.UtdanningsAktivitetInnhold;
import no.nav.pto.veilarbportefolje.domene.AktorClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.pto.veilarbportefolje.arenaaktiviteter.ArenaAktivitetUtils.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UtdanningsAktivitetService {
    private final AktivitetService aktivitetService;
    private final AktorClient aktorClient;
    private final ArenaHendelseRepository arenaHendelseRepository;

    public void behandleKafkaRecord(ConsumerRecord<String, UtdanningsAktivitetDTO> kafkaMelding) {
        UtdanningsAktivitetDTO melding = kafkaMelding.value();
        log.info(
                "Behandler kafka-melding med key {} og offset {} på topic {}",
                kafkaMelding.key(),
                kafkaMelding.offset(),
                kafkaMelding.topic()
        );
        behandleKafkaMelding(melding);
    }

    public void behandleKafkaMelding(UtdanningsAktivitetDTO kafkaMelding) {
        UtdanningsAktivitetInnhold innhold = getInnhold(kafkaMelding);
        if (innhold == null || erGammelMelding(kafkaMelding, innhold)) {
            return;
        }

        AktorId aktorId = getAktorId(aktorClient, innhold.getFnr());
        if (skalSlettesGoldenGate(kafkaMelding) || skalSletteUtdanningsAktivitet(innhold)) {
            log.info("Sletter aktivitet: {}", innhold.getAktivitetid());
            aktivitetService.slettOgIndekserAktivitet(innhold.getAktivitetid(), aktorId);
        } else {
            log.info("Lagrer aktivitet: {}", innhold.getAktivitetid());
            KafkaAktivitetMelding melding = mapTilKafkaAktivitetMelding(innhold, aktorId);
            aktivitetService.upsertOgIndekserAktiviteter(melding);
        }
        log.debug("Ferdig behandlet aktivitet: {}, pa aktor: {}, hendelse: {}", innhold.getAktivitetid(), aktorId, innhold.getHendelseId());
        arenaHendelseRepository.upsertHendelse(innhold.getAktivitetid(), innhold.getHendelseId());
    }

    static boolean skalSletteUtdanningsAktivitet(UtdanningsAktivitetInnhold utdanningsInnhold) {
        return utdanningsInnhold.getAktivitetperiodeTil() == null || utdanningsInnhold.getAktivitetperiodeFra() == null
                || erUtgatt(utdanningsInnhold.getAktivitetperiodeTil(), true);
    }

    /**
     * Har side effekt med a lagre hvilken arena meldinger som er lest i DB
     */
    private boolean erGammelMelding(UtdanningsAktivitetDTO kafkaMelding, UtdanningsAktivitetInnhold innhold) {
        Long hendelseIDB = arenaHendelseRepository.retrieveHendelse(innhold.getAktivitetid());

        if (erGammelHendelseBasertPaOperasjon(hendelseIDB, innhold.getHendelseId(), kafkaMelding.getOperationType())) {
            log.info("Fikk tilsendt gammel utdannings-aktivtet-melding hendelse: {}", innhold.getHendelseId());
            return true;
        }
        log.info("Fikk ny hendelse: {}", innhold.getHendelseId());
        return false;
    }

    private KafkaAktivitetMelding mapTilKafkaAktivitetMelding(UtdanningsAktivitetInnhold melding, AktorId aktorId) {
        if (melding == null || aktorId == null) {
            return null;
        }
        KafkaAktivitetMelding kafkaAktivitetMelding = new KafkaAktivitetMelding();
        kafkaAktivitetMelding.setAktorId(aktorId.get());
        kafkaAktivitetMelding.setAktivitetId(melding.getAktivitetid()); //TODO: Sjekk om denne er unik i forhold til de andre
        kafkaAktivitetMelding.setFraDato(getDateOrNull(melding.getAktivitetperiodeFra()));
        kafkaAktivitetMelding.setTilDato(getDateOrNull(melding.getAktivitetperiodeTil(), true));
        kafkaAktivitetMelding.setEndretDato(getDateOrNull(melding.getEndretDato()));

        kafkaAktivitetMelding.setAktivitetStatus(KafkaAktivitetMelding.AktivitetStatus.GJENNOMFORES);
        kafkaAktivitetMelding.setAktivitetType(KafkaAktivitetMelding.AktivitetTypeData.UTDANNINGAKTIVITET);
        kafkaAktivitetMelding.setAvtalt(true);
        kafkaAktivitetMelding.setHistorisk(false);
        kafkaAktivitetMelding.setVersion(-1L);

        return kafkaAktivitetMelding;
    }
}
