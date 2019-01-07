package no.nav.fo.veilarbportefolje.indeksering;

import io.micrometer.core.instrument.Gauge;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.util.EnvironmentUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;
import static no.nav.fo.veilarbportefolje.indeksering.IndekseringConfig.ALIAS;

@Component
@Slf4j
public class MetricsReporter {

    protected RestHighLevelClient elastic;

    @Inject
    public MetricsReporter(RestHighLevelClient elastic) {
        this.elastic = elastic;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
        scheduler.scheduleAtFixedRate(new ReportNumberOfDocuments(), 1, 1, MINUTES);

        Gauge.builder("veilarbelastic_number_of_docs", this::getNumberOfDocs)
                .register(MetricsFactory.getMeterRegistry());
    }

    class ReportNumberOfDocuments implements Runnable {

        @Override
        public void run() {
            long numberOfDocs = getNumberOfDocs();
            Event event = MetricsFactory.createEvent("veilarbelastic.numberofdocs");
            event.addFieldToReport("value", numberOfDocs);
            event.report();
        }
    }

    @SneakyThrows
    private long getNumberOfDocs() {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(0);
        searchRequest.indices(ALIAS);
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = elastic.search(searchRequest, RequestOptions.DEFAULT);
        return response.getHits().totalHits;
    }
}
