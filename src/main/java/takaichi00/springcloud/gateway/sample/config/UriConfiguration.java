package takaichi00.springcloud.gateway.sample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class UriConfiguration {
  private String httpBin;

  public String getHttpbin() {
    return httpBin;
  }

  public void setHttpbin(String httpbin) {
    this.httpBin = httpbin;
  }
}
