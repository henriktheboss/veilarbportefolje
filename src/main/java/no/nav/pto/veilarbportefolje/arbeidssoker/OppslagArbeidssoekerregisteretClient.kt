package no.nav.pto.veilarbportefolje.arbeidssoker

import jakarta.ws.rs.core.HttpHeaders
import no.nav.common.rest.client.RestUtils
import no.nav.common.types.identer.Fnr
import no.nav.common.utils.UrlUtils
import no.nav.pto.veilarbportefolje.oppfolgingsbruker.HentOppfolgingsbrukerRequest
import no.nav.pto.veilarbportefolje.util.deserializeJsonOrThrow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import java.util.function.Supplier

class OppslagArbeidssoekerregisteretClient(
    private val url: String,
    private val tokenSupplier: Supplier<String>,
    private val client: OkHttpClient,
    private val consumerId: String
) {

    fun hentArbeidssokerPerioder(fnr: Fnr): Optional<List<ArbeidssokerPeriodeDTO>> {
        val request: Request = Request.Builder()
            .url(UrlUtils.joinPaths(url, "/api/v1/veileder/arbeidssoekerperioder"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenSupplier.get()}")
            .header("Nav-Consumer-Id", consumerId)
            .post(RestUtils.toJsonRequestBody(HentArbeidssoekerPerioderRequest(fnr.get())))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty()
            }

            RestUtils.throwIfNotSuccessful(response)

            return Optional.ofNullable(
                response.deserializeJsonOrThrow()
            )
        }
    }
}

data class HentArbeidssoekerPerioderRequest(val identitetsnummer: String)

data class ArbeidssokerPeriodeDTO(
    val periodeId: UUID,
    val metadata: Metadata,
    val avsluttet: Metadata
)

data class Metadata(
    val tidspunkt: LocalDateTime,
    val utfoertAv: UtfoertAv,
    val kilde: String,
    val aarsak: String
)

data class UtfoertAv(
    val type: String // TODO enum
)