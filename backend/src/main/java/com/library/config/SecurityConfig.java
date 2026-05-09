package com.library.config;

import com.library.security.JwtAccessDeniedHandler;
import com.library.security.JwtAuthenticationEntryPoint;
import com.library.security.JwtAuthenticationFilter;
import com.library.security.LoginRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final XssSqlFilter xssSqlFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/register", "/auth/forgot-password/**").permitAll()
                        .requestMatchers("/captcha/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books", "/books/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories", "/categories/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/notices", "/notices/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publishers/{id}").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/doc.html", "/webjars/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/book-resources/download/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/upload/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "LIBRARIAN", "READER")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/borrows/self").authenticated()
                        .requestMatchers(HttpMethod.GET, "/borrows/my").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/borrows/*/renew").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/borrows/*/self-return").authenticated()
                        .requestMatchers("/borrows/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/inventory/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/statistics/**").hasAnyRole("ADMIN", "LIBRARIAN", "READER")
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/borrow-codes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/publishers").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/notices").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/notices/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/notices/**").hasAnyRole("ADMIN")
                        .requestMatchers("/bookshelves/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/permissions/**").hasRole("ADMIN")
                        .requestMatchers("/book-normalize/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/purchases/**").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/depts/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(xssSqlFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(securityHeadersFilter, XssSqlFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration(LoginRateLimitFilter filter) {
        FilterRegistrationBean<LoginRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(SecurityHeadersFilter filter) {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<XssSqlFilter> xssSqlFilterRegistration(XssSqlFilter filter) {
        FilterRegistrationBean<XssSqlFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
