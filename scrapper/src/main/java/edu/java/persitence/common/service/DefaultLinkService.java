package edu.java.persitence.common.service;

import edu.java.domain.dto.Link;
import edu.java.domain.dto.TgChat;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.exception.LinkAlreadyAddedException;
import edu.java.exception.LinkIsNotSupportedException;
import edu.java.exception.LinkNotFoundException;
import edu.java.persitence.common.repository.LinkRepository;
import edu.java.persitence.common.repository.TgChatLinkRepository;
import edu.java.provider.InformationProviders;
import edu.java.provider.api.InformationProvider;
import edu.java.provider.api.LinkInformation;
import edu.java.service.LinkService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DefaultLinkService implements LinkService {

    private final LinkRepository linkRepository;
    private final TgChatLinkRepository tgChatLinkRepository;
    private final InformationProviders informationProviders;

    @Override
    public ListLinksResponse listLinks(Long tgChatId) {
        var links = tgChatLinkRepository.findAllByChatId(tgChatId);
        var linkResponses = links.stream()
            .map(link -> new LinkResponse(link.getId(), URI.create(link.getUrl())))
            .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    @Override
    @Transactional
    public LinkResponse addLink(URI link, Long tgChatId) {
        if (linkRepository.findByUrl(link.toString()).isPresent()) {
            throw new LinkAlreadyAddedException(link);
        }
        InformationProvider provider = informationProviders.getProvider(link.getHost());
        if (provider == null || !provider.isSupported(link)) {
            throw new LinkIsNotSupportedException(link);
        }
        LinkInformation linkInformation = provider.fetchInformation(link);
        if (linkInformation == null) {
            throw new LinkIsNotSupportedException(link);
        }
        OffsetDateTime lastModified = OffsetDateTime.now();
        if (!linkInformation.events().isEmpty()) {
            lastModified = linkInformation.events().getFirst().lastModified();
        }
        var id = linkRepository.add(Link.create(
            link.toString(),
            linkInformation.title(),
            lastModified,
            OffsetDateTime.now(),
            linkInformation.metaInformation()
        ));
        tgChatLinkRepository.add(tgChatId, id);
        return new LinkResponse(id, link);
    }

    @Override
    @Transactional
    public LinkResponse removeLink(Long id, Long tgChatId) {
        Optional<Link> optionalLink = linkRepository.findById(id);
        if (optionalLink.isPresent()) {
            Link link = optionalLink.get();
            tgChatLinkRepository.remove(tgChatId, id);
            if (tgChatLinkRepository.findAllByLinkId(id).isEmpty()) {
                linkRepository.remove(id);
            }
            return new LinkResponse(link.getId(), URI.create(link.getUrl()));
        } else {
            throw new LinkNotFoundException(id);
        }
    }

    @Override
    @Transactional
    public List<Link> listOldLinks(Duration after, int limit) {
        return linkRepository.findLinksCheckedAfter(after, limit);
    }

    @Override
    @Transactional
    public void update(long id, OffsetDateTime lastModified, String metaInformation) {
        if (linkRepository.findById(id).isEmpty()) {
            throw new LinkNotFoundException(id);
        }
        linkRepository.update(id, lastModified, metaInformation);
    }

    @Override
    @Transactional
    public List<TgChat> getLinkSubscribers(long linkId) {
        return tgChatLinkRepository.findAllByLinkId(linkId);
    }

    @Override
    @Transactional
    public void checkNow(long id) {
        linkRepository.checkNow(id);
    }
}
