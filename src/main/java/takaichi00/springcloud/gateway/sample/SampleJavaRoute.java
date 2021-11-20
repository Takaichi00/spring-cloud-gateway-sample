package takaichi00.springcloud.gateway.sample;

import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import takaichi00.springcloud.gateway.sample.config.UriConfiguration;
import takaichi00.springcloud.gateway.sample.filter.SampleFilter;

@Configuration
@AllArgsConstructor
public class SampleJavaRoute {

  private final SampleFilter sampleFilter;

  @Bean
  public RouteLocator sampleRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
    String httpUri = uriConfiguration.getHttpbin();
    return builder.routes()
        .route(p -> p
            .path("/get")
            .and()
            .method(HttpMethod.GET)
            .filters(f -> f
                .addRequestHeader("Hello", "World")
                .addResponseHeader("CustomResponse", "Gateway was passed"))
            .uri(httpUri))
        .route(p -> p
            .path("/status/200").filters(f -> f
                .filter(sampleFilter)
                .retry(retryConfig -> retryConfig
                  .setRetries(1)
                  .setBackoff(Duration.ofMillis(10), Duration.ofMillis(50), 2, false)
                  .setSeries(HttpStatus.Series.SERVER_ERROR)
                  .setMethods(HttpMethod.GET)))
            .uri(httpUri))
        .route(p -> p
            .host("*.circuitbreaker.com")
            .filters(f -> f
                .circuitBreaker(config -> config
                    .setName("mycmd")
                    .setFallbackUri("forward:/fallback")))
            .uri(httpUri))
        .build();
  }

  @Bean
  public RouteLocator retryAndCircuitbreakerRoutes(RouteLocatorBuilder builder,
                                                   UriConfiguration uriConfiguration) {
    String httpUri = uriConfiguration.getHttpbin();
    return builder.routes().route(p -> p
            .host("*.circuitbreaker.with-retry.com")
            .filters(f -> f
                .circuitBreaker(config -> config
                    .setName("with-retry")
                    .setFallbackUri("forward:/fallback/with-retry"))
                .retry(retryConfig -> retryConfig
                .setRetries(2)
                .setBackoff(Duration.ofMillis(10),
                            Duration.ofMillis(50), 2, false)
                .setSeries(HttpStatus.Series.SERVER_ERROR)
                .setMethods(HttpMethod.GET, HttpMethod.POST))
            )
            .metadata(CONNECT_TIMEOUT_ATTR, 50)
            .metadata(RESPONSE_TIMEOUT_ATTR, 100)
            .uri(httpUri))
        .build();
  }

  @Bean
  public RouteLocator customizeCircuitbreakerRoutes(RouteLocatorBuilder builder,
                                                   UriConfiguration uriConfiguration) {
    String httpUri = uriConfiguration.getHttpbin();
    return builder.routes().route(p -> p
            .host("*.circuitbreaker.customize.com")
            .filters(f -> f
                .circuitBreaker(config -> config
                    .setName("customize")
                    .setFallbackUri("forward:/fallback")))
            .metadata(CONNECT_TIMEOUT_ATTR, 50)
            .metadata(RESPONSE_TIMEOUT_ATTR, 100)
            .uri(httpUri))
        .build();
  }
}
