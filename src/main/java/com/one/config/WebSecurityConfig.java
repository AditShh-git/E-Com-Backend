package com.one.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
public class WebSecurityConfig {

	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Bean
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authConf) throws Exception {
		return authConf.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		authenticationProvider.setUserDetailsService(userDetailsService);
		return authenticationProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// return new BCryptPasswordEncoder();

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
        return http
                // ============================================================
                // 🔒 BASIC SECURITY CONFIGURATION
                // ============================================================
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ============================================================
                // 🔐 AUTHORIZATION RULES
                // ============================================================
                .authorizeHttpRequests(auth -> auth

                        // ------------------------------------------------------------
                        // 🟢 PUBLIC ENDPOINTS (accessible by everyone)
                        // ------------------------------------------------------------
                        .requestMatchers(
                                "/api/auth/**",          // login, register, refresh
                                "/api/public/**",        // public product info
                                "/api/files/**"          // static or shared files
                        ).permitAll()

                        // ------------------------------------------------------------
                        // 🟡 SELLER-ONLY ENDPOINTS
                        // ------------------------------------------------------------
                        .requestMatchers("/api/seller/**")
                        .hasAuthority("SELLER")

                        // ------------------------------------------------------------
                        // 🔴 ADMIN-ONLY ENDPOINTS
                        // ------------------------------------------------------------
                        .requestMatchers("/api/admin/**", "/api/sellers/**")
                        .hasAuthority("ADMIN")

                        // ------------------------------------------------------------
                        // 🧩 DEFAULT RULE — all other routes require authentication
                        // ------------------------------------------------------------
                        .anyRequest().authenticated()
                )

                // ============================================================
                // ⚙️ JWT FILTER (runs before username-password filter)
                // ============================================================
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)

                // ============================================================
                // 🚀 BUILD FILTER CHAIN
                // ============================================================
                .build();
    }



    @Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		// log.info("ingnoring the security");
		return web -> web.ignoring()
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**")
				.requestMatchers("/error/**");
	}

//    @Bean
//    public JavaMailSender javaMailSender() {
//        return new JavaMailSenderImpl();
//    }

}
