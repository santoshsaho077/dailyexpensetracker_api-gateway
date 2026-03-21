package com.dailyexpensetracker.apigateway.actuator;

import com.dailyexpensetracker.apigateway.actuator.config.ConfigProperties;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.web.reactive.function.client.WebClient;

@Endpoint(id = "stats")
public class StatsController {

  private final WebClient webClient;
  private ConfigProperties configProperties;

  public StatsController(ConfigProperties configProperties, WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
    this.configProperties = configProperties;
  }

  @ReadOperation
  public Map<String, String> getHealthStatuses() {
    Map<String, String> response = new HashMap<>();
    if (!configProperties.getUserServiceUrl().isBlank()) {
      String health =
          webClient
              .get()
              .uri(configProperties.getUserServiceUrl() + "/actuator/health")
              .retrieve()
              .bodyToMono(String.class)
              .block();
      response.put("User Service", health);
    }
    return response;
  }
}
