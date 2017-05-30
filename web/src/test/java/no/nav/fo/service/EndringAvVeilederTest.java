package no.nav.fo.service;

import com.google.common.base.Joiner;
import no.nav.fo.config.ApplicationConfigTest;
import no.nav.fo.database.BrukerRepository;
import no.nav.fo.database.BrukerRepositoryTest;
import no.nav.fo.domene.BrukerOppdatertInformasjon;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdResponse;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Timestamp;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfigTest.class})
public class EndringAvVeilederTest {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    AktoerV2 aktoerV2;

    @Inject
    BrukerRepository brukerRepository;

    @Inject
    OppdaterBrukerdataFletter oppdaterBrukerdataFletter;

    @Before
    public void setUp() {
        try {
            jdbcTemplate.execute(Joiner.on("\n").join(IOUtils.readLines(BrukerRepositoryTest.class.getResourceAsStream("/create-table-aktoerid-to-personid-mapping.sql"))));
            jdbcTemplate.execute(Joiner.on("\n").join(IOUtils.readLines(BrukerRepositoryTest.class.getResourceAsStream("/create-table-bruker-data.sql"))));
            jdbcTemplate.execute(Joiner.on("\n").join(IOUtils.readLines(BrukerRepositoryTest.class.getResourceAsStream("/create-table-oppfolgingsbruker.sql"))));
            jdbcTemplate.execute(Joiner.on("\n").join(IOUtils.readLines(BrukerRepositoryTest.class.getResourceAsStream("/insert-aktoerid-to-personid-testdata.sql"))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        reset(aktoerV2);
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("drop table oppfolgingsbruker");
        jdbcTemplate.execute("drop table bruker_data");
        jdbcTemplate.execute("drop table AKTOERID_TO_PERSONID");
    }


    @Test
    public void skalHentePersonidFraDBogIkkeHenteFraAktoerV2() {
        BrukerOppdatertInformasjon bruker = new BrukerOppdatertInformasjon()
                .setAktoerid("11111111")
                .setVeileder("X111111")
                .setEndretTimestamp(Timestamp.valueOf("2017-01-14 13:33:16.000000"));

        oppdaterBrukerdataFletter.tilordneVeilederTilPersonId(bruker);
        try {
            verify(aktoerV2, never()).hentIdentForAktoerId(any(WSHentIdentForAktoerIdRequest.class));
        } catch (HentIdentForAktoerIdPersonIkkeFunnet hentIdentForAktoerIdPersonIkkeFunnet) {
            hentIdentForAktoerIdPersonIkkeFunnet.printStackTrace();
        }
        String personid = (String) brukerRepository.retrieveBruker("11111111").get(0).get("PERSONID");
        assertThat(personid).isEqualTo("222222");
    }

    @Test
    public void skalHenteFraAktoerLagrePersonidIDBogOppdatererBrukerdata() {
        updateDBWithPersonidbruker("156156","10000000065");

        WSHentIdentForAktoerIdResponse response = new WSHentIdentForAktoerIdResponse().withIdent("10000000065");

        try {
            when(aktoerV2.hentIdentForAktoerId(any(WSHentIdentForAktoerIdRequest.class))).thenReturn(response);
        } catch (HentIdentForAktoerIdPersonIkkeFunnet hentIdentForAktoerIdPersonIkkeFunnet) {
            hentIdentForAktoerIdPersonIkkeFunnet.printStackTrace();
        }


        BrukerOppdatertInformasjon bruker = new BrukerOppdatertInformasjon()
                .setAktoerid("22222222")
                .setVeileder("X111111")
                .setEndretTimestamp(Timestamp.valueOf("2017-01-14 13:33:16.000000"));

        oppdaterBrukerdataFletter.tilordneVeilederTilPersonId(bruker);

        try {
            verify(aktoerV2, times(1)).hentIdentForAktoerId(any(WSHentIdentForAktoerIdRequest.class));
        } catch (HentIdentForAktoerIdPersonIkkeFunnet hentIdentForAktoerIdPersonIkkeFunnet) {
            hentIdentForAktoerIdPersonIkkeFunnet.printStackTrace();
        }

        String veileder = (String) brukerRepository.retrieveBruker("22222222").get(0).get("VEILEDERIDENT");
        String personid = (String) brukerRepository.retrievePersonid("22222222").get(0).get("PERSONID");

        assertThat(personid).isEqualTo("156156");
        assertThat(veileder).isEqualTo("X111111");
    }

    private void updateDBWithPersonidbruker(String personid, String fnr) {
        jdbcTemplate.execute("INSERT INTO OPPFOLGINGSBRUKER (PERSON_ID, FODSELSNR, ETTERNAVN, FORNAVN, NAV_KONTOR, FORMIDLINGSGRUPPEKODE, " +
                "ISERV_FRA_DATO, KVALIFISERINGSGRUPPEKODE, RETTIGHETSGRUPPEKODE, HOVEDMAALKODE, SIKKERHETSTILTAK_TYPE_KODE, FR_KODE, " +
                "SPERRET_ANSATT, ER_DOED, DOED_FRA_DATO, TIDSSTEMPEL) VALUES " +
                "("+personid+", '"+fnr+"', 'GAASEN', 'GUNNAR', '0713', 'ARBS', null, 'BATT', 'IYT', 'SKAFFEA', 'TOAN', '7', 'J', 'N', null, " +
                "TO_TIMESTAMP('2017-01-13 14:59:29.000000', 'YYYY-MM-DD HH24:MI:SS.FF'));\n");
    }

}
