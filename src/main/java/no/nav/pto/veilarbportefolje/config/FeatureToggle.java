package no.nav.pto.veilarbportefolje.config;


import no.finn.unleash.UnleashContext;
import no.nav.pto.veilarbportefolje.service.UnleashService;

public class FeatureToggle {
    private FeatureToggle() {
    }

    public static final String PDL = "voff.bruk_pdl";

    public static final String AUTO_SLETT = "pto.slett_gamle_aktorer_elastic";

    public static final String POSTGRES = "veilarbportefolje.sok_med_postgres";

    public static final String GR202_PA_KAFKA = "veilarbportefolje.GR202_pa_kafka";
    public static final String GR199_PA_KAFKA = "veilarbportefolje.GR199_pa_kafka";

    public static final String CV_EKSISTERE_PRODSETTE = "veilarbportefolje.cv_eksistere";

    public static final String KAFKA_AIVEN_CONSUMERS_STOP = "veilarbportefolje.kafka_aiven_consumers_stop";
    public static final String KAFKA_ONPREM_CONSUMERS_STOP = "veilarbportefolje.kafka_onprem_consumers_stop";

    public static boolean erPostgresPa(UnleashService unleashService, String userId) {
        return unleashService.isEnabled(FeatureToggle.POSTGRES, new UnleashContext(userId, null, null, null));
    }

    public static boolean erGR202PaKafka(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.GR202_PA_KAFKA);
    }

    public static boolean erGR199PaKafka(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.GR199_PA_KAFKA);
    }

    public static boolean erCvEksistereIProd(UnleashService unleashService) {
        return unleashService.isEnabled(CV_EKSISTERE_PRODSETTE);
    }
}
