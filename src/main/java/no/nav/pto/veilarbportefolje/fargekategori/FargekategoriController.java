package no.nav.pto.veilarbportefolje.fargekategori;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.auth.AuthService;
import no.nav.pto.veilarbportefolje.domene.RestResponse;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FargekategoriController {
    private final AuthService authService;
    //private final FargekategoriService fargekategoriService;


    @PostMapping("/hent-fargekategori")
    public RestResponse hentFargekategori(@RequestBody HentFargekategoriRequest request) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping("/fargekategori")
    public RestResponse opprettFargekategori(@RequestBody OpprettFargekategoriRequest request) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PutMapping("/fargekategori")
    public RestResponse oppdaterFargekategori(@RequestBody OppdaterFargekategoriRequest request) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping("/fargekategori")
    public RestResponse slettFargekategori(@RequestBody SlettFargekategoriRequest request) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
    }


    public record HentFargekategoriRequest(Fnr fnr) {}

    public record OpprettFargekategoriRequest(
            Fnr fnr,
            EnhetId enhetId,
            AktorId aktorId,
            VeilederId veilederId,
            FargekategoriVerdi fargekategoriVerdi) {}

    public record OppdaterFargekategoriRequest() {}

    public record SlettFargekategoriRequest() {}

    public record OpprettFargekategoriResponse(
            String fargekategoriId,
            Fnr brukerFnr,
            EnhetId enhetId,
            FargekategoriVerdi fargekategoriVerdi,
            @JsonDeserialize(using = LocalDateDeserializer.class)
            LocalDate endretDato,
            String endretAv
    ) {}
}
