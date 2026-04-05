package com.crowdfunding_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class AppConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    return username -> null; // disables default Spring Security user
  }
}