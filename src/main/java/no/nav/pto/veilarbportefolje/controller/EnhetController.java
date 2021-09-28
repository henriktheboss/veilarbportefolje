package no.nav.pto.veilarbportefolje.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto.veilarbportefolje.arenapakafka.aktiviteter.TiltakServiceV2;
import no.nav.pto.veilarbportefolje.arenafiler.gr202.tiltak.TiltakService;
import no.nav.pto.veilarbportefolje.auth.AuthService;
import no.nav.pto.veilarbportefolje.auth.AuthUtils;
import no.nav.pto.veilarbportefolje.domene.*;
import no.nav.pto.veilarbportefolje.elastic.ElasticService;
import no.nav.pto.veilarbportefolje.postgres.PostgresService;
import no.nav.pto.veilarbportefolje.service.UnleashService;
import no.nav.pto.veilarbportefolje.util.PortefoljeUtils;
import no.nav.pto.veilarbportefolje.util.ValideringsRegler;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static no.nav.pto.veilarbportefolje.config.FeatureToggle.erGR202PaKafka;
import static no.nav.pto.veilarbportefolje.config.FeatureToggle.erPostgresPa;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enhet")
public class EnhetController {
    private final ElasticService elasticService;
    private final PostgresService postgresService;
    private final AuthService authService;
    private final TiltakService tiltakService;
    private final TiltakServiceV2 tiltakServiceV2;
    private final MetricsClient metricsClient;
    private final UnleashService unleashService;

    @PostMapping("/{enhet}/portefolje")
    public Portefolje hentPortefoljeForEnhet(
            @PathVariable("enhet") String enhet,
            @RequestParam(value = "fra", required = false) Integer fra,
            @RequestParam(value = "antall", required = false) Integer antall,
            @RequestParam("sortDirection") String sortDirection,
            @RequestParam("sortField") String sortField,
            @RequestBody Filtervalg filtervalg) {

        ValideringsRegler.sjekkEnhet(enhet);
        ValideringsRegler.sjekkSortering(sortDirection, sortField);
        ValideringsRegler.sjekkFiltervalg(filtervalg);
        authService.tilgangTilOppfolging();
        authService.tilgangTilEnhet(enhet);

        String ident = AuthUtils.getInnloggetVeilederIdent().toString();
        String identHash = DigestUtils.md5Hex(ident).toUpperCase();

        BrukereMedAntall brukereMedAntall;
        if (erPostgresPa(unleashService, ident)) {
            brukereMedAntall = postgresService.hentBrukere(enhet, null, sortDirection, sortField, filtervalg, fra, antall);
        } else {
            brukereMedAntall = elasticService.hentBrukere(enhet, Optional.empty(), sortDirection, sortField, filtervalg, fra, antall);
        }
        List<Bruker> sensurerteBrukereSublist = authService.sensurerBrukere(brukereMedAntall.getBrukere());

        Portefolje portefolje = PortefoljeUtils.buildPortefolje(brukereMedAntall.getAntall(),
                sensurerteBrukereSublist,
                enhet,
                Optional.ofNullable(fra).orElse(0));

        Event event = new Event("enhetsportefolje.lastet");
        event.addFieldToReport("identhash", identHash);
        metricsClient.report(event);

        return portefolje;
    }


    @GetMapping("/{enhet}/portefoljestorrelser")
    public FacetResults hentPortefoljestorrelser(@PathVariable("enhet") String enhet) {
        ValideringsRegler.sjekkEnhet(enhet);
        authService.tilgangTilEnhet(enhet);

        return elasticService.hentPortefoljestorrelser(enhet);
    }

    @GetMapping("/{enhet}/statustall")
    public StatusTall hentStatusTall(@PathVariable("enhet") String enhet) {
        ValideringsRegler.sjekkEnhet(enhet);
        authService.tilgangTilEnhet(enhet);

        return elasticService.hentStatusTallForEnhet(enhet);

    }

    @GetMapping("/{enhet}/tiltak")
    public EnhetTiltak hentTiltak(@PathVariable("enhet") String enhet) {
        ValideringsRegler.sjekkEnhet(enhet);
        authService.tilgangTilEnhet(enhet);
        if (erGR202PaKafka(unleashService)) {
            return tiltakServiceV2.hentEnhettiltak(EnhetId.of(enhet));
        }
        return tiltakService.hentEnhettiltak(enhet)
                .getOrElse(new EnhetTiltak());
    }
}
