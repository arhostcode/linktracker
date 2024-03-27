package edu.java.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull
    Scheduler scheduler,
    String githubToken,
    StackOverflowCredentials stackOverflow,
    AccessType databaseAccessType
) {
    public record Scheduler(
        boolean enable,
        @NotNull Duration interval,
        @NotNull Duration forceCheckDelay,
        int maxLinksPerCheck
    ) {
    }

    public record StackOverflowCredentials(
        String key,
        String accessToken
    ) {
    }

    public enum AccessType {
        JDBC, JPA, JOOQ
    }
}
