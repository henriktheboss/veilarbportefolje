package no.nav.pto.veilarbportefolje.admin;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.common.types.identer.Id;
import no.nav.pto.veilarbportefolje.arbeidsliste.ArbeidslisteService;
import no.nav.pto.veilarbportefolje.arenapakafka.ytelser.YtelsesService;
import no.nav.pto.veilarbportefolje.config.EnvironmentProperties;
import no.nav.pto.veilarbportefolje.database.BrukerRepository;
import no.nav.pto.veilarbportefolje.domene.AktorClient;
import no.nav.pto.veilarbportefolje.opensearch.OpensearchAdminService;
import no.nav.pto.veilarbportefolje.opensearch.OpensearchIndexer;
import no.nav.pto.veilarbportefolje.opensearch.OpensearchIndexerV2;
import no.nav.pto.veilarbportefolje.opensearch.domene.OppfolgingsBruker;
import no.nav.pto.veilarbportefolje.oppfolging.OppfolgingAvsluttetService;
import no.nav.pto.veilarbportefolje.oppfolging.OppfolgingRepository;
import no.nav.pto.veilarbportefolje.oppfolging.OppfolgingService;
import no.nav.pto.veilarbportefolje.postgres.opensearch.PostgresOpensearchMapper;
import no.nav.pto.veilarbportefolje.profilering.ProfileringService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final EnvironmentProperties environmentProperties;
    private final AktorClient aktorClient;
    private final OppfolgingAvsluttetService oppfolgingAvsluttetService;
    private final OpensearchIndexer opensearchIndexer;
    private final OpensearchIndexerV2 opensearchIndexerV2;
    private final OppfolgingService oppfolgingService;
    private final AuthContextHolder authContextHolder;
    private final YtelsesService ytelsesService;
    private final OppfolgingRepository oppfolgingRepository;
    private final ArbeidslisteService arbeidslisteService;
    private final ProfileringService profileringService;
    private final OpensearchAdminService opensearchAdminService;
    private final PostgresOpensearchMapper postgresOpensearchMapper;
    private final BrukerRepository brukerRepository;

    @PostMapping("/aktoerId")
    public String aktoerId(@RequestBody String fnr) {
        authorizeAdmin();
        return aktorClient.hentAktorId(Fnr.ofValidFnr(fnr)).get();
    }

    @DeleteMapping("/oppfolgingsbruker")
    public String slettOppfolgingsbruker(@RequestBody String aktoerId) {
        authorizeAdmin();
        oppfolgingAvsluttetService.avsluttOppfolging(AktorId.of(aktoerId));
        return "Slettet oppfølgingsbruker " + aktoerId;
    }

    @DeleteMapping("/fjernBrukerOpensearch")
    @SneakyThrows
    public String fjernBrukerFraOpensearch(@RequestBody String aktoerId) {
        authorizeAdmin();
        opensearchIndexerV2.slettDokumenter(List.of(AktorId.of(aktoerId)));
        return "Slettet bruker fra opensearch " + aktoerId;
    }


    @PostMapping("/lastInnOppfolging")
    public String lastInnOppfolgingsData() {
        authorizeAdmin();
        oppfolgingService.lastInnDataPaNytt();
        return "Innlastning av oppfolgingsdata har startet";
    }

    @PostMapping("/lastInnOppfolgingForBruker")
    public String lastInnOppfolgingsDataForBruker(@RequestBody String fnr) {
        authorizeAdmin();
        String aktorId = aktorClient.hentAktorId(Fnr.ofValidFnr(fnr)).get();
        oppfolgingService.oppdaterBruker(AktorId.of(aktorId));
        return "Innlastning av oppfolgingsdata har startet";
    }

    @PutMapping("/indeks/bruker")
    public String indeks(@RequestBody String fnr) {

        authorizeAdmin();
        String aktorId = aktorClient.hentAktorId(Fnr.ofValidFnr(fnr)).get();
        opensearchIndexer.indekser(AktorId.of(aktorId));
        return "Indeksering fullfort";
    }

    @PostMapping("/indeks/AlleBrukere")
    public String indekserAlleBrukere() {
        authorizeAdmin();
        List<AktorId> brukereUnderOppfolging = oppfolgingRepository.hentAlleGyldigeBrukereUnderOppfolging();
        opensearchIndexer.oppdaterAlleBrukereIOpensearch(brukereUnderOppfolging);
        return "Indeksering fullfort";
    }

    @PutMapping("/ytelser/allUsers")
    public String syncYtelserForAlle() {
        authorizeAdmin();
        ytelsesService.syncYtelserForAlleBrukere();
        return "Aktiviteter er nå i sync";
    }

    @PutMapping("/ytelser/idag")
    public String syncYtelserForIDag() {
        authorizeAdmin();
        ytelsesService.oppdaterBrukereMedYtelserSomStarterIDagOracle();
        return "Aktiviteter er nå i sync";
    }

    @PutMapping("/arbeidslista/migrer")
    public String migrerArbeidslista() {
        authorizeAdmin();
        arbeidslisteService.migrerArbeidslistaTilPostgres();
        return "Arbeidslista er nå migrert";
    }

    @PutMapping("/profilering/migrer")
    public String migrerProfilering() {
        authorizeAdmin();
        profileringService.migrerTilPostgres();
        return "Profilering er nå migrert";
    }

    @PostMapping("/opensearch/createIndex")
    public String createIndex() {
        authorizeAdmin();
        String indexName = opensearchAdminService.opprettNyIndeks();
        log.info("Opprettet index: {}", indexName);
        return indexName;
    }

    @GetMapping("/opensearch/getAliases")
    public String getAliases() {
        authorizeAdmin();
        return opensearchAdminService.hentAliaser();
    }

    @PostMapping("/opensearch/deleteIndex")
    public boolean deleteIndex(@RequestBody String indexName) {
        authorizeAdmin();
        log.info("Sletter index: {}", indexName);
        return opensearchAdminService.slettIndex(indexName);
    }

    @PostMapping("/opensearch/assignAliasToIndex")
    public String assignAliasToIndex(@RequestBody String indexName) {
        authorizeAdmin();
        opensearchAdminService.opprettAliasForIndeks(indexName);
        return "Ok";
    }

    @PostMapping("/opensearch/getSettings")
    public String getSettings(@RequestBody String indexName) {
        authorizeAdmin();
        return opensearchAdminService.getSettingsOnIndex(indexName);
    }

    @PostMapping("/opensearch/fixReadOnlyMode")
    public String fixReadOnlyMode() {
        authorizeAdmin();
        return opensearchAdminService.updateFromReadOnlyMode();
    }

    @PostMapping("/opensearch/forceShardAssignment")
    public String forceShardAssignment() {
        authorizeAdmin();
        return opensearchAdminService.forceShardAssignment();
    }

    @PostMapping("/test/postgresIndeksering")
    public void testHentUnderOppfolging() {
        authorizeAdmin();
        List<AktorId> brukereUnderOppfolging = oppfolgingRepository.hentAlleGyldigeBrukereUnderOppfolging();
        opensearchIndexer.dryrunAvPostgresTilOpensearchMapping(brukereUnderOppfolging);
    }

    @PutMapping("/test/hentFraOracleOgPostgres")
    public String testHentIndeksertPostgresOgOracleBruker(@RequestBody String aktoerId) {
        authorizeAdmin();
        OppfolgingsBruker fraOracle = brukerRepository.hentBrukerFraView(AktorId.of(aktoerId), false).get();
        opensearchIndexer.leggTilSisteEndring(fraOracle);
        OppfolgingsBruker fraPostgres = brukerRepository.hentBrukerFraView(AktorId.of(aktoerId), true).get();
        postgresOpensearchMapper.flettInnPostgresData(List.of(fraPostgres), true, true);

        return "{ \"oracle\":"+JsonUtils.toJson(fraOracle)+", \"postgres\":" +JsonUtils.toJson(fraPostgres)+" }";
    }
    private void authorizeAdmin() {
        final String ident = authContextHolder.getNavIdent().map(Id::toString).orElseThrow();
        if (!environmentProperties.getAdmins().contains(ident)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
