package takaichi00.springcloudgatewaysample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"http_bin=http://localhost:18080"})
class ErrorHandlingTest {

  @Autowired
  private WebTestClient webClient;

  @MockBean
  SampleFilter sampleFilter;

  @Test
  void 何らかの例外が発生した場合のテスト() {
    doThrow(new RuntimeException()).when(sampleFilter).filter(any(), any());
    webClient
        .get().uri("/get")
        .exchange()
        .expectStatus().isEqualTo(500)
        .expectBody()
        .jsonPath("$.message").isEqualTo("error has occurred");
  }
}
