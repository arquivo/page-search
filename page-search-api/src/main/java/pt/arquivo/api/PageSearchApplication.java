package pt.arquivo.api;

import org.apache.solr.client.solrj.SolrClient;
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
import pt.arquivo.services.solr.SolrClientFactory;
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
     * Shared by both the active SolrSearchService (when selected) and the /healthcheck endpoint, so they
     * hit the exact same connection/ZK session instead of each opening their own, and so the healthcheck's
     * behavior (standalone vs SolrCloud, timeouts) always matches what real searches actually experience.
     * The healthcheck still always exercises Solr regardless of which SearchService backend is currently
     * selected — it must not skip Solr just because NutchWax is active instead.
     */
    @Bean
    SolrClient solrClient(
            @Value("${searchpages.textsearch.service.bean.solr.link:http://localhost:8983/solr/searchpages}") String baseSolrUrl,
            @Value("${searchpages.textsearch.service.bean.solr.zkhosts:}") String zkHosts,
            @Value("${searchpages.textsearch.service.bean.solr.collection:}") String defaultCollection,
            @Value("${searchpages.textsearch.service.bean.solr.connectiontimeout.ms:5000}") int connectionTimeoutMillis,
            @Value("${searchpages.textsearch.service.bean.solr.sockettimeout.ms:20000}") int socketTimeoutMillis) {
        return SolrClientFactory.buildClient(baseSolrUrl, zkHosts, defaultCollection,
                connectionTimeoutMillis, socketTimeoutMillis);
    }

    @Bean
    SearchService generateService(SolrClient solrClient) throws Exception {
        if (searchServiceBackend.equalsIgnoreCase("nutchwax")) {
            LOG.info("Loading Nutchwax Search Service backend...");
            Class<?> clazz = Class.forName("pt.arquivo.services.nutchwax.NutchWaxSearchService");
            return (SearchService) clazz.getDeclaredConstructor().newInstance();
        }
        LOG.info("Loading Solr Search Service backend...");
        SolrSearchService service = new SolrSearchService();
        service.setSolrClient(solrClient);
        return service;
    }

    public static void main(String[] args) {
        SpringApplication.run(PageSearchApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PageSearchApplication.class);
    }
}
