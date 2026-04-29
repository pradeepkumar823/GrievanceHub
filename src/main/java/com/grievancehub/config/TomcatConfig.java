package com.grievancehub.config;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public TomcatConnectorCustomizer connectorCustomizer() {
        return (connector) -> {
            // Increase part limit (form fields + files) from Tomcat's default of 10
            connector.setMaxPartCount(50);
        };
    }
}
