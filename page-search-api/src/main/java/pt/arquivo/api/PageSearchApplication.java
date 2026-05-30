package pt.arquivo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import pt.arquivo.services.SearchService;
import pt.arquivo.services.cdx.CDXSearchService;
import pt.arquivo.services.solr.SolrSearchService;

import java.io.IOException;

@SpringBootApplication
public class PageSearchApplication extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(PageSearchApplication.class);

    @Value("${searchpages.textsearch.service.bean}")
    private String searchServiceBackend;

    @Bean
    CDXSearchService generateCDXSearchService() throws IOException {
        return new CDXSearchService();
    }

    @Bean
    SearchService generateService() throws Exception {
        if (searchServiceBackend.equalsIgnoreCase("nutchwax")) {
            LOG.info("Loading Nutchwax Search Service backend...");
            Class<?> clazz = Class.forName("pt.arquivo.services.nutchwax.NutchWaxSearchService");
            return (SearchService) clazz.getDeclaredConstructor().newInstance();
        }
        LOG.info("Loading Solr Search Service backend...");
        return new SolrSearchService();
    }

    public static void main(String[] args) {
        SpringApplication.run(PageSearchApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PageSearchApplication.class);
    }
}
