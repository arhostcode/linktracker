package edu.java.service;

import edu.java.domain.dto.Link;
import edu.java.domain.dto.TgChat;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkService {

    ListLinksResponse listLinks(Long tgChatId);

    LinkResponse addLink(URI link, Long tgChatId);

    LinkResponse removeLink(Long id, Long tgChatId);

    List<Link> listOldLinks(Duration after, int limit);

    void update(long id, OffsetDateTime lastModified, String metaInformation);

    List<TgChat> getLinkSubscribers(long linkId);

    void checkNow(long id);
}
