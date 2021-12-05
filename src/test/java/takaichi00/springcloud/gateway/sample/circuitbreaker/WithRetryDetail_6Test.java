package takaichi00.springcloud.gateway.sample.circuitbreaker;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"http_bin=http://localhost:18080",
        "wiremock.reset-mappings-after-each-test=true",
        "server.port=12306"})
class WithRetryDetail_6Test extends CircuitBreakerTestBase {

  @Test
  void _1回目がタイムアウト_2回目が成功の場合はリクエストが成功する() {
    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(500))
        .willSetStateTo("Cause Success"));

    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("Cause Success")
        .willReturn(aResponse()
            .withStatus(204)));

    webTestClient
        .get()
        .uri("/status/204")
        .header("Host", "www.circuitbreaker.with-retry.com")
        .exchange()
        .expectStatus()
        .isNoContent();

    VerificationResult result = wiremock.countRequestsMatching(
        getRequestedFor(urlEqualTo("/status/204")).build());

    assertThat(result.getCount()).isEqualTo(2);
  }
}
