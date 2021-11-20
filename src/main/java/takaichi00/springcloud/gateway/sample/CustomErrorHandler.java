package takaichi00.springcloud.gateway.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class CustomErrorHandler implements ErrorWebExceptionHandler {
  @Override
  public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable ex) {

    log.error("exception has occurred. exception is:", ex);

    ServerHttpResponse response = serverWebExchange.getResponse();

    if (response.isCommitted()) {
      return Mono.error(ex);
    }
    response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    String responseJsonBody = "{\"message\":\"error has occurred\"}";
    DataBuffer dataBuffer = response.bufferFactory().wrap(responseJsonBody.getBytes());
    return response.writeWith(Mono.just(dataBuffer));
  }
}
