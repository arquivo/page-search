package pt.arquivo.api;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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

    /**
     * Dedicated to the /healthcheck endpoint, so it always pings the Solr backend regardless of which
     * SearchService is active (e.g. it must not ping NutchWax when searchpages.textsearch.service.bean
     * selects that backend instead). Timeouts are explicit and comparatively short, so a Solr that's up
     * but hanging fails the healthcheck quickly instead of blocking the deploy gate that calls it.
     */
    @Bean
    SolrClient healthCheckSolrClient(
            @Value("${searchpages.textsearch.service.bean.solr.link:http://localhost:8983/solr/searchpages}") String baseSolrUrl,
            @Value("${searchpages.healthcheck.solr.connectiontimeout.ms:2000}") int connectionTimeoutMillis,
            @Value("${searchpages.healthcheck.solr.sockettimeout.ms:3000}") int socketTimeoutMillis) {
        return new HttpSolrClient.Builder(baseSolrUrl)
                .withConnectionTimeout(connectionTimeoutMillis)
                .withSocketTimeout(socketTimeoutMillis)
                .build();
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
