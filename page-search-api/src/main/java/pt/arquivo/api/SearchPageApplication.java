package pt.arquivo.api;

import pt.arquivo.services.solr.SolrSearchService;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.nutchwax.NutchWaxSearchService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class SearchPageApplication {

    @Bean
    SearchService generateService() throws IOException {
        // return new SolrSearchService();
        return new NutchWaxSearchService();
    };

    public static void main(String[] args) {
        SpringApplication.run(SearchPageApplication.class, args);
    }
}
