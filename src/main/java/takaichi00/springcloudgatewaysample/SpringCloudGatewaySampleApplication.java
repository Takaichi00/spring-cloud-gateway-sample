package takaichi00.springcloudgatewaysample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
public class SpringCloudGatewaySampleApplication {

  @Autowired
  SampleFilter sampleFilter;

  public static void main(String[] args) {
    SpringApplication.run(SpringCloudGatewaySampleApplication.class, args);
  }

//  @Bean
//  public RouteLocator myRoutes(RouteLocatorBuilder builder) {
//    return builder.routes()
//        .route(p -> p
//          .path("/get")
//          .filters(f -> f.addRequestHeader("Hello", "World")) // Header "Hello" に "World" を追加
//          .uri("http://httpbin.org:80"))
//        .route(p -> p
//          .host("*.circuitbreaker.com")
//          .filters(f -> f.circuitBreaker(config -> config
//              .setName("mycmd")
//              .setFallbackUri("forward:/fallback")))
//          .uri("http://httpbin.org:80"))
//        .build();
//  }

  @Bean
  public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
    String httpUri = uriConfiguration.getHttpbin();
    return builder.routes()
        .route(p -> p
            .path("/get")
            .filters(f -> f.addRequestHeader("Hello", "World"))
            .uri(httpUri))
        .route(p -> p
            .path("/get/1").filters(f -> f.filter(sampleFilter))
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
