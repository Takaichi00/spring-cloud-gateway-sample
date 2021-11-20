package takaichi00.springcloud.gateway.sample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Getter
@Setter
public class SampleConfigurationProperties {
  private String httpBin;
  private Sample sample;

  @Getter
  @Setter
  public static class Sample {
    private Integer firstBackoff;
    private Integer maxBackoff;
    private Integer factor;
  }
}
