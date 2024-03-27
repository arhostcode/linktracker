package edu.java.bot.util.retry;

import edu.java.bot.configuration.RetryConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@UtilityClass
public class RetryFactory {

    private static final Map<String, Function<RetryConfig.RetryElement, Retry>> RETRY_BUILDERS = new HashMap<>();

    static {
        RETRY_BUILDERS.put(
            "fixed",
            retryElement -> RetryBackoffSpec.fixedDelay(retryElement.maxAttempts(), retryElement.minDelay())
                .filter(buildErrorFilter(retryElement.codes()))
        );
        RETRY_BUILDERS.put(
            "exponential",
            retryElement -> RetryBackoffSpec.backoff(retryElement.maxAttempts(), retryElement.minDelay())
                .maxBackoff(retryElement.maxDelay()).filter(buildErrorFilter(retryElement.codes()))
        );
        RETRY_BUILDERS.put(
            "linear",
            retryElement -> LinearRetryBackoffSpec.linear(retryElement.maxAttempts(), retryElement.minDelay())
                .factor(retryElement.factor()).filter(buildErrorFilter(retryElement.codes()))
        );
    }

    public static ExchangeFilterFunction createFilter(Retry retry) {
        return (response, next) -> next.exchange(response)
            .flatMap(clientResponse -> {
                if (clientResponse.statusCode().isError()) {
                    return clientResponse.createError();
                } else {
                    return Mono.just(clientResponse);
                }
            }).retryWhen(retry);
    }

    public static Retry createRetry(RetryConfig config, String target) {
        return config.targets().stream().filter(element -> element.target().equals(target)).findFirst()
            .map(element -> RETRY_BUILDERS.get(element.type()).apply(element))
            .orElseThrow(() -> new IllegalStateException("Unknown target " + target));
    }

    private static Predicate<Throwable> buildErrorFilter(List<Integer> retryCodes) {
        return retrySignal -> {
            if (retrySignal instanceof WebClientResponseException e) {
                return retryCodes.contains(e.getStatusCode().value());
            }
            return true;
        };
    }

}
