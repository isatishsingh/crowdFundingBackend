package com.crowdfunding_backend.config;

import com.crowdfunding_backend.exception.ErrorResponse;
import com.crowdfunding_backend.security.JwtFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired private JwtFilter jwtFilter;

  @Autowired private ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    http.csrf(csrf -> csrf.disable())
        .exceptionHandling(
            exceptions
            -> exceptions
                   .authenticationEntryPoint(
                       (request, response, authException)
                           -> writeJsonError(response, 401, "AUTH_REQUIRED",
                                           "Please sign in to continue."))
                   .accessDeniedHandler(
                       (request, response, accessDeniedException)
                           -> writeJsonError(
                               response, 403, "ACCESS_DENIED",
                               "You must be signed in as a creator to complete verification.")))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())

        .sessionManagement(
            session
            -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(
            auth
            -> auth.requestMatchers("/api/users/login", "/api/users/register")
                   .permitAll()

                   .requestMatchers(HttpMethod.GET, "/api/users")
                   .permitAll()

                   .requestMatchers("/api/users/me")
                   .authenticated()

                   .requestMatchers("/api/webhook/razorpay")
                   .permitAll()

                   .requestMatchers(HttpMethod.OPTIONS, "/**")
                   .permitAll()

                   .requestMatchers("/api/users/creator/**")
                   .hasRole("CREATOR")

                   .requestMatchers("/api/users/investor/**")
                   .hasRole("INVESTOR")

                   .requestMatchers("/api/creator/**")
                   .hasRole("CREATOR")

                   .requestMatchers("/api/projects/mine")
                   .hasRole("CREATOR")

                   .requestMatchers(HttpMethod.GET, "/api/projects/**")
                   .permitAll()

                   .requestMatchers(HttpMethod.POST, "/api/projects")
                   .hasRole("CREATOR")

                   .requestMatchers(HttpMethod.PUT, "/api/projects/**")
                   .hasRole("CREATOR")

                   .requestMatchers(HttpMethod.DELETE, "/api/projects/**")
                   .hasRole("CREATOR")

                   .requestMatchers("/api/investments/**")
                   .hasRole("INVESTOR")

                   .requestMatchers("/api/subscriptions/**")
                   .authenticated()

                   .requestMatchers("/api/payments/receipt/**")
                   .authenticated()

                   .requestMatchers(HttpMethod.POST,
                                    "/api/investment-request")
                   .hasRole("INVESTOR")

                   .requestMatchers(HttpMethod.GET,
                                    "/api/investment-request/investor")
                   .hasRole("INVESTOR")

                   .requestMatchers(HttpMethod.GET,
                                    "/api/investment-request/customer")
                   .hasRole("CREATOR")

                   .requestMatchers(HttpMethod.POST,
                                    "/api/investment-request/*/approve",
                                    "/api/investment-request/*/reject")
                   .hasRole("CREATOR")

                   .requestMatchers("/admin/login")
                   .permitAll()

                   .requestMatchers("/ws-chat/**")
                   .permitAll()

                   .anyRequest()
                   .authenticated())

        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of("*")); // or your frontend URL
    configuration.setAllowedMethods(
        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  private void writeJsonError(jakarta.servlet.http.HttpServletResponse response,
                              int status, String code, String message)
      throws java.io.IOException {

    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse body = ErrorResponse.builder()
                             .message(message)
                             .code(code)
                             .status(status)
                             .timestamp(LocalDateTime.now())
                             .build();

    objectMapper.writeValue(response.getWriter(), body);
  }
}