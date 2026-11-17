package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Step 1 of reading any project (slide 5):
 * Spring Boot starts here, then loads the Spring XML files.
 * The <camelContext> inside camel-context.xml becomes THE CamelContext.
 */
@SpringBootApplication
@ImportResource({
        "classpath:spring/beans.xml",
        "classpath:spring/common-routes.xml",
        "classpath:spring/mock-backend-routes.xml",
        "classpath:spring/camel-context.xml"
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}