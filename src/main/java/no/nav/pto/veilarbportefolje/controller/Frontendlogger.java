package no.nav.pto.veilarbportefolje.controller;

import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbportefolje.opensearch.MetricsReporter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.nav.common.utils.EnvironmentUtils.isProduction;
import static no.nav.pto.veilarbportefolje.util.SecureLog.secureLog;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logger")
public class Frontendlogger {
    private final MeterRegistry prometheusMeterRegistry = new MetricsReporter.ProtectedPrometheusMeterRegistry();

    @PostMapping("/event")
    public void skrivEventTilPrometheus(@RequestBody FrontendEvent event) {
        List<Tag> tags = new ArrayList<>();
        if (event.getTags() != null) {
            event.getTags().forEach((k, v) -> tags.add(new ImmutableTag(k, v)));
        }
        if (event.getFields() != null) {
            event.getFields().forEach((k, v) -> tags.add(new ImmutableTag(k, v.toString())));
        }

        tags.add(isProduction().orElse(false) ? new ImmutableTag("environment", "p") : new ImmutableTag("environment", "q1"));

        if (!isProduction().orElse(false)) {
            secureLog.info("Skriver event til prometheus: " + eventToString(event.name));
        }
        Iterable<Tag> iterable = tags;
        prometheusMeterRegistry.counter(event.name + ".event", iterable);
    }

    @Data
    @Accessors(chain = true)
    static class FrontendEvent {
        String name;
        Map<String, Object> fields;
        Map<String, String> tags;
    }

    public static String eventToString(String name) {
        return "name: " + name + ".event";
    }
}
