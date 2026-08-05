package dev.sam.capi;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean CustomerDataHasher customerDataHasher() { return new CustomerDataHasher(); }
    @Bean MetaEventFactory metaEventFactory(CustomerDataHasher hasher, Clock clock) { return new MetaEventFactory(hasher, clock); }

    @Bean
    WebMvcConfigurer cors(@Value("${app.frontend-url}") String frontendUrl) {
        return new WebMvcConfigurer() {
            @Override public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins(frontendUrl).allowedMethods("GET", "POST");
            }
        };
    }
}
