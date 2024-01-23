package no.nav.pto.veilarbportefolje.fargekategori;

import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.auth.AuthUtils;
import no.nav.pto.veilarbportefolje.config.ApplicationConfigTest;
import no.nav.pto.veilarbportefolje.domene.value.NavKontor;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.fargekategori.FargekategoriController.*;
import no.nav.pto.veilarbportefolje.util.TestDataClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.common.json.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FargekategoriController.class)
@Import(ApplicationConfigTest.class)
public class FargekategoriControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @Autowired
    protected TestDataClient testDataClient;


    @Test
    void test_opprett_og_hent_fargekategori_for_bruker() throws Exception {
        Fnr fnr = Fnr.of("10987654321");
        EnhetId enhetId = EnhetId.of("1234");
        AktorId aktorId = AktorId.of("99988877766655");
        VeilederId veilederId = AuthUtils.getInnloggetVeilederIdent();
        testDataClient.lagreBrukerUnderOppfolging(aktorId, fnr, NavKontor.of(enhetId.get()), veilederId);
        FargekategoriVerdi fargekategoriVerdi = FargekategoriVerdi.GUL;

        OpprettFargekategoriRequest opprettRequest = new OpprettFargekategoriRequest(fnr, fargekategoriVerdi);
        String opprettetFargekategoriId = mockMvc.perform(
                        post("/api/fargekategori")
                                .contentType(APPLICATION_JSON)
                                .content(toJson(opprettRequest))
                )
//                .andExpect(status().is(201)) // Det vi faktisk vil ha til slutt
                .andExpect(status().is(405))
                .andReturn().getResponse().getContentAsString();

        HentFargekategoriRequest hentFargekategoriForBrukerRequest = new HentFargekategoriRequest(fnr);
        HentFargekategoriResponse expected = new HentFargekategoriResponse(
                opprettetFargekategoriId,
                fnr,
                fargekategoriVerdi,
                LocalDateTime.now(),
                veilederId.getValue()
        );

        String hentFargekategoriResult = mockMvc.perform(
                        post("/api/hent-fargekategori")
                                .contentType(APPLICATION_JSON)
                                .content(toJson(hentFargekategoriForBrukerRequest))
                )
//                .andExpect(status().is(200))
                .andExpect(status().is(405))
                .andReturn().getResponse().getContentAsString();

        HentFargekategoriResponse hentetFargekategoriBody = fromJson(hentFargekategoriResult, HentFargekategoriResponse.class);

        assertThat(hentetFargekategoriBody.fargekategoriId()).isEqualTo(expected.fargekategoriId());
        assertThat(hentetFargekategoriBody.brukerFnr()).isEqualTo(expected.brukerFnr());
        assertThat(hentetFargekategoriBody.fargekategoriVerdi()).isEqualTo(expected.fargekategoriVerdi());
        assertThat(hentetFargekategoriBody.endretDato()).isEqualTo(expected.endretDato());
        assertThat(hentetFargekategoriBody.endretAv()).isEqualTo(expected.endretAv());
    }

    @Test
    void test_oppdatering_av_fargekategori() throws Exception {
        OppdaterFargekategoriRequest oppdaterRequest = new OppdaterFargekategoriRequest();
        mockMvc.perform(
                        put("/api/fargekategori")
                                .contentType(APPLICATION_JSON)
                                .content(toJson(oppdaterRequest))
                )
                .andExpect(status().is(405));
    }

    @Test
    void test_sletting_av_fargekategori() throws Exception {
        SlettFargekategoriRequest slettRequest = new SlettFargekategoriRequest();
        mockMvc.perform(
                        put("/api/fargekategori")
                                .contentType(APPLICATION_JSON)
                                .content(toJson(slettRequest))
                )
                .andExpect(status().is(405));
    }
}
