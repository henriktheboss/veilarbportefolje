package no.nav.pto.veilarbportefolje.opensearch;


import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.rest.client.RestUtils;
import no.nav.pto.veilarbportefolje.opensearch.domene.OpensearchClientConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static no.nav.common.rest.client.RestClient.baseClient;
import static no.nav.pto.veilarbportefolje.opensearch.MetricsReporter.getMeterRegistry;

@Slf4j
@Service
public class OpensearchCountService {
    private final OpensearchClientConfig opensearchClientConfig;
    private final String indexName;
    private final OkHttpClient client;
    private final MeterRegistry prometheusMeterRegistry;

    @Autowired
    public OpensearchCountService(
            OpensearchClientConfig opensearchClientConfig,
            IndexName opensearchIndex
    ) {
        this.opensearchClientConfig = opensearchClientConfig;
        this.indexName = opensearchIndex.getValue();
        client = baseClient();
        prometheusMeterRegistry = getMeterRegistry();
    }

    @SneakyThrows
    public long getCount() {
        String url = createAbsoluteUrl(opensearchClientConfig, indexName) + "_doc/_count";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", getAuthHeaderValue(opensearchClientConfig))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            long count = RestUtils.parseJsonResponse(response, CountResponse.class)
                    .map(CountResponse::getCount)
                    .orElse(0L);

            reportDocCountToPrometheus(count);
            return count;
        }
    }

    private void reportDocCountToPrometheus(long count) {
        Gauge.builder("portefolje.antall.brukere", () -> count)
                .register(prometheusMeterRegistry);
    }

    public static String createAbsoluteUrl(OpensearchClientConfig config, String indexName) {
        return String.format("%s%s/",
                createAbsoluteUrl(config),
                indexName
        );
    }

    public static String createAbsoluteUrl(OpensearchClientConfig config) {
        return String.format(
                "%s://%s:%s/",
                config.getScheme(),
                config.getHostname(),
                config.getPort()
        );
    }

    public static String getAuthHeaderValue(OpensearchClientConfig config) {
        String auth = config.getUsername() + ":" + config.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Data
    private static class CountResponse {
        private long count;
    }

}