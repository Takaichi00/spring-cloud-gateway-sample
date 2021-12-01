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
        "server.port=12303"})
class CircuitBreaker_3Test extends CircuitBreakerTestBase {
  @Test
  void _1回目のリクエストが失敗した場合はCircuitbreakerはOPENにならなずにそのままfallbackのレスポンスが返る() {

    wiremock.stubFor(post(urlEqualTo("/status/201"))
        .inScenario("Custom CircuitBreaker Scenario")
        .willReturn(aResponse()
            .withStatus(500))
        .willSetStateTo("Cause Success"));

    webTestClient
        .post()
        .uri("/status/201")
        .header("Host", "www.circuitbreaker.customize.com")
        .exchange()
        .expectStatus()
        .isEqualTo(502);

    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(1);
  }
}
