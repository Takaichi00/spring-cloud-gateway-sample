package takaichi00.springcloud.gateway.sample.circuitbreaker;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"http_bin=http://localhost:18080",
        "wiremock.reset-mappings-after-each-test=true",
        "server.port=12304"})
class CircuitBreaker_4Test extends CircuitBreakerTestBase {
  @Test
  void 十分なリクエストが失敗したときはCircuitbreakerがOPENになりfallbackのレスポンスが返るか検証() {
    //1
    wiremock.stubFor(post(urlEqualTo("/status/201"))
        .inScenario("Custom CircuitBreaker Scenario")
        .willReturn(aResponse()
            .withStatus(201))
        .willSetStateTo("Count" + 1));

    //2-5
    for(int i = 1; i < 5; ++i) {
      wiremock.stubFor(post(urlEqualTo("/status/201"))
          .inScenario("Custom CircuitBreaker Scenario")
          .whenScenarioStateIs("Count" + i)
          .willReturn(aResponse()
              .withStatus(201))
          .willSetStateTo("Count" + (i + 1)));
    }
    //6-30
    for (int i = 5; i < 30; ++i) {
      wiremock.stubFor(post(urlEqualTo("/status/201"))
          .inScenario("Custom CircuitBreaker Scenario")
          .whenScenarioStateIs("Count" + i)
          .willReturn(aResponse()
              .withStatus(500))
          .willSetStateTo("Count" + (i + 1)));
    }

    for (int i = 0; i < 29; ++i) {
      webTestClient
          .post()
          .uri("/status/201")
          .header("Host", "www.circuitbreaker.customize.com")
          .exchange();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    webTestClient
        .post()
        .uri("/status/201")
        .header("Host", "www.circuitbreaker.customize.com")
        .exchange()
        .expectStatus()
        .isEqualTo(502);

    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(9);
  }
}
