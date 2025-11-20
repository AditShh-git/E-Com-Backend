package com.one.config;

import com.one.aim.repo.AdminRepo;
import com.one.aim.repo.SellerRepo;
import com.one.aim.repo.UserRepo;
import com.one.aim.repo.VendorRepo;
import com.one.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.one.security.jwt.AuthEntryPointJwt;
import com.one.security.jwt.AuthTokenFilter;
import com.one.service.impl.UserDetailsServiceImpl;
import com.one.utils.EncryptionUtils;
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final UserRepo userRepo;
    private final AdminRepo adminRepo;
    private final SellerRepo sellerRepo;
    private final VendorRepo vendorRepo;

    // ---------------------------------------------------------------
    //  1. JWT Filter Bean (Pass ALL 6 dependencies)
    // ---------------------------------------------------------------
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(
                jwtUtils,
                userDetailsService,
                userRepo,
                adminRepo,
                sellerRepo,
                vendorRepo
        );
    }

    // ---------------------------------------------------------------
    //  2. Authentication Provider
    // ---------------------------------------------------------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        //  IMPORTANT — enables isEnabled(), isAccountNonLocked(), etc.
        authProvider.setPreAuthenticationChecks(new AccountStatusUserDetailsChecker());

        return authProvider;
    }


    // ---------------------------------------------------------------
    //  3. Authentication Manager
    // ---------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ---------------------------------------------------------------
    //  4. Password Encoder (MD5-based)
    // ---------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                try {
                    return EncryptionUtils.makeMD5String(charSequence.toString());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                try {
                    return EncryptionUtils.checkMD5Password(charSequence.toString(), s);
                } catch (Exception e) {
                    return false;
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth

                        // ======================================
                        // PUBLIC AUTH (ALL ROLES)
                        // ======================================
                        .requestMatchers(
                                "/auth/**",
                                "/api/files/public/**"
                        ).permitAll()

                        // ======================================
                        // PUBLIC SIGNUP ROUTES
                        // ======================================
                        .requestMatchers(
                                "/api/user/signup",
                                "/api/seller/signup",
                                "/api/admin/create"
                        ).permitAll()

                        // ======================================
                        // PUBLIC RAZORPAY TEST ROUTES
                        // ======================================
                        .requestMatchers(
                                "/razorpay-test.html",
                                "/api/payment/create",
                                "/api/payment/verify"
                        ).permitAll()

                        // ======================================
                        // PUBLIC BROWSING ROUTES
                        // ======================================
                        .requestMatchers(
                                "/api/public/**",
                                "/api/search",
                                "/api/cart/category/**"
                        ).permitAll()

                        // ======================================
                        // USER ROUTES
                        // ======================================
                        .requestMatchers(
                                "/api/cart/**",
                                "/api/order/**",
                                "/api/user/me",
                                "/api/user/profile/update",
                                "/api/user/download/**"
                        ).hasAuthority("USER")

                        // ======================================
                        // SELLER ROUTES
                        // ======================================
                        .requestMatchers(
                                "/api/seller/me",
                                "/api/seller/carts",
                                "/api/seller/product/**",
                                "/api/seller/download/**",
                                "/api/seller/all/invoices",
                                "/api/seller/analytics/**"
                        ).hasAuthority("SELLER")

                        // ======================================
                        // ADMIN ROUTES
                        // ======================================
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasAuthority("ADMIN")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }








    // ---------------------------------------------------------------
    //  6. Swagger / Error Ignored from Security
    // ---------------------------------------------------------------
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**",
                        "/error/**"
                );
    }
}
