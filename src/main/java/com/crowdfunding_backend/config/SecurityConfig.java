package com.crowdfunding_backend.config;

import com.crowdfunding_backend.security.JwtFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())

        .sessionManagement(
            session
            -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(
            auth
            -> auth.requestMatchers("/api/users/login", "/api/users",
                                    "/api/users/register")
                   .permitAll()

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

                   .requestMatchers("/api/projects/**")
                   .permitAll()

                   .requestMatchers("/api/projects")
                   .authenticated()

                   .requestMatchers("/api/investments/**")
                   .hasRole("INVESTOR")

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
}