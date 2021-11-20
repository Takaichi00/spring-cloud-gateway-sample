package takaichi00.springcloud.gateway.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import takaichi00.springcloud.gateway.sample.config.UriConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
public class SpringCloudGatewaySampleApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringCloudGatewaySampleApplication.class, args);
  }
}
