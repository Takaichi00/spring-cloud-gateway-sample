package takaichi00.springcloudgatewaysample.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class GlobalFilterSample {

  @Bean
  @Order(-1)
  public GlobalFilter firstOrderFilter() {
    return (exchange, chain) -> {
      log.info("first pre filter");
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        log.info("third post filter");
      }));
    };
  }

  @Bean
  @Order(0)
  public GlobalFilter secondOrderFilter() {
    return (exchange, chain) -> {
      log.info("second pre filter");
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        log.info("second post filter");
      }));
    };
  }

  @Bean
  @Order(1)
  public GlobalFilter thirdOrderFilter() {
    return (exchange, chain) -> {
      log.info("third pre filter");
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        log.info("first post filter");
      }));
    };
  }
}
