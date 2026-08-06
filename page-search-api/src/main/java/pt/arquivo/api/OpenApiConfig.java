package pt.arquivo.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    /* https://springdoc.org/ */

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .tags(List.of(
                        new Tag().name("PageSearch").description("Endpoints to search for Archived WebPages content"),
                        new Tag().name("Metadata").description("(Not Published) Endpoints to retrieve metadata information about an Archived Web Resource")
                ));
    }
}
