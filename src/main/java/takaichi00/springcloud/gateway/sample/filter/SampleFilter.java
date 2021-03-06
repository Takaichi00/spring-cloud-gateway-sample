package takaichi00.springcloud.gateway.sample.filter;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Reference: https://www.fixes.pub/program/199584.html

@RefreshScope
@Component
public class SampleFilter implements GatewayFilter {

  @Order(HIGHEST_PRECEDENCE)
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    //Make your business logic, this is a simple sample.
    if (!request.getHeaders().containsKey("x-api-key")) {
      return this.onError(exchange, HttpStatus.FORBIDDEN);
    }
    return chain.filter(exchange); //Forward to route
  }

  private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus)  {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(httpStatus);
    return response.setComplete();
  }
}
