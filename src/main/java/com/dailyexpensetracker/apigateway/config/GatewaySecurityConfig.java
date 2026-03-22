package com.dailyexpensetracker.apigateway.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

  // Store this in application.yml, e.g.: app.jwt.secret=your-256-bit-secret
  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity httpSecurity) {
    return httpSecurity
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers("/v1/user/**", "/v1/actuator/health")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt -> {
                      jwt.jwtDecoder(reactiveJwtDecoder()); // ✅ provide decoder
                      jwt.jwtAuthenticationConverter(
                          jwtAuthenticationConverter()); // ✅ reactive converter
                    }))
        .build();
  }

  /**
   * Decodes and validates the JWT signature using your HMAC secret key. Replace MacAlgorithm.HS256
   * with HS384/HS512 to match how tokens are signed.
   */
  @Bean
  public ReactiveJwtDecoder reactiveJwtDecoder() {
    log.info("Initializing JWT decoder with secret length: {}", jwtSecret.length());
    SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
    return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  }

  /**
   * Reactive-compatible JWT authentication converter. JwtGrantedAuthoritiesConverter maps JWT
   * claims → Spring Security authorities.
   */
  @Bean
  public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthorityPrefix("ROLE_"); // optional: customize prefix
    authoritiesConverter.setAuthoritiesClaimName("roles"); // match your JWT claim name

    ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt ->
            Flux.fromIterable(authoritiesConverter.convert(jwt)) // ✅ returns Flux<GrantedAuthority>
        );
    return converter;
  }
}
