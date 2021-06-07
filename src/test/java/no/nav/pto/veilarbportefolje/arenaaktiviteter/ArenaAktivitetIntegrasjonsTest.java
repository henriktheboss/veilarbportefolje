package no.nav.pto.veilarbportefolje.arenaaktiviteter;

import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.aktiviteter.AktivitetService;
import no.nav.pto.veilarbportefolje.arenaaktiviteter.arenaDTO.GoldenGateDTO;
import no.nav.pto.veilarbportefolje.arenaaktiviteter.arenaDTO.GoldenGateOperations;
import no.nav.pto.veilarbportefolje.arenaaktiviteter.arenaDTO.UtdanningsAktivitetInnhold;
import no.nav.pto.veilarbportefolje.database.BrukerRepository;
import no.nav.pto.veilarbportefolje.database.Table;
import no.nav.pto.veilarbportefolje.domene.AktorClient;
import no.nav.pto.veilarbportefolje.domene.value.PersonId;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.util.EndToEndTest;
import no.nav.sbl.sql.SqlUtils;
import org.elasticsearch.action.get.GetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ArenaAktivitetIntegrasjonsTest extends EndToEndTest {
    private final UtdanningsAktivitetService utdanningsAktivitetService;
    private final JdbcTemplate jdbcTemplate;

    private final BrukerRepository brukerRepository;
    private final AktorId aktorId = AktorId.of("1000123");
    private final Fnr fnr = Fnr.of("12345678912");
    private final VeilederId veilederId = VeilederId.of("Z123456");
    private final EnhetId testEnhet = EnhetId.of("0000");
    private final PersonId personId = PersonId.of("123");

    @Autowired
    public ArenaAktivitetIntegrasjonsTest(AktivitetService aktivitetService, JdbcTemplate jdbcTemplate, BrukerRepository brukerRepository) {
        this.brukerRepository = brukerRepository;
        AktorClient aktorClient = Mockito.mock(AktorClient.class);
        Mockito.when(aktorClient.hentAktorId(fnr)).thenReturn(aktorId);
        this.jdbcTemplate = jdbcTemplate;

        ArenaAktivitetService arenaAktivitetService = new ArenaAktivitetService(aktorClient);
        this.utdanningsAktivitetService = new UtdanningsAktivitetService(aktivitetService, arenaAktivitetService);
    }

    @BeforeEach
    public void resetOgInsert() {
        jdbcTemplate.execute("truncate table " + Table.OPPFOLGINGSBRUKER.TABLE_NAME);
        jdbcTemplate.execute("truncate table " + Table.OPPFOLGING_DATA.TABLE_NAME);
        jdbcTemplate.execute("truncate table " + Table.AKTOERID_TO_PERSONID.TABLE_NAME);

    }

    @Test
    public void skal_komme_i_aktivitet() {
        insertBruker();
        utdanningsAktivitetService.behandleKafkaMelding(
                new GoldenGateDTO<UtdanningsAktivitetInnhold>()
                        .setOperationType(GoldenGateOperations.INSERT)
                        .setAfter(
                        new UtdanningsAktivitetInnhold()
                                .setFnr(fnr.get())
                                .setAktivitetperiodeFra(new Date(120, Calendar.JANUARY, 1)) //2020-01-01
                                .setAktivitetperiodeTil(new Date(130, Calendar.JANUARY, 1)) //2030-01-01
                                .setEndretDato(new Date())
                                .setAktivitetid("UA-123456789")
                )
        );

        GetResponse response = elasticTestClient.fetchDocument(aktorId);
        assertThat(response.isExists()).isTrue();

        Object nestedObject = response.getSourceAsMap().get("aktiviteter");
        assertThat(nestedObject).isNotNull();
    }


    @Test
    public void skal_ut_av_aktivitet() {
        insertBruker();
        utdanningsAktivitetService.behandleKafkaMelding(
                new GoldenGateDTO<UtdanningsAktivitetInnhold>()
                        .setOperationType(GoldenGateOperations.INSERT)
                        .setAfter(
                                new UtdanningsAktivitetInnhold()
                                        .setFnr(fnr.get())
                                        .setAktivitetperiodeFra(new Date(120, Calendar.JANUARY, 1)) //2020-01-01
                                        .setAktivitetperiodeTil(new Date(130, Calendar.JANUARY, 1)) //2030-01-01
                                        .setEndretDato(new Date())
                                        .setAktivitetid("UA-123456789")
                        )
        );

        utdanningsAktivitetService.behandleKafkaMelding(
                new GoldenGateDTO<UtdanningsAktivitetInnhold>()
                        .setOperationType(GoldenGateOperations.DELETE)
                        .setBefore(
                                new UtdanningsAktivitetInnhold()
                                        .setFnr(fnr.get())
                                        .setAktivitetperiodeFra(new Date(120, Calendar.JANUARY, 1)) //2020-01-01
                                        .setAktivitetperiodeTil(new Date(130, Calendar.JANUARY, 1)) //2030-01-01
                                        .setEndretDato(new Date())
                                        .setAktivitetid("UA-123456789")
                        )
        );

        GetResponse response = elasticTestClient.fetchDocument(aktorId);
        assertThat(response.isExists()).isTrue();

        List<String> nestedObject = (List<String>) response.getSourceAsMap().get("aktiviteter");
        assertThat(nestedObject.isEmpty()).isTrue();
    }


    private void insertBruker(){
        populateElastic(testEnhet, veilederId, aktorId.get());
        SqlUtils.insert(jdbcTemplate, Table.OPPFOLGINGSBRUKER.TABLE_NAME)
                .value(Table.OPPFOLGINGSBRUKER.FODSELSNR, fnr.toString())
                .value(Table.OPPFOLGINGSBRUKER.NAV_KONTOR, testEnhet.toString())
                .value(Table.OPPFOLGINGSBRUKER.PERSON_ID, personId.toString())
                .execute();
        SqlUtils.insert(jdbcTemplate, Table.OPPFOLGING_DATA.TABLE_NAME)
                .value(Table.OPPFOLGING_DATA.AKTOERID, aktorId.toString())
                .value(Table.OPPFOLGING_DATA.OPPFOLGING, "J")
                .value(Table.OPPFOLGING_DATA.VEILEDERIDENT, veilederId.toString())
                .execute();
        SqlUtils.insert(jdbcTemplate, Table.AKTOERID_TO_PERSONID.TABLE_NAME)
                .value(Table.AKTOERID_TO_PERSONID.AKTOERID, aktorId.toString())
                .value(Table.AKTOERID_TO_PERSONID.PERSONID, personId.toString())
                .value(Table.AKTOERID_TO_PERSONID.GJELDENE, 1)
                .execute();

    }
}
