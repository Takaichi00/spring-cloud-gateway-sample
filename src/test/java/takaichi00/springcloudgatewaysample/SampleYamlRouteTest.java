package takaichi00.springcloudgatewaysample;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"http_bin=http://localhost:${wiremock.server.port}"})
@AutoConfigureWireMock(port = 0)
class SampleYamlRouteTest {

  @Autowired
  private WebTestClient webClient;

  @Test
  @Disabled
  void getHttpBin() {
//    stubFor(get(urlEqualTo("/get"))
//        .willReturn(aResponse()
//            .withBody("{\"headers\":{\"Hello\":\"World\"}}")
//            .withHeader("Content-Type", "application/json")));

    webClient
        .get().uri("/example")
        .exchange()
        .expectStatus().isOk();
//        .expectBody()
//        .jsonPath("$.example").isEqualTo("hello");
  }

}
