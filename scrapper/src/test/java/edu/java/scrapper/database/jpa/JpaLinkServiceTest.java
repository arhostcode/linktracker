package edu.java.scrapper.database.jpa;

import edu.java.domain.dto.Link;
import edu.java.domain.dto.TgChat;
import edu.java.dto.response.LinkResponse;
import edu.java.provider.InformationProviders;
import edu.java.provider.api.LinkInformation;
import edu.java.provider.github.GithubInformationProvider;
import edu.java.scrapper.IntegrationEnvironment;
import edu.java.service.LinkService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class JpaLinkServiceTest extends IntegrationEnvironment {

    @Autowired
    private LinkService linkService;

    @Autowired
    private JdbcClient client;

    @Autowired
    private EntityManager manager;

    @MockBean
    private InformationProviders informationProviders;

    @MockBean
    private GithubInformationProvider githubInformationProvider;

    @Test
    @Transactional
    @Rollback
    public void registerShouldCreateLinkInDatabase() {
        Mockito.when(informationProviders.getProvider(Mockito.any())).thenReturn(githubInformationProvider);
        Mockito.when(githubInformationProvider.fetchInformation(Mockito.any())).thenReturn(
            new LinkInformation(URI.create("https://example.com"), "Example", List.of())
        );
        Mockito.when(githubInformationProvider.isSupported(Mockito.any())).thenReturn(true);

        client.sql("INSERT INTO tg_chat (id) VALUES (5)").update();

        linkService.addLink(URI.create("https://example.com"), 5L);
        manager.flush();
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM link WHERE url = 'https://example.com'")
                .query(Long.class).single())
            .isEqualTo(1L);
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM chat_link WHERE chat_id = 5")
                .query(Long.class).single())
            .isEqualTo(1L);
    }

    @Test
    @Transactional
    @Rollback
    public void deleteShouldRemoveLinkFromDatabase() {
        client.sql("INSERT INTO tg_chat (id) VALUES (5)").update();
        var linkId =
            client.sql("INSERT INTO link (url, description) VALUES ('https://example.com', '') RETURNING id")
                .query(Long.class)
                .single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (5, ?)").params(linkId).update();
        linkService.removeLink(linkId, 5L);
        manager.flush();
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM link WHERE url = 'https://example.com'")
                .query(Long.class).single())
            .isEqualTo(0L);
        Assertions.assertThat(client.sql("SELECT COUNT(*) FROM chat_link WHERE chat_id = 5")
                .query(Long.class).single())
            .isEqualTo(0L);
    }

    @Test
    @Transactional
    @Rollback
    public void updateShouldUpdateLinkInDatabase() {
        var linkId =
            client.sql("INSERT INTO link (url, description) VALUES ('https://example.com', '') RETURNING id")
                .query(Long.class)
                .single();
        linkService.update(linkId, OffsetDateTime.now(), "new meta");
        manager.flush();
        Assertions.assertThat(client.sql("SELECT meta_information FROM link WHERE id = ?").params(linkId)
                .query(String.class)
                .single())
            .isEqualTo("new meta");
    }

    @Test
    @Transactional
    @Rollback
    public void getLinkSubscribersShouldReturnSubscribers() {
        client.sql("INSERT INTO tg_chat (id) VALUES (5)").update();
        var linkId =
            client.sql("INSERT INTO link (url, description) VALUES ('https://example.com', '') RETURNING id")
                .query(Long.class)
                .single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (5, ?)").params(linkId).update();
        Assertions.assertThat(linkService.getLinkSubscribers(linkId))
            .containsExactly(new TgChat(5L));
    }

    @Test
    @Rollback
    @Transactional
    public void listLinksShouldReturnLinks() {
        client.sql("INSERT INTO tg_chat (id) VALUES (5)").update();
        var linkId = client.sql("INSERT INTO link (url, description) VALUES ('https://example.com', '') RETURNING id")
            .query(Long.class).single();
        client.sql("INSERT INTO chat_link (chat_id, link_id) VALUES (5, ?)").params(linkId).update();
        Assertions.assertThat(linkService.listLinks(5L).links())
            .contains(new LinkResponse(linkId, URI.create("https://example.com")));
    }

    @Test
    @Rollback
    @Transactional
    public void checkNowShouldCheckLinkToDatabase() {
        var linkId = client.sql("INSERT INTO link (url, description) VALUES ('https://example.com', '') RETURNING id")
            .query(Long.class).single();
        linkService.checkNow(linkId);
        manager.flush();
        Assertions.assertThat(client.sql("SELECT * FROM link WHERE id = ?").params(linkId)
                .query(Link.class).single().getLastCheckedAt().withOffsetSameInstant(ZoneOffset.ofHours(3)))
            .isEqualToIgnoringHours(OffsetDateTime.now(ZoneOffset.ofHours(3)));
    }

    @Test
    @Rollback
    @Transactional
    public void listOldLinksShouldReturnLinks() {
        client.sql(
                "INSERT INTO link (url, description, last_checked_at) VALUES ('https://example.com', '', ?)")
            .params(OffsetDateTime.now().minusDays(4))
            .update();
        client.sql(
                "INSERT INTO link (url, description, last_checked_at) VALUES ('https://example2.com', '',?)")
            .params(OffsetDateTime.now().minusDays(3))
            .update();
        client.sql(
                "INSERT INTO link (url, description, last_checked_at) VALUES ('https://example3.com','', ?)")
            .params(OffsetDateTime.now().minusDays(2))
            .update();

        Assertions.assertThat(linkService.listOldLinks(Duration.ofDays(3).minusHours(1), 5))
            .map(Link::getUrl)
            .containsExactlyInAnyOrder("https://example2.com", "https://example.com");
    }

    @DynamicPropertySource
    static void jpaProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jpa");
    }
}
