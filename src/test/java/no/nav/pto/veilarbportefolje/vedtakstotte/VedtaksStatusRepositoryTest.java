package no.nav.pto.veilarbportefolje.vedtakstotte;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.pto.veilarbportefolje.config.LocalJndiContextConfig.setupInMemoryDatabase;

public class VedtaksStatusRepositoryTest {

    private VedtakStatusRepository vedtakStatusRepository;

    private static String AKTORID = "123456789";
    private static long VEDTAKID = 1;

    @Before
    public void setup() {
        JdbcTemplate db = new JdbcTemplate(setupInMemoryDatabase());
        this.vedtakStatusRepository = new VedtakStatusRepository(db);
        vedtakStatusRepository.slettGamleVedtakOgUtkast(AKTORID);
    }


    @Test
    public void skallSetteInVedtak() {
        insertVedtakIDB();
        List<VedtakStatusEndring> endringer = vedtakStatusRepository.hentVedtak(AKTORID);
        assertThat(endringer.size()).isEqualTo(1);
    }

    @Test
    public void skallOppdatereVedtak() {
        insertVedtakIDB();

        LocalDateTime time = LocalDateTime.now();
        VedtakStatusEndring vedtakStatusEndring = new VedtakStatusEndring()
                .setVedtakStatus(VedtakStatusEndring.KafkaVedtakStatus.SENDT_TIL_BESLUTTER)
                .setHovedmal(VedtakStatusEndring.Hovedmal.BEHOLDE_ARBEID)
                .setInnsatsgruppe(VedtakStatusEndring.Innsatsgruppe.STANDARD_INNSATS)
                .setStatusEndretTidspunkt(time)
                .setAktorId(AKTORID)
                .setVedtakId(VEDTAKID);

        vedtakStatusRepository.upsertVedtak(vedtakStatusEndring);
        List<VedtakStatusEndring> endringer = vedtakStatusRepository.hentVedtak(AKTORID);
        assertThat(endringer.size()).isEqualTo(1);
    }


    private void insertVedtakIDB() {
        LocalDateTime time = LocalDateTime.now();
        VedtakStatusEndring vedtakStatusEndring = new VedtakStatusEndring()
                .setVedtakStatus(VedtakStatusEndring.KafkaVedtakStatus.UTKAST_OPPRETTET)
                .setStatusEndretTidspunkt(time)
                .setAktorId(AKTORID)
                .setVedtakId(VEDTAKID)
                .setHovedmal(null)
                .setInnsatsgruppe(null);
        vedtakStatusRepository.upsertVedtak(vedtakStatusEndring);
    }



}
