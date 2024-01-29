package no.nav.pto.veilarbportefolje.schema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RestController
@RequestMapping("/api/v2/schema")
@RequiredArgsConstructor
public class SchemaController {

    @Value("classpath:json-schema:filtervalg.json")
    private Resource filtervalgSchema;

    @GetMapping("/filtervalg")
    public ResponseEntity<String> filtervalgSchema() {
        try (Reader reader = new InputStreamReader(filtervalgSchema.getInputStream(), UTF_8)) {
            return ResponseEntity.ok(FileCopyUtils.copyToString(reader));
        } catch (IOException e) {
            log.warn("Can't read json schema for: filter valg");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
