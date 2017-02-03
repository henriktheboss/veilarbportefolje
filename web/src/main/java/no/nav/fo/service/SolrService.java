package no.nav.fo.service;

import no.nav.fo.database.BrukerRepository;
import no.nav.fo.domene.Bruker;
import no.nav.fo.util.DbUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.solr.client.solrj.SolrQuery.ORDER.desc;
import static org.slf4j.LoggerFactory.getLogger;

public class SolrService {

    private static final Logger logger = getLogger(SolrService.class);

    @Inject
    private HttpSolrServer server;

    @Inject
    private BrukerRepository brukerRepository;

    @Scheduled(cron = "${veilarbportefolje.cron.hovedindeksering}")
    public void hovedindeksering() {
        if (isSlaveNode()) {
            logger.info("Noden er en slave. Kun masternoden kan iverksett indeksering. Avbryter.");
            return;
        }

        logger.info("Starter hovedindeksering");

        List<Map<String, Object>> rader = brukerRepository.retrieveAlleBrukere();
        List<SolrInputDocument> dokumenter = rader.stream().map(DbUtils::mapRadTilDokument).collect(Collectors.toList());

        deleteAllDocuments();
        addDocuments(dokumenter);
        updateTimestamp(rader);

        logger.info("Hovedindeksering fullført!");
        logger.info(dokumenter.size() + " dokumenter ble lagt til i solrindeksen");
    }

    @Scheduled(cron = "${veilarbportefolje.cron.deltaindeksering}")
    public void deltaindeksering() {
        if (isSlaveNode()) {
            logger.info("Noden er en slave. Kun masternoden kan iverksett indeksering. Avbryter.");
            return;
        }

        logger.info("Starter deltaindeksering");
        List<Map<String, Object>> rader = brukerRepository.retrieveOppdaterteBrukere();
        if (rader.isEmpty()) {
            logger.info("Ingen nye dokumenter i databasen");
            return;
        }

        List<SolrInputDocument> dokumenter = rader.stream().map(DbUtils::mapRadTilDokument).collect(Collectors.toList());
        addDocuments(dokumenter);
        updateTimestamp(rader);
        logger.info("Deltaindeksering fullført!");
        logger.info(dokumenter.size() + " dokumenter ble oppdatert/lagt til i solrindeksen");
    }

    public List<Bruker> hentBrukere(String enhetId, String sortOrder) {
        SolrQuery.ORDER order = SolrQuery.ORDER.asc;
        if ("descending".equals(sortOrder)) {
            order = desc;
        }

        String queryString = "enhet_id: " + enhetId;
        SolrQuery solrQuery = new SolrQuery(queryString);
        solrQuery.addSort("etternavn", order);
        solrQuery.addSort("fornavn", order);

        List<Bruker> brukere = new ArrayList<>();
        try {
            QueryResponse response = server.query(solrQuery);
            SolrDocumentList results = response.getResults();
            logger.debug(results.toString());
            brukere = results.stream().map(Bruker::of).collect(toList());
        } catch (SolrServerException e) {
            logger.error("Spørring mot indeks feilet: ", e.getMessage(), e);
        }
        return brukere;
    }

    Map<String, Object> nyesteBruker(List<Map<String, Object>> brukere) {
        return brukere.stream().max(Comparator.comparing(r -> new DateTime(r.get("tidsstempel")).getMillis())).get();
    }


    static boolean isSlaveNode() {
        String isMasterString = System.getProperty("cluster.ismasternode", "false");
        return !Boolean.parseBoolean(isMasterString);
    }

    private void updateTimestamp(List<Map<String, Object>> rader) {
        Timestamp tidsstempel = (Timestamp) nyesteBruker(rader).get("tidsstempel");
        brukerRepository.updateTidsstempel(tidsstempel);
    }

    private UpdateResponse addDocuments(List<SolrInputDocument> dokumenter) {
        UpdateResponse response = null;
        try {
            response = server.add(dokumenter);
            checkSolrUpdateResponseCode(response);
        } catch (SolrServerException | IOException e) {
            logger.error("Kunne ikke legge til dokumenter.", e.getMessage(), e);
        } catch (SolrUpdateResponseCodeException e) {
            logger.error(e.getMessage());
        }
        return response;
    }

    private UpdateResponse deleteAllDocuments() {
        UpdateResponse response = null;
        try {
            response = server.deleteByQuery("*:*");
            checkSolrUpdateResponseCode(response);
        } catch (SolrServerException | IOException e) {
            logger.error("Kunne ikke slette dokumenter.", e.getMessage(), e);
        } catch (SolrUpdateResponseCodeException e) {
            logger.error(e.getMessage());
        }
        return response;
    }

    private void checkSolrUpdateResponseCode(UpdateResponse response) {
        if (response.getStatus() != 0) {
            throw new SolrUpdateResponseCodeException(String.format("Solr returnerte med responskode %s", response.getStatus()));
        }
    }

}
