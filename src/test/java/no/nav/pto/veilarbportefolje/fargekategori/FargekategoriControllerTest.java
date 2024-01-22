package no.nav.pto.veilarbportefolje.fargekategori;

import no.nav.pto.veilarbportefolje.config.ApplicationConfigTest;
import no.nav.pto.veilarbportefolje.fargekategori.FargekategoriController.HentFargekategoriRequest;
import no.nav.pto.veilarbportefolje.fargekategori.FargekategoriController.OppdaterFargekategoriRequest;
import no.nav.pto.veilarbportefolje.fargekategori.FargekategoriController.OpprettFargekategoriRequest;
import no.nav.pto.veilarbportefolje.fargekategori.FargekategoriController.SlettFargekategoriRequest;
import no.nav.pto.veilarbportefolje.util.TestDataClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static no.nav.common.json.JsonUtils.toJson;
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
    void test_at_endepunkt_returnerer_fargekategori_for_bruker() throws Exception {
        HentFargekategoriRequest hentRequest = new HentFargekategoriRequest();
        mockMvc.perform(
                        post("/api/hent-fargekategori")
                                .contentType(APPLICATION_JSON)
                                .content(toJson(hentRequest))
                )
                .andExpect(status().is(405));
    }

    @Test
    void test_endepunkt_for_oppretting_av_fargekategori() throws Exception {
        OpprettFargekategoriRequest opprettRequest = new OpprettFargekategoriRequest();
        mockMvc.perform(
                        post("/api/fargekategori")
                                .contentType(APPLICATION_JSON)
                                .content(toJson(opprettRequest))
                )
                .andExpect(status().is(405));
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
