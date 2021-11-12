package takaichi00.springcloudgatewaysample;

import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
@AllArgsConstructor
public class SampleJavaRoute {

  private final SampleFilter sampleFilter;

  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
    String httpUri = uriConfiguration.getHttpbin();
    return builder.routes()
        .route(p -> p
            .path("/get")
            .and()
            .method(HttpMethod.GET)
            .filters(f -> f
                .addRequestHeader("Hello", "World")
                .addResponseHeader("CustomResponse", "Gateway was passed")
            )
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
}
