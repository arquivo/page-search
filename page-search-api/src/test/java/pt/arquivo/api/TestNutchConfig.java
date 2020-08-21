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
        // to solve the following problem  https://stackoverflow.com/questions/17265002/hadoop-no-filesystem-for-scheme-file
        configuration.set("fs.hdfs.impl",
                org.apache.hadoop.dfs.DistributedFileSystem.class.getName()
        );
        configuration.set("fs.file.impl",
                org.apache.hadoop.fs.LocalFileSystem.class.getName()
        );
        return new NutchWaxSearchService(configuration);
    }
}
