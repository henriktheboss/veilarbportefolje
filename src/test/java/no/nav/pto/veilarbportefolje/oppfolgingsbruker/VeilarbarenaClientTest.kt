package no.nav.pto.veilarbportefolje.oppfolgingsbruker

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import no.nav.common.rest.client.RestClient
import no.nav.common.types.identer.Fnr
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.function.Supplier

class VeilarbarenaClientTest {

    @JvmField
    @Rule
    val wireMockRule: WireMockRule = WireMockRule(0)

    @Test
    fun hentOppfolgingsbruker_gir_forventet_respons() {
        val fnr = Fnr.of("123")

        val client = VeilarbarenaClient(
            "http://localhost:" + wireMockRule.port(),
            { "TOKEN" },
            RestClient.baseClient()
        )

        val responseBody = """
                    {
                      "fodselsnr": "17858998980",
                      "formidlingsgruppekode": "ARBS",
                      "iserv_fra_dato": null,
                      "nav_kontor": "0220",
                      "kvalifiseringsgruppekode": "BATT",
                      "rettighetsgruppekode": "INDS",
                      "hovedmaalkode": "SKAFFEA",
                      "sikkerhetstiltak_type_kode": null,
                      "fr_kode": null,
                      "har_oppfolgingssak": true,
                      "sperret_ansatt": false,
                      "er_doed": false,
                      "doed_fra_dato": null
                    }
                """.trimIndent()

        WireMock.givenThat(
            WireMock.post(WireMock.urlEqualTo("/api/v2/hent-oppfolgingsbruker")).withRequestBody(
                WireMock.equalToJson(
                    "{\"fnr\":\"$fnr\"}"
                )
            ).willReturn(WireMock.aResponse().withStatus(200).withBody(responseBody))
        )

        val response = client.hentOppfolgingsbruker(fnr)

        val forventet = OppfolgingsbrukerDTO(
            fodselsnr = "17858998980",
            formidlingsgruppekode = "ARBS",
            navKontor = "0220",
            kvalifiseringsgruppekode = "BATT",
            rettighetsgruppekode = "INDS",
            hovedmaalkode = "SKAFFEA",
            sikkerhetstiltakTypeKode = null,
            frKode = null,
            harOppfolgingssak = true,
            sperretAnsatt = false,
            erDoed = false,
            doedFraDato = null
        )

        Assertions.assertThat(response).isEqualTo(Optional.of(forventet))
    }
}