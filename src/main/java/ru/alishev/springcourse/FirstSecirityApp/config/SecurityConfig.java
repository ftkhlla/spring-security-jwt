package ru.alishev.springcourse.FirstSecirityApp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import ru.alishev.springcourse.FirstSecirityApp.services.PersonDetailService;

import static org.apache.coyote.http11.Constants.a;


@EnableWebSecurity
@Configuration
@EnableGlobalAuthentication
public class SecurityConfig   {

    private final PersonDetailService personDetailService;
    private final JWTFilter jwtFilter;

    @Autowired
    public SecurityConfig(PersonDetailService personDetailService, JWTFilter jwtFilter) {
        this.personDetailService = personDetailService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return  new BCryptPasswordEncoder();
    }

    // настраиваем аутентификацию
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(personDetailService).passwordEncoder(passwordEncoder());  ///*authenticationProvider(authProvider)*/
        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf(AbstractHttpConfigurer::disable);

        http.authenticationManager(authenticationManager)
                .authorizeHttpRequests((authz) -> authz
//                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/", "/auth/login", "auth/registration").permitAll()
                        .anyRequest().hasAnyRole("USER","ADMIN")
                );

        http.formLogin((formLogin) ->
                formLogin
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/process_login")
                        .defaultSuccessUrl("/hello", true)
                        .failureUrl("/auth/login?error")
        );

        http.logout((logout)->
                logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login")

                );

        //Сессии не хранятся на сервере
        http.sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }




}
