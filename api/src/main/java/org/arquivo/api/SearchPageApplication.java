package org.arquivo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
// @EnableAutoConfiguration
// @EnableWebMvc
// @ComponentScan
public class SearchPageApplication extends SpringBootServletInitializer {
    public static void main(String[] args){
        SpringApplication.run(SearchPageApplication.class, args);
    }
}
