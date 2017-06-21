package no.nav.fo.service;

import no.nav.fo.database.BrukerRepository;
import no.nav.fo.domene.Filtervalg;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SolrServiceTest {

    @Mock
    private BrukerRepository brukerRepository;
    @Mock
    private SolrClient solrClientSlave;
    @Mock
    private SolrClient solrClientMaster;

    private SolrService service;

    @Before
    public void setup() {
        service = new SolrService(solrClientMaster, solrClientSlave, brukerRepository);
    }

    @Test
    public void deltaindekseringSkalOppdatereTidsstempel() throws Exception {
        SolrInputDocument dummyDocument = new SolrInputDocument();
        dummyDocument.addField("person_id", "dummy");
        when(brukerRepository.retrieveOppdaterteBrukere()).thenReturn(singletonList(dummyDocument));
        System.setProperty("cluster.ismasternode", "true");

        service.deltaindeksering();

        verify(brukerRepository, atLeastOnce()).updateTidsstempel(any(Timestamp.class));
    }

    @Test
    public void deltaindekseringSkalIkkeOppdatereTidsstempel() throws Exception {
        when(brukerRepository.retrieveOppdaterteBrukere()).thenReturn(emptyList());
        System.setProperty("cluster.ismasternode", "true");

        service.deltaindeksering();

        verify(brukerRepository, never()).updateTidsstempel(any(Timestamp.class));
    }

    @Test
    public void hentBrukereForEnhet() throws IOException, SolrServerException {
        ArgumentCaptor<SolrQuery> captor = ArgumentCaptor.forClass(SolrQuery.class);
        when(solrClientSlave.query(any(SolrQuery.class))).thenReturn(queryResponse(0, new SolrDocumentList()));

        service.hentBrukere("0100", Optional.empty(), "ikke_satt", "ikke_satt", new Filtervalg());

        verify(solrClientSlave, times(1)).query(captor.capture());
        assertThat(captor.getValue().getQuery()).isEqualTo("*:*");
        assertThat(captor.getValue().getFilterQueries().length).isEqualTo(1);
        assertThat(captor.getValue().getFilterQueries()[0]).isEqualTo("enhet_id: 0100");
    }

    @Test
    public void hentBrukereForVeileder() throws IOException, SolrServerException {
        ArgumentCaptor<SolrQuery> captor = ArgumentCaptor.forClass(SolrQuery.class);
        when(solrClientSlave.query(any(SolrQuery.class))).thenReturn(queryResponse(0, new SolrDocumentList()));

        service.hentBrukere("0100", Optional.of("Z900000"), "ikke_satt", "ikke_satt", new Filtervalg());

        verify(solrClientSlave, times(1)).query(captor.capture());
        assertThat(captor.getValue().getQuery()).isEqualTo("*:*");
        assertThat(captor.getValue().getFilterQueries().length).isEqualTo(1);
        assertThat(captor.getValue().getFilterQueries()[0]).isEqualTo("veileder_id: Z900000 AND enhet_id: 0100");
    }

    @Test
    public void byggQueryString() {
        assertThat(service.byggQueryString("0100", Optional.empty())).isEqualTo("enhet_id: 0100");
        assertThat(service.byggQueryString("0100", Optional.of(""))).isEqualTo("enhet_id: 0100");
        assertThat(service.byggQueryString("0100", Optional.of("Z900000"))).isEqualTo("veileder_id: Z900000 AND enhet_id: 0100");
    }

    private QueryResponse queryResponse(int status, SolrDocumentList data) {
        QueryResponse response = new QueryResponse();
        NamedList<Object> responseData = new NamedList<>();
        NamedList<Object> headerData = new NamedList<>();
        headerData.add("status", status);
        responseData.add("responseHeader", headerData);
        responseData.add("response", data);
        response.setResponse(responseData);

        return response;
    }
}