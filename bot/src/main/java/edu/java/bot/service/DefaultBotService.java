package edu.java.bot.service;

import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.client.scrapper.dto.request.AddLinkRequest;
import edu.java.bot.client.scrapper.dto.request.RemoveLinkRequest;
import edu.java.bot.client.scrapper.dto.response.LinkResponse;
import edu.java.bot.client.scrapper.dto.response.ListLinksResponse;
import edu.java.bot.dto.OptionalAnswer;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultBotService implements BotService {

    private final ScrapperClient scrapperClient;

    @Override
    public OptionalAnswer<Void> registerUser(Long id) {
        return scrapperClient.registerChat(id);
    }

    @Override
    public OptionalAnswer<LinkResponse> linkUrlToUser(String url, Long userId) {
        return scrapperClient.addLink(userId, new AddLinkRequest(URI.create(url)));
    }

    @Override
    public OptionalAnswer<LinkResponse> unlinkUrlFromUser(Long linkId, Long userId) {
        return scrapperClient.removeLink(userId, new RemoveLinkRequest(linkId));
    }

    @Override
    public OptionalAnswer<ListLinksResponse> listLinks(Long userId) {
        return scrapperClient.listLinks(userId);
    }

    @PostConstruct
    public void init() {
        scrapperClient.deleteChat(1L);
    }
}
