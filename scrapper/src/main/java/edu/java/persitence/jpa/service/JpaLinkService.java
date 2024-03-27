package edu.java.persitence.jpa.service;

import edu.java.domain.dto.Link;
import edu.java.domain.dto.TgChat;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.exception.ChatNotFoundException;
import edu.java.exception.LinkIsNotSupportedException;
import edu.java.persitence.jpa.entity.LinkEntity;
import edu.java.persitence.jpa.repository.JpaChatRepository;
import edu.java.persitence.jpa.repository.JpaLinkRepository;
import edu.java.provider.InformationProviders;
import edu.java.service.LinkService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatRepository chatRepository;
    private final InformationProviders informationProviders;

    @Override
    @Transactional
    public ListLinksResponse listLinks(Long tgChatId) {
        return chatRepository
            .findById(tgChatId).map(
                chat -> new ListLinksResponse(
                    chat.getLinks().stream().map(
                        link -> new LinkResponse(
                            link.getId(),
                            URI.create(link.getUrl())
                        )
                    ).toList()
                )
            ).orElseThrow(() -> new ChatNotFoundException(tgChatId));
    }

    @Override
    @Transactional
    public LinkResponse addLink(URI link, Long tgChatId) {
        var chat = chatRepository.findById(tgChatId).orElseThrow(() -> new ChatNotFoundException(tgChatId));
        var provider = informationProviders.getProvider(link.getHost());
        if (provider == null || !provider.isSupported(link)) {
            throw new LinkIsNotSupportedException(link);
        }
        var linkInformation = provider.fetchInformation(link);
        if (linkInformation == null) {
            throw new LinkIsNotSupportedException(link);
        }
        var lastModified = OffsetDateTime.now();
        if (!linkInformation.events().isEmpty()) {
            lastModified = linkInformation.events().getFirst().lastModified();
        }
        var optionalLink = linkRepository.findByUrl(link.toString());
        if (optionalLink.isPresent()) {
            LinkEntity linkEntity = optionalLink.get();
            linkEntity.setUpdatedAt(lastModified);
            linkEntity.setLastCheckedAt(OffsetDateTime.now());
            linkEntity.setMetaInformation(linkInformation.metaInformation());
            chat.addLink(linkEntity);
            return new LinkResponse(linkEntity.getId(), link);
        }
        var linkEntity = new LinkEntity(
            link.toString(),
            linkInformation.title(),
            lastModified,
            OffsetDateTime.now(),
            linkInformation.metaInformation()
        );
        linkRepository.save(linkEntity);
        chat.addLink(linkEntity);
        return new LinkResponse(linkEntity.getId(), link);
    }

    @Override
    @Transactional
    public LinkResponse removeLink(Long id, Long tgChatId) {
        var chat = chatRepository.findById(tgChatId).orElseThrow(() -> new ChatNotFoundException(tgChatId));
        var link = linkRepository.findById(id).orElseThrow();
        chat.removeLink(link);
        if (link.getTgChats().isEmpty()) {
            linkRepository.delete(link);
        }
        return new LinkResponse(id, URI.create(link.getUrl()));
    }

    @Override
    @Transactional
    public List<Link> listOldLinks(Duration after, int limit) {
        return linkRepository.findAllByLastCheckedAtBefore(OffsetDateTime.now().minus(after), Limit.of(limit))
            .stream().map(LinkEntity::toDto).toList();
    }

    @Override
    @Transactional
    public void update(long id, OffsetDateTime lastModified, String metaInformation) {
        var link = linkRepository.findById(id).orElseThrow();
        link.setUpdatedAt(lastModified);
        link.setMetaInformation(metaInformation);
    }

    @Override
    @Transactional
    public List<TgChat> getLinkSubscribers(long linkId) {
        var link = linkRepository.findById(linkId).orElseThrow();
        return link.getTgChats().stream().map(chat -> new TgChat(chat.getId())).toList();
    }

    @Override
    @Transactional
    public void checkNow(long linkId) {
        var link = linkRepository.findById(linkId).orElseThrow();
        link.setLastCheckedAt(OffsetDateTime.now());
    }
}
