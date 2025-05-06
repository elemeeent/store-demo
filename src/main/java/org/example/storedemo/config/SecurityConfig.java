package org.example.storedemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${app.user.login}")
	private String login;
	@Value("${app.user.password}")
	private String password;
	@Value("${app.user.role}")
	private String role;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/login","/logout").permitAll()
						// public
						.requestMatchers(HttpMethod.GET, "/products/").permitAll()

						.requestMatchers(HttpMethod.GET, "/orders/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/orders/").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/orders/").permitAll()

						.requestMatchers(HttpMethod.POST, "/payments/**").permitAll()

						// admin
						.requestMatchers(HttpMethod.GET, "/admin/**").hasRole(role)
						.requestMatchers(HttpMethod.POST, "/admin/**").hasRole(role)
						.requestMatchers(HttpMethod.PATCH, "/admin/**").hasRole(role)
						.requestMatchers(HttpMethod.DELETE, "/admin/**").hasRole(role)
						.requestMatchers("/swagger-ui/**").hasRole(role)
						.requestMatchers("/v3/api-docs/**").hasRole(role)
						.requestMatchers("/h2-console/**").hasRole(role)

						.anyRequest().permitAll()
				)
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.httpBasic(Customizer.withDefaults())
				.formLogin(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}




	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withUsername(login)
				.password(passwordEncoder().encode(password))
				.roles(role)
				.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
