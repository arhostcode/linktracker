package edu.java.provider.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@Getter
public abstract class EventCollectableInformationProvider<T> extends WebClientInformationProvider {

    private final Map<String, Function<T, LinkUpdateEvent>> linkUpdateEventsCollector = new HashMap<>();

    public EventCollectableInformationProvider(WebClient webClient) {
        super(webClient);
    }

    public EventCollectableInformationProvider(String apiUrl) {
        super(apiUrl);
    }

    public EventCollectableInformationProvider(String apiUrl, Retry retry) {
        super(apiUrl, retry);
    }

    public void registerCollector(String type, Function<T, LinkUpdateEvent> collector) {
        linkUpdateEventsCollector.put(type, collector);
    }

    public Function<T, LinkUpdateEvent> getCollector(String type) {
        return linkUpdateEventsCollector.get(type);
    }
}
