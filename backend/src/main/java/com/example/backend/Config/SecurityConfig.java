package com.example.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

     protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/scorecard").permitAll() // Allow access to /scorecard
                .anyRequest().authenticated())
            .formLogin(form -> form.defaultSuccessUrl("/home", true)); // Configure form login using Lambda DSL
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Your frontend URL
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsSource) throws Exception {
        http
            // 1. Enable CORS (using your CorsConfigurationSource bean)
            .cors(cors -> cors.configurationSource(corsSource))

            // 2. CSRF protection (keep enabled for form login)
            .csrf(csrf -> csrf.disable())

            // 3. Authorize requests
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/scorecard", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // 4. Login configuration
            .formLogin(form -> form
                .loginPage("/login")                // your custom login page
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")                // same login page
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
            )

            // 5. Logout configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}