package edu.java.provider.github;

import edu.java.configuration.ApplicationConfig;
import edu.java.configuration.RetryConfig;
import edu.java.provider.api.EventCollectableInformationProvider;
import edu.java.provider.api.LinkInformation;
import edu.java.provider.api.LinkUpdateEvent;
import edu.java.provider.github.model.GithubEventInfo;
import edu.java.provider.github.model.GithubEventsHolder;
import edu.java.util.retry.RetryFactory;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GithubInformationProvider extends EventCollectableInformationProvider<GithubEventInfo> {

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile("https://github.com/(.+)/(.+)");
    private static final int MAX_PER_UPDATE = 10;

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    @Autowired
    public GithubInformationProvider(
        @Value("${provider.github.url}") String apiUrl,
        ApplicationConfig config,
        RetryConfig retryConfig
    ) {
        super(WebClient.builder()
            .baseUrl(apiUrl)
            .defaultHeaders(headers -> {
                if (config.githubToken() != null) {
                    headers.set("Authorization", "Bearer " + config.githubToken());
                }
            })
            .filter(RetryFactory.createFilter(RetryFactory.createRetry(retryConfig, "github")))
            .build()
        );
        registerCollector(
            "PushEvent",
            item -> new LinkUpdateEvent(
                "github.push_event",
                item.lastModified(),
                Map.of(
                    "size", String.valueOf(item.payload().size()),
                    "repo", String.valueOf(item.repo().name()),
                    "user", String.valueOf(item.actor().login())
                )
            )
        );
        registerCollector(
            "IssueCommentEvent",
            item -> new LinkUpdateEvent(
                "github.issue_comment_event",
                item.lastModified(),
                Map.of(
                    "title", item.payload().issue().title(),
                    "user", String.valueOf(item.actor().login())
                )
            )
        );
        registerCollector(
            "IssuesEvent",
            item -> new LinkUpdateEvent(
                "github.issues_event",
                item.lastModified(),
                Map.of(
                    "title", item.payload().issue().title(),
                    "repo", String.valueOf(item.repo().name())
                )
            )
        );
        registerCollector(
            "PullRequestEvent",
            item -> new LinkUpdateEvent(
                "github.pull_request_event",
                item.lastModified(),
                Map.of(
                    "title", item.payload().pullRequest().title(),
                    "repo", String.valueOf(item.repo().name())
                )
            )
        );
        registerCollector(
            "CreateEvent",
            item -> new LinkUpdateEvent(
                "github.create_event",
                item.lastModified(),
                Map.of(
                    "ref", String.valueOf(item.payload().ref()),
                    "ref_type", String.valueOf(item.payload().refType()),
                    "repo", String.valueOf(item.repo().name())
                )
            )
        );
    }

    @Override
    public boolean isSupported(URI url) {
        return REPOSITORY_PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getType() {
        return "github.com";
    }

    @Override
    public LinkInformation fetchInformation(URI url) {
        if (!isSupported(url)) {
            return null;
        }
        var info = executeRequest(
            "/repos/" + url.getPath() + "/events?per_page=" + MAX_PER_UPDATE,
            GithubEventsHolder.class,
            GithubEventsHolder.EMPTY
        );

        if (info == null || info.equals(GithubEventsHolder.EMPTY)) {
            return null;
        }
        return new LinkInformation(
            url,
            !info.events().isEmpty() ? info.events().getFirst().repo().name() : "",
            info.events().stream().map(it -> {
                var collector = getCollector(it.type());
                if (collector == null) {
                    return new LinkUpdateEvent(
                        "github." + it.type().toLowerCase(),
                        it.lastModified(),
                        Map.of("type", it.type())
                    );
                }
                return getCollector(it.type()).apply(it);
            }).toList()
        );
    }

    @Override
    public LinkInformation filter(LinkInformation info, OffsetDateTime after, String optionalContext) {
        List<LinkUpdateEvent> realUpdates =
            info.events().stream().filter(event -> event.lastModified().isAfter(after)).toList();
        return new LinkInformation(
            info.url(),
            info.title(),
            realUpdates
        );
    }
}
