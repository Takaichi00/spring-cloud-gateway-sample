package takaichi00.springcloudgatewaysample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import takaichi00.springcloudgatewaysample.config.UriConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
public class SpringCloudGatewaySampleApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringCloudGatewaySampleApplication.class, args);
  }
}
