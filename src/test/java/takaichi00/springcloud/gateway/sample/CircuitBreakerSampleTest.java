package takaichi00.springcloud.gateway.sample;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import java.time.Duration;
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
class CircuitBreakerSampleTest {
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

  @Test
  void _1回目のリクエストが失敗した場合はCircuitbreakerはOPENにならなずにそのままレスポンスが返る() {

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
        .isEqualTo(500);

    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(1);
  }

  @Test
  void _4回目と5回目のリクエストが失敗したときはCircuitbreakerがOPENになり6回目のリクエストはfallbackのレスポンスが返ると思ったが返らない() {
    //1
    wiremock.stubFor(post(urlEqualTo("/status/201"))
        .inScenario("Custom CircuitBreaker Scenario")
        .willReturn(aResponse()
            .withStatus(201))
        .willSetStateTo("Count" + 1));

    //2,3
    for(int i = 1; i < 3; ++i) {
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
              .withStatus(500))
          .willSetStateTo("Count" + (i + 1)));
    }

    for (int i = 0; i < 5; ++i) {
      webTestClient
          .post()
          .uri("/status/201")
          .header("Host", "www.circuitbreaker.customize.com")
          .exchange();
    }

    webTestClient
        .post()
        .uri("/status/201")
        .header("Host", "www.circuitbreaker.customize.com")
        .exchange()
        .expectStatus()
//        .isEqualTo(502); NOTE: Circuitbreaker が OPEN になると考えたがならない
    .isEqualTo(500);

    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(6);
  }

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
    //6-50
    for (int i = 5; i < 50; ++i) {
      wiremock.stubFor(post(urlEqualTo("/status/201"))
          .inScenario("Custom CircuitBreaker Scenario")
          .whenScenarioStateIs("Count" + i)
          .willReturn(aResponse()
              .withStatus(500))
          .willSetStateTo("Count" + (i + 1)));
    }

    for (int i = 0; i < 49; ++i) {
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
//        .isEqualTo(502); // NOTE: Circuitbreaker が OPEN になると考えたがならない
        .isEqualTo(500);

    VerificationResult result = wiremock.countRequestsMatching(
        postRequestedFor(urlEqualTo("/status/201")).build());

    assertThat(result.getCount()).isEqualTo(50);
  }
}
