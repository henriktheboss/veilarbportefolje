package no.nav.fo.veilarbportefolje.krr;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.CollectionUtils;
import no.nav.fo.veilarbportefolje.config.HttpConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.fo.veilarbportefolje.util.SubjectUtils.getOidcToken;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Slf4j
public class KrrService {

    public static final String DKIF_URL_PROPERTY_NAME = "DKIF_URL";
    public static final String DKIF_URL_PATH = "/api/v1/personer/kontaktinformasjon";
    public static final int BULK_SIZE = 500;

    private KrrRepository krrRepository;

    @Inject
    public KrrService(KrrRepository krrRepository) {
        this.krrRepository = krrRepository;
    }

    public void oppdaterDigitialKontaktinformasjon() {
        log.info("Indeksering: Starter henting av KRR informasjon...");

        krrRepository.slettKrrInformasjon();
        List<String> fodselsnummere = krrRepository.hentAlleFnrUnderOppfolging();

        oppdaterKrrInfo(fodselsnummere);

        log.info("Indeksering: Fullført henting av KRR informasjon");
    }

    public void oppdaterKrrInfo(List<String> fodselsnummere) {
        CollectionUtils
                .partition(fodselsnummere, BULK_SIZE)
                .forEach(this::oppdaterKrrKontaktinfo);
    }

    private void oppdaterKrrKontaktinfo(List<String> fodselsnummere) {
        log.info("Oppdaterer KRR for {} brukere", fodselsnummere.size());
        Map<String, KrrKontaktInfoDTO> kontaktinfo = hentKrrKontaktInfo(fodselsnummere).getKontaktinfo();
        krrRepository.lagreKrrKontaktInfo(kontaktinfo.values());
    }

    public static KrrDTO hentKrrKontaktInfo(List<String> fodselsnummere) {

        List<String> fnrWithQuotes = fodselsnummere.stream().map(fnr -> "\"" + fnr + "\"").collect(Collectors.toList());

        return HttpConfig.usingFailSafeClient(client ->
                client.target(getRequiredProperty(DKIF_URL_PROPERTY_NAME))
                        .path(DKIF_URL_PATH)
                        .queryParam("inkluderSikkerDigitalPost", false)
                        .request()
                        .header(AUTHORIZATION, "Bearer " + getOidcToken())
                        .header("Nav-Personidenter", "List " + fnrWithQuotes)
                        .get(KrrDTO.class)
        );
    }
}
