package med.voll.api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import med.voll.api.infra.exception.AccessDeniedHandlerPersonalizado;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)//ATIVA A SEGURANÇA A NÍVEL DE MÉTODO. PERMITE USAR ANOTAÇÕES COMO @PreAuthorize, @PostAuthorize, @Secured e OUTRAS NAS CLASSES DE SERVICE OU CONTROLLERS.
public class SecurityConfigurations {
	
	@Autowired
	private AccessDeniedHandlerPersonalizado accessDeniedHandlerPersonalizado;
	 
	@Autowired
	    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
        		.csrf(csrf -> csrf.disable())
        		.cors(cors -> cors.configurationSource(new CorsConfig().corsConfigurationSource())) // chama CorsConfig
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                	
                	//É SÓ NECESSÁRIO ESTAR AUTENTICADO (AUTHENTICATED()), SEM EXIGIR ROLE ESPECÍFICA.
                	req.requestMatchers(HttpMethod.PATCH, "/usuarios/reset-senha").authenticated();
             	
                	// LIBERA LOGIN E SWAGGER
                    req.requestMatchers("/login").permitAll();             
                    req.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();        

                    // LIBERA CADASTRO PARA MÉDICOS E PACIENTES
                    req.requestMatchers(HttpMethod.POST, "/medicos").permitAll();
                    req.requestMatchers(HttpMethod.PUT, "/medicos").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/pacientes").permitAll();
                    req.requestMatchers(HttpMethod.PUT, "/pacientes").permitAll();

                    // RESTRINGE LEITURA DE DADOS AOS SEUS RESPECTIVOS PAPÉIS
                    req.requestMatchers(HttpMethod.GET, "/medicos/**").hasAnyRole("MEDICO", "ADMIN","PACIENTE");
                    req.requestMatchers(HttpMethod.DELETE, "/medicos/**").hasAnyRole("MEDICO", "ADMIN");
                    req.requestMatchers(HttpMethod.GET, "/pacientes/**").hasAnyRole("PACIENTE", "ADMIN");
                    req.requestMatchers(HttpMethod.DELETE, "/pacientes/**").hasAnyRole("PACIENTE", "ADMIN");

                    // O ADMIN PODE ACESSAR TUDO
                    req.requestMatchers("/admin/**").hasRole("ADMIN");

                    // QUALQUER OUTRA REQUISIÇÃO PRECISA DE AUTENTICAÇÃO
                    req.anyRequest().authenticated();                

                })
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandlerPersonalizado))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
