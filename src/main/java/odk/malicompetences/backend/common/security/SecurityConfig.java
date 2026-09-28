//package odk.malicompetences.backend.common.security;
//
//import org.springframework.context.annotation.*;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.*;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.*;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    public SecurityConfig(
//            JwtAuthenticationFilter jwtAuthenticationFilter
//    ) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity http
//    ) throws Exception {
//
//        return http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(
//                                SessionCreationPolicy.STATELESS
//                        )
//                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/api/v1/auth/**",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(
//                        jwtAuthenticationFilter,
//                        UsernamePasswordAuthenticationFilter.class
//                )
//                .build();
//    }
//}