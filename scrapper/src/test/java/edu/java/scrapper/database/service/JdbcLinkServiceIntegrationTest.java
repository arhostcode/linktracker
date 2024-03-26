package edu.java.scrapper.database.service;

import edu.java.exception.LinkNotFoundException;
import edu.java.domain.dto.Link;
import edu.java.domain.dto.TgChat;
import edu.java.persitence.jdbc.repository.JdbcChatLinkRepository;
import edu.java.persitence.jdbc.repository.JdbcLinkRepository;
import edu.java.provider.InformationProviders;
import edu.java.provider.api.LinkInformation;
import edu.java.provider.github.GithubInformationProvider;
import edu.java.scrapper.IntegrationEnvironment;
import edu.java.service.ChatService;
import edu.java.service.LinkService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class JdbcLinkServiceIntegrationTest extends IntegrationEnvironment {
    @Autowired
    private LinkService linkService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private JdbcLinkRepository linkRepository;
    @Autowired
    private JdbcChatLinkRepository chatLinkRepository;

    @MockBean
    private GithubInformationProvider provider;
    @MockBean
    private InformationProviders providers;

    @Test
    @Transactional
    @Rollback
    void addLinkShouldAddLinkAndCreateIfNotExists() {
        String url = "https://github.com/arhostcode/linktracker";
        Mockito.when(provider.fetchInformation(Mockito.any()))
            .thenReturn(new LinkInformation(URI.create(url), "github", List.of()));
        Mockito.when(provider.isSupported(Mockito.any())).thenReturn(true);
        Mockito.when(providers.getProvider(Mockito.any())).thenReturn(provider);
        chatService.registerChat(123L);

        var response = linkService.addLink(URI.create(url), 123L);
        Assertions.assertThat(linkRepository.findById(response.id()).get().getUrl())
            .isEqualTo(url);
        Assertions.assertThat(chatLinkRepository.findAllByChatId(123L)).map(Link::getUrl)
            .contains(url);
    }

    @Test
    @Transactional
    @Rollback
    void getLinkSubscriberShouldCorrectlyWork() {
        chatService.registerChat(123L);
        var id = linkRepository.add(Link.create("url", "github", OffsetDateTime.now(), OffsetDateTime.now()));
        chatLinkRepository.add(123L, id);

        var response = linkService.getLinkSubscribers(id);
        Assertions.assertThat(response).map(TgChat::getId).contains(123L);
    }

    @Test
    @Transactional
    @Rollback
    void removeLinkShouldThrowExceptionWhenLinkNotExist() {
        chatService.registerChat(123L);

        Assertions.assertThatThrownBy(() -> linkService.removeLink(1L, 123L))
            .isInstanceOf(LinkNotFoundException.class);
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jdbc");
    }
}
