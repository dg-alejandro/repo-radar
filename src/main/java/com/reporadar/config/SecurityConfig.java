package com.reporadar.config;

import com.reporadar.security.AdminDetailsService;
import com.reporadar.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //indicamos a spring que esta clase sirve para configuracion
@EnableWebSecurity //activa la seguridad web de spring security
public class SecurityConfig {

    private final AdminDetailsService adminDetailsService;
    private final JwtAuthFilter jwtAuthFilter; //filtro JWT para autenticar peticiones de la API con token

    public SecurityConfig(AdminDetailsService adminDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.adminDetailsService = adminDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean //indica que el metodo de debajo devuelve un bean (objeto gestionado por spring)

    //metodo que construye y devuelve la configuracion principal de seguridad. SecurityFilterChain es la cadena de filtros
    //que spring va a aplicar a las peticiones http
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //esta parte configura las reglas de autorizacion de las peticiones http
        //indicamos que cualquier ruta que comience con /admin/ necesita autenticacion, mientras que el resto de rutas seran publicas
        //las rutas de favoritos requieren JWT valido; el registro y login son publicos
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/favorites/**").authenticated()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )

                //aqui se configura el login con formulario html, loginPage indica la url de la pagina de login,
                //loginProcessingUrl define la url a la que se envia el formulario cuando el admin pulsa iniciar sesion.
                //successUrl y failureUrl indican la ruta dependiendo de si el login va bien o mal.
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/admin/projects", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )

                //configuracion de cierre de sesion, se definen la url que ejecuta el cierre y la url de redireccion al desloguearse
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .permitAll()
                )

                //la sesion HTTP se mantiene para el admin (IF_REQUIRED), el JWT no necesita sesion porque es stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                //se registra el filtro JWT antes del filtro de autenticacion por formulario
                //asi cada peticion a /api/** con Bearer token queda autenticada antes de llegar al controlador
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .userDetailsService(adminDetailsService);

        return http.build(); //se construye la configuracion final de la seguridad y se devuelve como SecurityFilterChain
    }

    @Bean
    //se define el codificador de contraseñas que vamos a usar, PasswordEncoder es la interfaz que usa security para hashear y verificar contraseñas
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //las contraseñas se almacenan y comparan usando BCrypt, algoritmo seguro para contraseñas
    }
}