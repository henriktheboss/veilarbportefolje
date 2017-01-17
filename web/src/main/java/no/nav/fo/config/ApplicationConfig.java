package no.nav.fo.config;

import no.nav.fo.internal.IsAliveServlet;
import no.nav.fo.service.ServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        Pingables.class,
        DatabaseConfig.class,
        VirksomhetEnhetEndpointConfig.class,
        ServiceConfig.class,
        PortefoljeMock.class
})
public class ApplicationConfig {

    @Bean
    public IsAliveServlet isAliveServlet() {
        return new IsAliveServlet();
    }

}
