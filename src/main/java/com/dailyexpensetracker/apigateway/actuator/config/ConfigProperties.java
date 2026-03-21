package com.dailyexpensetracker.apigateway.actuator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties
public class ConfigProperties {
  private String userServiceUrl;
}
