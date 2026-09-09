package pt.arquivo.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {
    /* https://springdoc.org/ */

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .tags(Arrays.asList(
                        new Tag().name("PageSearch").description("Endpoints to search for Archived WebPages content"),
                        new Tag().name("Metadata").description("(Not Published) Endpoints to retrieve metadata information about an Archived Web Resource"),
                        new Tag().name("HealthCheck").description("Endpoints to check the connectivity of this service's dependencies")
                ));
    }
}
