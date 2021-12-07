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
            .withStatus(204)
            .withFixedDelay(500))
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

  @Test
  void _2回目までタイムアウト_3回目が成功の場合はリクエストが成功する() {
    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(204)
            .withFixedDelay(500))
        .willSetStateTo("1"));

    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("1")
        .willReturn(aResponse()
            .withStatus(204)
            .withFixedDelay(500))
        .willSetStateTo("2"));

    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("2")
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

    assertThat(result.getCount()).isEqualTo(3);
  }

  @Test
  void _3回目タイムアウトしたらfallbackのエンドポイントにforwardされる() {
    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(204)
            .withFixedDelay(500))
        .willSetStateTo("1"));

    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("1")
        .willReturn(aResponse()
            .withStatus(204)
            .withFixedDelay(500))
        .willSetStateTo("2"));

    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("2")
        .willReturn(aResponse()
            .withFixedDelay(500)
            .withStatus(204)));

    webTestClient
        .get()
        .uri("/status/204")
        .header("Host", "www.circuitbreaker.with-retry.com")
        .exchange()
        .expectStatus()
        .isEqualTo(504);

    VerificationResult result = wiremock.countRequestsMatching(
        getRequestedFor(urlEqualTo("/status/204")).build());

    assertThat(result.getCount()).isEqualTo(3);
  }

  @Test
  void _10回リクエストがタイムアウトしてもCircuitBreakerがOPENにはならない() {
    // 1
    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(204)
            .withFixedDelay(500))
        .willSetStateTo("Retry-1"));

    // 2-30
    for (int i = 1; i < 30; ++i) {
      wiremock.stubFor(get(urlEqualTo("/status/204"))
          .inScenario("Retry Scenario")
          .whenScenarioStateIs("Retry-" + i)
          .willReturn(aResponse()
              .withStatus(204)
              .withFixedDelay(500))
          .willSetStateTo("Retry-" + (i + 1)));
    }

    // 31
    wiremock.stubFor(get(urlEqualTo("/status/204"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("Retry-30")
        .willReturn(aResponse()
            .withStatus(204))
        .willSetStateTo("Cause Success"));

    for (int i = 0; i < 10; ++i) {
      webTestClient
          .get()
          .uri("/status/204")
          .header("Host", "www.circuitbreaker.with-retry.com")
          .exchange()
          .expectStatus()
          .isEqualTo(504);
    }

    webTestClient
        .get()
        .uri("/status/204")
        .header("Host", "www.circuitbreaker.with-retry.com")
        .exchange()
        .expectStatus()
//        .isEqualTo(504);
        .isEqualTo(204);

    VerificationResult result = wiremock.countRequestsMatching(
        getRequestedFor(urlEqualTo("/status/204")).build());

    assertThat(result.getCount()).isEqualTo(31);
  }
}
