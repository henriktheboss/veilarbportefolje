package no.nav.pto.veilarbportefolje.fargekategori;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbportefolje.auth.AuthService;
import no.nav.pto.veilarbportefolje.domene.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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


    public record HentFargekategoriRequest() {

    }

    public record OpprettFargekategoriRequest() {

    }

    public record OppdaterFargekategoriRequest() {

    }

    public record SlettFargekategoriRequest() {

    }
}
