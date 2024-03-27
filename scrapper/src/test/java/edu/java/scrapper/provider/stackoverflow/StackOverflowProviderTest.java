package edu.java.scrapper.provider.stackoverflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.configuration.ApplicationConfig;
import edu.java.configuration.RetryConfig;
import edu.java.provider.api.LinkInformation;
import edu.java.provider.stackoverflow.StackOverflowInformationProvider;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static edu.java.scrapper.provider.Utils.readAll;

public class StackOverflowProviderTest {
    private static WireMockServer server;
    private static final ApplicationConfig EMPTY_CONFIG = new ApplicationConfig(
        null,
        null,
        new ApplicationConfig.StackOverflowCredentials(null, null),
        null
    );
    private static final RetryConfig RETRY_CONFIG = new RetryConfig(
        List.of(new RetryConfig.RetryElement(
                "stackoverflow",
                "fixed",
                1,
                1,
                Duration.ofSeconds(1),
                null,
                List.of(404)
            )
        )
    );

    @BeforeAll
    public static void setUp() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.stubFor(get(urlPathMatching("/questions/100.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(readAll("/stackoverflow-mock-answer.json"))));
        server.stubFor(get(urlPathMatching("/questions/101.*"))
            .willReturn(aResponse()
                .withStatus(404)));
        server.start();
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnCorrectInformation() {
        StackOverflowInformationProvider provider =
            new StackOverflowInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG,
                new ObjectMapper(),
                RETRY_CONFIG
            );
        var info = provider.fetchInformation(new URI("https://stackoverflow.com/questions/100/?hello_world"));
        Assertions.assertThat(info)
            .extracting(LinkInformation::url, LinkInformation::title)
            .contains(
                new URI("https://stackoverflow.com/questions/100/?hello_world"),
                "What is the &#39;--&gt;&#39; operator in C/C++?"
            );
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnNullWhenQuestionNotFound() {
        StackOverflowInformationProvider provider =
            new StackOverflowInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG,
                new ObjectMapper(),
                RETRY_CONFIG
            );
        var info = provider.fetchInformation(new URI("https://stackoverflow.com/questions/101/?hello_world"));
        Assertions.assertThat(info).isNull();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnTrueIfHostIsValid() {
        StackOverflowInformationProvider provider =
            new StackOverflowInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG,
                new ObjectMapper(),
                RETRY_CONFIG
            );
        var info = provider.isSupported(new URI("https://stackoverflow.com/questions/100/?hello_world"));
        Assertions.assertThat(info).isTrue();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnFalseIfHostIsInValid() {
        StackOverflowInformationProvider provider =
            new StackOverflowInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG,
                new ObjectMapper(),
                RETRY_CONFIG
            );
        var info = provider.isSupported(new URI("https://memoryoutofrange.com/jij/hih"));
        Assertions.assertThat(info).isFalse();
    }
}
