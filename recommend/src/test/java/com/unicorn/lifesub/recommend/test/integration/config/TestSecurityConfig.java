package com.unicorn.lifesub.recommend.test.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 통합 테스트를 위한 보안 설정 클래스입니다.
 * 테스트 환경에서 보안 설정을 간소화하여 테스트가 원활하게 수행되도록 합니다.
 */
@Configuration
@Profile("integration-test")
public class TestSecurityConfig {

    /**
     * 테스트 환경에서 사용할 보안 필터 체인을 구성합니다.
     * CSRF 보호를 비활성화하고 모든 API 경로에 대한 접근을 허용합니다.
     *
     * @param http 보안 설정을 위한 HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/recommend/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}