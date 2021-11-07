package takaichi00.springcloudgatewaysample;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;

import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"http_bin=http://localhost:18080",
        "wiremock.reset-mappings-after-each-test=true"})
class DetailSampleRouteTest {

  private final static WireMockServer wiremock = new WireMockServer(options().port(18080));

  @Autowired
  private WebTestClient webTestClient;

  @BeforeAll
  static void setupVariables() {
    wiremock.start();
    WireMock.configureFor(18080);
  }

  @BeforeEach
  void setUp() {
    webTestClient = webTestClient.mutate()
        .responseTimeout(Duration.ofMillis(30000))
        .build();
    wiremock.resetAll();
  }

  @AfterEach
  public void restartWireMockServer() {
    if (!wiremock.isRunning()) {
      wiremock.start();
    }
  }

  @AfterAll
  static void tearDown() {
    wiremock.stop();
  }

  @Test
  void test_1回目のリクエストが500エラーで2回目のリクエストが200OKだった場合は200OKが返る() {
    wiremock.stubFor(get(urlEqualTo("/status/200"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(500))
        .willSetStateTo("Cause Success"));

    wiremock.stubFor(get(urlEqualTo("/status/200"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("Cause Success")
        .willReturn(aResponse()
            .withStatus(200)));

    webTestClient
        .get()
          .uri("/status/200")
          .header("x-api-key", "test")
        .exchange()
        .expectStatus()
          .isOk();

    VerificationResult result = wiremock.countRequestsMatching(
        getRequestedFor(urlEqualTo("/status/200")).build());

    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  void test_1回目のリクエストがタイムアウトで2回目のリクエストが200OKだった場合は200OKが返る() {
    wiremock.stubFor(get(urlEqualTo("/status/200"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(500))
        .willSetStateTo("Cause Success"));

    wiremock.stubFor(get(urlEqualTo("/status/200"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("Cause Success")
        .willReturn(aResponse()
            .withStatus(200)));

    webTestClient
        .get()
          .uri("/status/200")
          .header("x-api-key", "test")
        .exchange()
        .expectStatus()
          .isOk();

    VerificationResult result = wiremock.countRequestsMatching(
        getRequestedFor(urlEqualTo("/status/200")).build());

    assertThat(result.getCount()).isEqualTo(2);
  }

  @Test
  void test_2回ともタイムアウトだった場合は504が返る() {
    wiremock.stubFor(get(urlEqualTo("/status/200"))
        .inScenario("Retry Scenario")
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(500))
        .willSetStateTo("Cause Success"));

    wiremock.stubFor(get(urlEqualTo("/status/200"))
        .inScenario("Retry Scenario")
        .whenScenarioStateIs("Cause Success")
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(500)));

    webTestClient
        .get()
          .uri("/status/200")
          .header("x-api-key", "test")
        .exchange()
          .expectStatus()
          .isEqualTo(504);

    VerificationResult result = wiremock.countRequestsMatching(
        getRequestedFor(urlEqualTo("/status/200")).build());

    assertThat(result.getCount()).isEqualTo(2);
  }
}
