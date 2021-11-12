package takaichi00.springcloudgatewaysample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"http_bin=http://localhost:${wiremock.server.port}",
                  "wiremock.reset-mappings-after-each-test=true"})
@AutoConfigureWireMock(port = 18080)
class SampleYamlRouteTest {

  @Autowired
  private WebTestClient webClient;

  @Test
  void yamlに設定してRouteをテストすることができる() {
    stubFor(get(urlEqualTo("/"))
        .willReturn(aResponse()
            .withBody("{\"example\":\"Hello\"}")
            .withHeader("Content-Type", "application/json")));

    webClient
        .get().uri("/example")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.example").isEqualTo("Hello");

    verify(1, getRequestedFor(urlEqualTo("/")));
  }
}
