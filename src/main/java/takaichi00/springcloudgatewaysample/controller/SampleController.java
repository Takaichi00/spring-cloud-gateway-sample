package takaichi00.springcloudgatewaysample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class SampleController {

  @RequestMapping("/fallback")
  public Mono<String> fallback() {
    return Mono.just("fallback");
  }

  @RequestMapping("/fallback/with-retry")
  @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
  public void fallbackWithRetry() {
    log.error("backend call with retry is timeout");
  }
}
