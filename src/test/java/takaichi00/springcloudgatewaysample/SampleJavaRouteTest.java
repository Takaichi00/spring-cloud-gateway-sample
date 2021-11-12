package takaichi00.springcloudgatewaysample;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"http_bin=http://localhost:${wiremock.server.port}"})
@AutoConfigureWireMock(port = 0)
class SampleJavaRouteTest {

  @Autowired
  private WebTestClient webClient;

  @Test
  void getHttpBin() {
    stubFor(get(urlEqualTo("/get"))
        .willReturn(aResponse()
            .withBody("{\"headers\":{\"Hello\":\"World\"}}")
            .withHeader("Content-Type", "application/json")));

    webClient
        .get().uri("/get")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().valueEquals("CustomResponse", "Gateway was passed")
        .expectBody()
        .jsonPath("$.headers.Hello").isEqualTo("World");
  }

  @Test
  void 許可していないHttpMethodの場合は404エラーが返る() {
    stubFor(get(urlEqualTo("/get"))
        .willReturn(aResponse()
            .withBody("{\"headers\":{\"Hello\":\"World\"}}")
            .withHeader("Content-Type", "application/json")));

    webClient
        .post().uri("/get")
        .exchange()
        .expectStatus()
        .isNotFound();

    verify(0, getRequestedFor(urlEqualTo("/get")));
  }

  @Test
  void circuitbreaker() {
    stubFor(get(urlEqualTo("/delay/3"))
        .willReturn(aResponse()
            .withBody("no fallback")
            .withFixedDelay(3000)));
    webClient
        .get().uri("/delay/3")
        .header("Host", "www.circuitbreaker.com")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(
            response -> assertThat(response.getResponseBody()).isEqualTo("fallback".getBytes()));
  }

  @Test
  void filterOkTest() {
    stubFor(get(urlEqualTo("/status/200"))
        .willReturn(aResponse()
            .withBody("{\"headers\":{\"Hello\":\"World\"}}")
            .withHeader("Content-Type", "application/json")));

    webClient
        .get().uri("/status/200").header("x-api-key", "test")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void filterFailTest() {
    stubFor(get(urlEqualTo("/status/200"))
        .willReturn(aResponse()
            .withBody("{\"headers\":{\"Hello\":\"World\"}}")
            .withHeader("Content-Type", "application/json")));

    webClient
        .get().uri("/status/200")
        .exchange()
        .expectStatus().isForbidden();
  }
}
