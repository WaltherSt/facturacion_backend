package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    // Proveedor de autenticación personalizado, que se encargará de validar las credenciales.
    private final AuthenticationProvider authenticationProvider;

    // Filtro de autenticación JWT personalizado, que se ejecuta antes del filtro estándar.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor que inyecta el proveedor de autenticación y el filtro JWT
    public SecurityConfiguration(AuthenticationProvider authenticationProvider,
                                 JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Método que configura la cadena de seguridad de Spring Security.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Configuración de la seguridad HTTP
        http.csrf(csrf -> {
                    try {
                        // Desactiva la protección CSRF, ya que no es necesaria para APIs RESTful que no usan sesiones.
                        csrf.disable()
                                // Configura las reglas de autorización.
                                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                                        // Permite todas las solicitudes a las rutas que comienzan con /auth (ej. login, registro).
                                        .requestMatchers("/auth/**")
                                        .permitAll()
                                        // Todas las demás solicitudes requieren autenticación.
                                        .anyRequest()
                                        .authenticated());
                    } catch (Exception e) {
                        // Si ocurre una excepción durante la configuración, se lanza una RuntimeException.
                        throw new RuntimeException(e);
                    }
                })
                // Configura la gestión de sesiones para que sea "stateless", es decir, sin usar sesiones del lado del servidor.
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Establece el proveedor de autenticación personalizado.
                .authenticationProvider(this.authenticationProvider)
                // Agrega el filtro JWT personalizado antes del filtro estándar de autenticación por nombre de usuario y contraseña.
                .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Construye y devuelve la cadena de seguridad configurada.
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Crea una nueva instancia de la configuración CORS.
        CorsConfiguration configuration = new CorsConfiguration();

        // Define los orígenes permitidos para solicitudes CORS.
        // En este caso, solo se permiten solicitudes desde "http://localhost:8080".
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));

        // Especifica los métodos HTTP permitidos en las solicitudes CORS.
        // Aquí solo se permiten las solicitudes GET y POST.
        configuration.setAllowedMethods(List.of("GET", "POST"));

        // Define los encabezados permitidos en las solicitudes CORS.
        // Solo se permiten "Authorization" y "Content-Type".
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Crea una fuente de configuración CORS basada en URL y registra la configuración
        // para que se aplique a todas las rutas (/**).
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        // Devuelve la fuente de configuración CORS que será utilizada por Spring Security.
        return source;
    }


}