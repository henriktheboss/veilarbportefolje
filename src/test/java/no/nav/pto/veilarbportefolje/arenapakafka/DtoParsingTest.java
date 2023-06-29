package no.nav.pto.veilarbportefolje.arenapakafka;

import no.nav.pto.veilarbportefolje.arenapakafka.arenaDTO.*;
import no.nav.pto.veilarbportefolje.config.ApplicationConfigTest;
import no.nav.pto.veilarbportefolje.util.EndToEndTest;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.pto.veilarbportefolje.util.TestUtil.readFileAsJsonString;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApplicationConfigTest.class)
public class DtoParsingTest extends EndToEndTest {
    @Test
    public void skal_bygge_korrekt_UtdanningsAktivitetInnhold_json() {
        String goldenGateDtoString = readFileAsJsonString("/goldenGateUtdanningsAktivitet.json", getClass());

        UtdanningsAktivitetDTO goldenGateDTO = fromJson(goldenGateDtoString, UtdanningsAktivitetDTO.class);
        assertThat(goldenGateDTO.getCurrentTimestamp()).isEqualTo("2021-06-23T09:03:55.677014");
        assertThat(goldenGateDTO.getAfter().getClass()).isEqualTo(UtdanningsAktivitetInnhold.class);
        assertThat(goldenGateDTO.getAfter().getEndretDato().getDato().toString().substring(0,10)).isEqualTo("2021-06-18");
        assertThat(goldenGateDTO.getAfter()).isNotNull();
        assertThat(goldenGateDTO.getBefore()).isNull();
    }

    @Test
    public void skal_bygge_korrekt_GruppeAktivitetInnhold_json() {
        String goldenGateDtoString = readFileAsJsonString("/goldenGateUtdanningsAktivitet.json", getClass());

        GruppeAktivitetDTO goldenGateDTO = fromJson(goldenGateDtoString, GruppeAktivitetDTO.class);
        assertThat(goldenGateDTO.getCurrentTimestamp()).isEqualTo("2021-06-23T09:03:55.677014");
        assertThat(goldenGateDTO.getAfter().getClass()).isEqualTo(GruppeAktivitetInnhold.class);
        assertThat(goldenGateDTO.getAfter()).isNotNull();
        assertThat(goldenGateDTO.getBefore()).isNull();
    }

    @Test
    public void skal_bygge_korrekt_dagpenge_json() {
        String goldenGateDtoString = readFileAsJsonString("/goldenGateDagpenger.json", getClass());

        YtelsesDTO goldenGateDTO = fromJson(goldenGateDtoString, YtelsesDTO.class);
        assertThat(goldenGateDTO.getAfter().getClass()).isEqualTo(YtelsesInnhold.class);
        assertThat(goldenGateDTO.getAfter()).isNotNull();
        assertThat(goldenGateDTO.getBefore()).isNull();
    }
}
