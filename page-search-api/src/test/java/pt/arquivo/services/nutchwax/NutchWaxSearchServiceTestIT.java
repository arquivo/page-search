package pt.arquivo.services.nutchwax;

import org.archive.access.nutch.NutchwaxConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import pt.arquivo.services.SearchQueryImpl;
import pt.arquivo.services.SearchResults;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class NutchWaxSearchServiceTestIT {

    @Configuration
    static class ContextConfiguration {
        @Bean
        public NutchWaxSearchService initNutchWaxService() throws IOException {
            org.apache.hadoop.conf.Configuration configuration = NutchwaxConfiguration.getConfiguration();
            String fullPath = getClass().getClassLoader().getResource("search-servers.txt").getPath();
            String basePath = fullPath.substring(0, fullPath.lastIndexOf("/"));
            configuration.set("searcher.dir", basePath);
            return new NutchWaxSearchService(configuration);
        }
    }

    @Autowired
    private NutchWaxSearchService nutchWaxSearchService;

    @Test
    public void query() {
        SearchQueryImpl searchQuery = new SearchQueryImpl("teste");
        SearchResults searchResults = this.nutchWaxSearchService.query(searchQuery);
        assertThat(searchResults).isNotNull();
    }

    @Test
    public void testQuery() {
    }
}