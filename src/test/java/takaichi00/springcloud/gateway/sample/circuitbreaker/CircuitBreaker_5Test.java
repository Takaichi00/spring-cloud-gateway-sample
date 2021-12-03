package takaichi00.springcloud.gateway.sample.circuitbreaker;

import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"http_bin=http://localhost:18080",
        "wiremock.reset-mappings-after-each-test=true",
        "server.port=12305"})
class CircuitBreaker_5Test extends CircuitBreakerTestBase {

  @Test
  void _4回目と5回目のリクエストがタイムアウトで失敗したときはCircuitbreakerがOPENになり6回目のリクエストはBackendへRoutingしない () {
    //1
    wiremock.stubFor(post(urlEqualTo("/status/201"))
        .inScenario("Custom CircuitBreaker Scenario")
        .willReturn(aResponse()
            .withStatus(201))
        .willSetStateTo("Count" + 1));

    //2,3
    for (int i = 1; i < 3; ++i) {
      wiremock.stubFor(post(urlEqualTo("/status/201"))
          .inScenario("Custom CircuitBreaker Scenario")
          .whenScenarioStateIs("Count" + i)
          .willReturn(aResponse()
              .withStatus(201))
          .willSetStateTo("Count" + (i + 1)));
    }
    //4,5,6
    for (int i = 3; i < 6; ++i) {
      wiremock.stubFor(post(urlEqualTo("/status/201"))
          .inScenario("Custom CircuitBreaker Scenario")
          .whenScenarioStateIs("Count" + i)
          .willReturn(aResponse()
              .withStatus(201)
              .withFixedDelay(550))
          .willSetStateTo("Count" + (i + 1)));
    }

    for (int i = 0; i < 3; ++i) {
      webTestClient
          .post()
          .uri("/status/201")
          .header("Host", "www.circuitbreaker.customize.com")
          .exchange().expectStatus().isEqualTo(201);
    }

    for (int i = 0; i < 3; ++i) {
      webTestClient
          .post()
          .uri("/status/201")
          .header("Host", "www.circuitbreaker.customize.com")
          .exchange().expectStatus().isEqualTo(502);
    }


    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(5);
  }
}
