package edu.java.scrapper.provider.github;

import com.github.tomakehurst.wiremock.WireMockServer;
import edu.java.configuration.ApplicationConfig;
import edu.java.provider.api.LinkInformation;
import edu.java.provider.github.GithubInformationProvider;
import java.net.URI;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static edu.java.scrapper.provider.Utils.readAll;

public class GithubInformationProviderTest {

    private static WireMockServer server;
    private static final ApplicationConfig EMPTY_CONFIG = new ApplicationConfig(
        null,
        null,
        null,
        null
    );

    @BeforeAll
    public static void setUp() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.stubFor(get(urlPathMatching("/repos/arhostcode/linktracker/events"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(readAll("/github-mock-answer.json"))));
        server.stubFor(get(urlPathMatching("/repos/jij/hih"))
            .willReturn(aResponse()
                .withStatus(404)));
        server.start();
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnCorrectInformation() {
        GithubInformationProvider provider =
            new GithubInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG
            );
        var info = provider.fetchInformation(new URI("https://github.com/arhostcode/linktracker"));
        Assertions.assertThat(info)
            .extracting(LinkInformation::url, LinkInformation::title)
            .contains(
                new URI("https://github.com/arhostcode/linktracker"),
                "arhostcode/linktracker"
            );
    }

    @SneakyThrows
    @Test
    public void getInformationShouldReturnNullWhenRepositoryNotFound() {
        GithubInformationProvider provider =
            new GithubInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG
            );
        var info = provider.fetchInformation(new URI("https://github.com/jij/hih"));
        Assertions.assertThat(info).isNull();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnTrueIfHostIsValid() {
        GithubInformationProvider provider =
            new GithubInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG
            );
        var info = provider.isSupported(new URI("https://github.com/jij/hih"));
        Assertions.assertThat(info).isTrue();
    }

    @SneakyThrows
    @Test
    public void isSupportedShouldReturnFalseIfHostIsInValid() {
        GithubInformationProvider provider =
            new GithubInformationProvider(
                server.baseUrl(),
                EMPTY_CONFIG
            );
        var info = provider.isSupported(new URI("https://gitlab.com/jij/hih"));
        Assertions.assertThat(info).isFalse();
    }

}
