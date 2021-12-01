package takaichi00.springcloud.gateway.sample.circuitbreaker;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

class CircuitBreakerTestBase {
  protected final static WireMockServer wiremock = new WireMockServer(options().port(18080));

  @Autowired
  protected WebTestClient webTestClient;

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
}
