package edu.java.util.retry;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.retry.ExhaustedRetryException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
public class LinearRetryBackoffSpec extends Retry {
    private static final Duration MAX_BACKOFF = Duration.ofMillis(Long.MAX_VALUE);
    private final Duration minBackoff;
    private final Duration maxBackoff;
    private final double factor;
    private final int maxAttempts;
    private final Predicate<Throwable> errorFilter;
    private final Supplier<Scheduler> schedulerSupplier;

    public LinearRetryBackoffSpec factor(double factor) {
        return new LinearRetryBackoffSpec(
            this.minBackoff,
            this.maxBackoff,
            factor,
            this.maxAttempts,
            this.errorFilter,
            this.schedulerSupplier
        );
    }

    public LinearRetryBackoffSpec filter(Predicate<Throwable> errorFilter) {
        return new LinearRetryBackoffSpec(
            this.minBackoff,
            this.maxBackoff,
            this.factor,
            this.maxAttempts,
            errorFilter,
            this.schedulerSupplier
        );
    }

    public static LinearRetryBackoffSpec linear(int maxAttempts, Duration minDelay) {
        return new LinearRetryBackoffSpec(
            minDelay,
            MAX_BACKOFF,
            1.0,
            maxAttempts,
            e -> true,
            Schedulers::parallel
        );
    }

    @Override
    public Publisher<?> generateCompanion(Flux<RetrySignal> retrySignals) {
        return Flux.deferContextual(cv ->
            retrySignals.contextWrite(cv)
                .concatMap(retryWhenState -> {
                    RetrySignal copy = retryWhenState.copy();
                    Throwable currentFailure = copy.failure();
                    long iteration = copy.totalRetries();
                    if (currentFailure == null) {
                        return Mono.error(new IllegalStateException(
                            "Retry.RetrySignal#failure() not expected to be null"));
                    }
                    if (!errorFilter.test(currentFailure)) {
                        return Mono.error(currentFailure);
                    }
                    if (iteration >= maxAttempts) {
                        return Mono.error(new ExhaustedRetryException("Retry exhausted: " + this));
                    }

                    Duration nextBackoff;
                    try {
                        nextBackoff = minBackoff.multipliedBy((long) (iteration * factor));
                        if (nextBackoff.compareTo(maxBackoff) > 0) {
                            nextBackoff = maxBackoff;
                        }
                    } catch (ArithmeticException overflow) {
                        nextBackoff = maxBackoff;
                    }

                    return Mono.delay(nextBackoff, schedulerSupplier.get()).contextWrite(cv);
                })
                .onErrorStop()
        );
    }

}
