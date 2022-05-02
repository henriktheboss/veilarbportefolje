package no.nav.pto.veilarbportefolje.persononinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.SneakyThrows;
import no.nav.common.client.pdl.PdlClientImpl;
import no.nav.pto.veilarbportefolje.config.ApplicationConfigTest;
import no.nav.pto.veilarbportefolje.persononinfo.PdlResponses.PDLIdent;
import no.nav.pto.veilarbportefolje.persononinfo.PdlResponses.PdlIdentRespons;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static no.nav.pto.veilarbportefolje.util.TestDataUtils.randomAktorId;
import static no.nav.pto.veilarbportefolje.util.TestUtil.readFileAsJsonString;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApplicationConfigTest.class)
public class PdlServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String pdlResponsFraFil = readFileAsJsonString("/identer_pdl.json", getClass());
    private final JdbcTemplate db;
    private PdlService pdlService;

    @Autowired
    public PdlServiceTest(@Qualifier("PostgresJdbc") JdbcTemplate db){
        this.db = db;
    }

    @BeforeEach
    public void setup() {
        WireMockServer server = new WireMockServer();
        db.update("truncate bruker_identer");

        server.stubFor(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(pdlResponsFraFil))
        );
        server.start();

        this.pdlService = new PdlService(
                new PdlRepository(db),
                new PdlClientImpl("http://localhost:" + server.port(), () -> "SYSTEM", () -> "SYSTEM")
        );
    }

    @Test
    @SneakyThrows
    public void lagreIdenterFraPdl() {
        var identerFraFil = mapper.readValue(pdlResponsFraFil, PdlIdentRespons.class)
                .getData()
                .getHentIdenter()
                .getIdenter();

        pdlService.hentOgLagreIdenter(randomAktorId());
        List<PDLIdent> identerFraPostgres = db.queryForList("select * from bruker_identer")
                .stream()
                .map(PdlRepository::mapTilident)
                .toList();

        assertThat(identerFraPostgres).containsExactlyInAnyOrderElementsOf(identerFraFil);
    }
}