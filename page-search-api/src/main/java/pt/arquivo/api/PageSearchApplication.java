package pt.arquivo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.cdx.CDXSearchService;
import pt.arquivo.services.nutchwax.NutchWaxSearchService;

import java.io.IOException;

@SpringBootApplication
public class PageSearchApplication extends SpringBootServletInitializer {

    @Bean
    CDXSearchService generateCDXSearchService() throws IOException {
        return new CDXSearchService();
    }

    @Bean
    SearchService generateService() throws IOException {
        // return new SolrSearchService();
        return new NutchWaxSearchService();
    }

    public static void main(String[] args) {
        SpringApplication.run(PageSearchApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
       return application.sources(PageSearchApplication.class);
    }
}
