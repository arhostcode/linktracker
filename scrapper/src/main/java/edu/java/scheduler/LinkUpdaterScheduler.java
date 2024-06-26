package edu.java.scheduler;

import edu.java.client.bot.request.LinkUpdate;
import edu.java.configuration.ApplicationConfig;
import edu.java.domain.dto.Link;
import edu.java.domain.dto.TgChat;
import edu.java.provider.InformationProviders;
import edu.java.provider.api.InformationProvider;
import edu.java.provider.api.LinkInformation;
import edu.java.service.LinkService;
import edu.java.service.LinkUpdateSender;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private final LinkService linkService;
    private final ApplicationConfig appConfig;
    private final InformationProviders informationProviders;
    private final LinkUpdateSender sender;

    @Scheduled(fixedDelayString = "#{@'app-edu.java.configuration.ApplicationConfig'.scheduler.interval}")
    public void update() {
        log.info("Update started");
        linkService.listOldLinks(appConfig.scheduler().forceCheckDelay(), appConfig.scheduler().maxLinksPerCheck())
            .forEach(link -> {
                log.info("Updating link {}", link);
                URI uri = URI.create(link.getUrl());
                InformationProvider provider = informationProviders.getProvider(uri.getHost());
                LinkInformation linkInformation = provider.fetchInformation(uri);
                linkInformation = provider.filter(linkInformation, link.getUpdatedAt(), link.getMetaInformation());
                processLinkInformation(linkInformation, link);
            });
        log.info("Update finished");
    }

    private void processLinkInformation(LinkInformation linkInformation, Link link) {
        if (linkInformation.events().isEmpty()) {
            linkService.checkNow(link.getId());
            return;
        }
        linkService.update(
            link.getId(),
            linkInformation.events().getFirst().lastModified(),
            linkInformation.metaInformation()
        );
        var subscribers = linkService.getLinkSubscribers(link.getId()).stream()
            .map(TgChat::getId)
            .toList();
        linkInformation.events().reversed()
            .forEach(event -> sender.sendUpdate(new LinkUpdate(
                link.getId(),
                URI.create(link.getUrl()),
                event.type(),
                subscribers,
                event.additionalData()
            )));
    }

}
