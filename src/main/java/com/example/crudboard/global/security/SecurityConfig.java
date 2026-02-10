package com.example.crudboard.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Profile("dev")
    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${app.security.admin.username}") String username,
            @Value("${app.security.admin.password}") String password) {
        log.info("ADMIN_USERNAME={}, ADMIN_PASSWORD length={}", username, password == null ? null : password.length());
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(encoder.encode(password))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    @Profile("dev")
    @Order(1)
    SecurityFilterChain dbConsoleChain(HttpSecurity http) throws Exception {

        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("H2");
        entryPoint.afterPropertiesSet();

        http.securityMatcher("/h2-console/**", "/h2-console")
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .httpBasic(b -> b.authenticationEntryPoint(entryPoint))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint));
        return http.build();
    }

    // 이건 쫌 많이 이해가 필요할 듯
    @Bean
    @Order(2)
    SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // 공개 페이지
                        .requestMatchers("/", "/home").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/posts/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // 인증 api
                        .requestMatchers("/api/auth/**").permitAll()

                        // read-only public apis
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()

                        // 로그인 필요 apis
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        http.headers(h -> h.frameOptions(FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    // 2. 9.(월) 트러블 슈팅 내역
    /**
     * 2. 9.(월) 트러블 슈팅 내역
     * 현상 : http://localhost:8080/h2-console/ 접속시 ERROR 403 이슈
     * 원인 흐름
     * 	1.	/h2-console/ 요청이 dbConsoleChain을 탐
     * 	2.	익명(ROLE_ANONYMOUS)이라 hasRole("ADMIN")에서 거절됨
     * 	3.	ExceptionTranslationFilter가 AuthenticationEntryPoint로 보내려고 함 (여기까지는 맞음)
     * 	4.	그런데 응답이 에러로 처리되면서 톰캣이 ERROR 디스패치로 /error로 포워드
     * 	5.	/error는 appChain이 잡는데, appChain은 anyRequest().authenticated() + (httpBasic/formLogin 둘 다 disable)
     * 	6.	그래서 /error에서 Http403ForbiddenEntryPoint가 찍히면서 최종적으로 403 화면이 사용자에게 보임
     */
}
