package no.nav.fo.service;

import javaslang.control.Try;
import no.nav.fo.database.BrukerRepository;
import no.nav.fo.domene.Bruker;
import no.nav.fo.domene.FacetResults;
import no.nav.fo.domene.Filtervalg;
import no.nav.fo.domene.StatusTall;
import no.nav.fo.exception.SolrUpdateResponseCodeException;
import no.nav.fo.util.DbUtils;
import no.nav.fo.util.SolrUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class SolrService {

    private static final Logger logger = getLogger(SolrService.class);

    private static final String HOVEDINDEKSERING = "Hovedindeksering";
    private static final String DELTAINDEKSERING = "Deltaindeksering";

    @Inject
    private JdbcTemplate db;

    @Inject
    private SolrClient solrClientSlave;

    @Inject
    private SolrClient solrClientMaster;

    @Inject
    private BrukerRepository brukerRepository;

    @Transactional
    public void hovedindeksering() {
        if (SolrUtils.isSlaveNode()) {
            logger.info("Noden er en slave. Kun masternoden kan iverksett indeksering. Avbryter.");
            return;
        }

        logger.info("Starter hovedindeksering");
        LocalDateTime t0 = LocalDateTime.now();

        List<SolrInputDocument> dokumenter = brukerRepository.retrieveAlleBrukere();
        deleteAllDocuments();
        addDocuments(dokumenter);
        commit();
        brukerRepository.updateTidsstempel(Timestamp.valueOf(t0));

        logFerdig(t0, dokumenter.size(), HOVEDINDEKSERING);
    }


    @Scheduled(cron = "${veilarbportefolje.cron.deltaindeksering}")
    @Transactional
    public void deltaindeksering() {
        if (SolrUtils.isSlaveNode()) {
            logger.info("Noden er en slave. Kun masternoden kan iverksett indeksering. Avbryter.");
            return;
        }

        logger.info("Starter deltaindeksering");
        LocalDateTime t0 = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(t0);

        logger.info("Syncer materialiserte views");
        db.execute(lagSyncSql());

        List<SolrInputDocument> dokumenter = brukerRepository.retrieveOppdaterteBrukere();
        if (!dokumenter.isEmpty()) {
            logger.info("Ingen nye dokumenter i databasen");
            return;
        }

        addDocuments(dokumenter);
        brukerRepository.updateTidsstempel(timestamp);
        commit();

        logFerdig(t0, dokumenter.size(), DELTAINDEKSERING);
    }

    private String lagSyncSql() {
        Stream<String> views = Stream.of(
                "OPPFOLGINGSBRUKER",
                "SIKKERHETSTILTAK_TYPE",
                "HOVEDMAAL",
                "RETTIGHETSGRUPPETYPE",
                "KVALIFISERINGSGRUPPETYPE",
                "FORMIDLINGSGRUPPETYPE"
        );

        String viewsSql = views
                .map((view) -> String.format("DBMS_MVIEW.REFRESH('%s', 'F');", view))
                .collect(joining(" "));

        return String.format("BEGIN %s END;", viewsSql);
    }

    public List<Bruker> hentBrukereForEnhet(String enhetId, String sortOrder, String sortField, Filtervalg filtervalg) {
        String queryString = "enhet_id: " + enhetId;
        return hentBrukere(queryString, sortOrder, sortField, filtervalg);
    }

    public List<Bruker> hentBrukereForVeileder(String veilederIdent, String enhetId, String sortOrder, String sortField, Filtervalg filtervalg) {
        String queryString = "veileder_id: " + veilederIdent + " AND enhet_id: " + enhetId;
        return hentBrukere(queryString, sortOrder, sortField, filtervalg);
    }

    public List<Bruker> hentBrukere(String queryString, String sortOrder, String sortField, Filtervalg filtervalg) {
        List<Bruker> brukere = new ArrayList<>();
        try {
            QueryResponse response = solrClientSlave.query(SolrUtils.buildSolrQuery(queryString, filtervalg));
            SolrUtils.checkSolrResponseCode(response.getStatus());
            SolrDocumentList results = response.getResults();
            logger.debug(results.toString());
            brukere = results.stream().map(Bruker::of).collect(toList());
        } catch (SolrServerException | IOException e) {
            logger.error("Spørring mot indeks feilet: ", e.getMessage(), e);
        }
        return SolrUtils.sortBrukere(brukere, sortOrder, sortField);
    }

    public FacetResults hentPortefoljestorrelser(String enhetId) {

        String facetFieldString = "veileder_id";

        SolrQuery solrQuery = SolrUtils.buildSolrFacetQuery("enhet_id: " + enhetId, facetFieldString);

        QueryResponse response = new QueryResponse();
        try {
            response = solrClientSlave.query(solrQuery);
            logger.debug(response.toString());
        } catch (SolrServerException | IOException e) {
            logger.error("Spørring mot indeks feilet", e.getMessage(), e);
        }

        FacetField facetField = response.getFacetField(facetFieldString);

        return SolrUtils.mapFacetResults(facetField);
    }

    public void indekserBrukerdata(String personId) {
        logger.info("Legger bruker med personId % til i indeks ", personId);
        List<Map<String, Object>> rader = brukerRepository.retrieveBrukermedBrukerdata(personId);
        List<SolrInputDocument> dokumenter = rader.stream().map(DbUtils::mapRadTilDokument).collect(Collectors.toList());
        addDocuments(dokumenter);
        commit();
        logger.info("Bruker med personId {} lagt til i indeksen", personId);
    }

    public Try<UpdateResponse> commit() {
        return Try.of(() -> solrClientMaster.commit())
                .onFailure(e -> logger.error("Kunne ikke gjennomføre commit ved indeksering!", e));
    }

    public List<SolrInputDocument> addDocuments(List<SolrInputDocument> dokumenter) {
        // javaslang.collection-API brukes her pga sliding-metoden
        javaslang.collection.List.ofAll(dokumenter)
                .sliding(10000, 10000)
                .forEach(docs -> {
                    try {
                        solrClientMaster.add(docs.toJavaList());
                        logger.info(format("Legger til %d dokumenter i indeksen", docs.length()));
                    } catch (SolrServerException | IOException e) {
                        logger.error("Kunne ikke legge til dokumenter.", e.getMessage(), e);
                    }
                });
        return dokumenter;
    }

    private void deleteAllDocuments() {
        try {
            UpdateResponse response = solrClientMaster.deleteByQuery("*:*");
            SolrUtils.checkSolrResponseCode(response.getStatus());
        } catch (SolrServerException | IOException e) {
            logger.error("Kunne ikke slette dokumenter.", e.getMessage(), e);
        } catch (SolrUpdateResponseCodeException e) {
            logger.error(e.getMessage());
        }
    }

    private void logFerdig(LocalDateTime t0, int antall, String indekseringstype) {
        Duration duration = Duration.between(t0, LocalDateTime.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds();
        String logString = format("%s fullført! | Tid brukt(hh:mm:ss): %02d:%02d:%02d | Dokumenter oppdatert: %d", indekseringstype, hours, minutes, seconds, antall);
        logger.info(logString);

        Event event = MetricsFactory.createEvent("deltaindeksering.fullfort");
        event.addFieldToReport("antall.oppdateringer", antall );
        event.report();
    }

    public StatusTall hentStatusTallForPortefolje(String enhet) {
        SolrQuery solrQuery = new SolrQuery("*:*");

        String nyeBrukere = "-veileder_id:*";
        String inaktiveBrukere = "formidlingsgruppekode:ISERV AND veileder_id:*";

        solrQuery.addFilterQuery("enhet_id:" + enhet);
        solrQuery.addFacetQuery(nyeBrukere);
        solrQuery.addFacetQuery(inaktiveBrukere);
        solrQuery.setRows(0);

        StatusTall statusTall = new StatusTall();
        QueryResponse response;
        try {
            response = solrClientSlave.query(solrQuery);
            long antallTotalt = response.getResults().getNumFound();
            long antallNyeBrukere = response.getFacetQuery().get(nyeBrukere);
            long antallInaktiveBrukere = response.getFacetQuery().get(inaktiveBrukere);
            statusTall.setTotalt(antallTotalt).setInaktiveBrukere(antallInaktiveBrukere).setNyeBrukere(antallNyeBrukere);
        } catch (SolrServerException | IOException e) {
            logger.error("Henting av statustall for portefølje feilet ", e.getMessage(), e);
        }

        return statusTall;
    }

    public StatusTall hentStatusTallForVeileder(String enhet, String veilederIdent) {
        SolrQuery solrQuery = new SolrQuery("*:*");

        String inaktiveBrukere = "formidlingsgruppekode:ISERV";

        solrQuery.addFilterQuery("enhet_id:" + enhet);
        solrQuery.addFilterQuery("veileder_id:" + veilederIdent);
        solrQuery.addFacetQuery(inaktiveBrukere);
        solrQuery.setRows(0);

        StatusTall statusTall = new StatusTall();
        QueryResponse response;
        try {
            response = solrClientSlave.query(solrQuery);
            long antallTotalt = response.getResults().getNumFound();
            long antallInaktiveBrukere = response.getFacetQuery().get(inaktiveBrukere);
            statusTall.setTotalt(antallTotalt).setInaktiveBrukere(antallInaktiveBrukere);
        } catch (SolrServerException | IOException e) {
            logger.error("Henting av statustall for veileder feilet ", e.getMessage(), e);
        }

        return statusTall;
    }
}
