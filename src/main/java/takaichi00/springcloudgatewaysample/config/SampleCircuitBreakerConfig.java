package takaichi00.springcloudgatewaysample.config;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleCircuitBreakerConfig {
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
        .timeLimiterConfig(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(10000))
            .build())
        .build());
  }

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> withRetryCustomizer() {
    return factory ->
        factory.configure(builder -> builder
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(5000))
                .build()),  "with-retry");
  }

  /*
  * reference: https://resilience4j.readme.io/docs/circuitbreaker
  * reference (Japanese): https://github.com/resilience4j-docs-ja/resilience4j-docs-ja/blob/master/core-modules/circuitbreaker.md
  */
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> sampleCustomizer() {
    return factory ->
        factory.configure(builder -> builder
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .failureRateThreshold(25)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofMillis(1000))
                .permittedNumberOfCallsInHalfOpenState(3)
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(0))
                .slidingWindowType(COUNT_BASED)
                .slidingWindowSize(20)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(5000))
                .build()),  "customize");
  }
}
