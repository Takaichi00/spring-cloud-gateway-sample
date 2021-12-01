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
        "server.port=12302"})
class CircuitBreaker_2Test extends CircuitBreakerTestBase {
  @Test
  void _1回目のリクエストで成功した場合() {

    wiremock.stubFor(post(urlEqualTo("/status/201"))
        .inScenario("Custom CircuitBreaker Scenario")
        .willReturn(aResponse()
            .withStatus(201))
        .willSetStateTo("Cause Success"));

    webTestClient
        .post()
        .uri("/status/201")
        .header("Host", "www.circuitbreaker.customize.com")
        .exchange()
        .expectStatus()
        .isEqualTo(201);

    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(1);
  }
}
