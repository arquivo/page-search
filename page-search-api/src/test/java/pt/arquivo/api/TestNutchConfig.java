package pt.arquivo.api;

import org.archive.access.nutch.NutchwaxConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import pt.arquivo.services.nutchwax.NutchWaxSearchService;

import java.io.IOException;
import java.net.URLDecoder;

@TestConfiguration
public class TestNutchConfig {

    @Bean
    @Primary
    public NutchWaxSearchService initTestNutchWaxService() throws IOException {
        org.apache.hadoop.conf.Configuration configuration = NutchwaxConfiguration.getConfiguration();
        String fullPath = getClass().getClassLoader().getResource("search-servers.txt").getPath();
        String basePath = fullPath.substring(0, fullPath.lastIndexOf("/"));
        System.out.println(">>>>>>>> search-server.txt file location: " + basePath);
        configuration.set("searcher.dir", URLDecoder.decode(basePath, "utf8"));
        return new NutchWaxSearchService(configuration);
    }
}
