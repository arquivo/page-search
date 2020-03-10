package org.arquivo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// @EnableAutoConfiguration
// @EnableWebMvc
// @ComponentScan
@SpringBootApplication
public class SearchPageApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchPageApplication.class, args);
    }
}
