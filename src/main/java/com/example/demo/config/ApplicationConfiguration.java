package com.example.demo.config;
import com.example.demo.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuración principal de la aplicación relacionada con la autenticación y la seguridad.
 */
@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    /**
     * Constructor que inyecta el repositorio de usuarios para gestionar la autenticación.
     *
     * @param userRepository Repositorio para gestionar los usuarios en la base de datos.
     */
    public ApplicationConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Define un `UserDetailsService` que busca a los usuarios por su email.
     *
     * @return Un servicio que carga los detalles del usuario.
     * @throws UsernameNotFoundException Si no se encuentra al usuario.
     */
    @Bean
    public UserDetailsService userDetailsService() {

        return username -> userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Usuario no encontrado"));


    }

    /**
     * Define un `BCryptPasswordEncoder` que será usado para encriptar las contraseñas.
     *
     * @return Un `BCryptPasswordEncoder` para la codificación de contraseñas.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define un `AuthenticationManager` que gestiona la autenticación de los usuarios.
     *
     * @param configuration Configuración de autenticación de Spring Security.
     * @return Un `AuthenticationManager` que gestiona la autenticación.
     * @throws Exception Si ocurre un error al obtener el `AuthenticationManager`.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Define un `AuthenticationProvider` que utiliza el `UserDetailsService` y un codificador de contraseñas.
     * El proveedor autentica al usuario verificando los detalles y validando la contraseña.
     *
     * @return Un `DaoAuthenticationProvider` configurado con el servicio de detalles de usuario y el codificador de contraseñas.
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}