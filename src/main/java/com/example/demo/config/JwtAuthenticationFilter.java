package com.example.demo.config;

import com.example.demo.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Filtro personalizado de autenticación JWT que intercepta cada solicitud HTTP
 * y verifica la validez del token JWT en el encabezado de la solicitud.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor que inyecta los servicios necesarios para la validación de JWT,
     * la resolución de excepciones y la carga de detalles del usuario.
     *
     * @param handlerExceptionResolver Resuelve excepciones dentro del filtro.
     * @param jwtService               Servicio para gestionar JWT (creación, validación).
     * @param userDetailsService       Servicio para cargar los detalles del usuario.
     */
    public JwtAuthenticationFilter(
            HandlerExceptionResolver handlerExceptionResolver,
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;

    }

    /**
     * Método principal que intercepta cada solicitud HTTP y verifica el encabezado de autenticación.
     *
     * @param request     Solicitud HTTP entrante.
     * @param response    Respuesta HTTP saliente.
     * @param filterChain Cadena de filtros que continúa la solicitud.
     * @throws ServletException Si ocurre un error relacionado con el servlet.
     * @throws IOException      Si ocurre un error de I/O.
     */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {

        // Obtiene el valor del encabezado "Authorization" de la solicitud HTTP
        final String authHeader = request.getHeader("Authorization");

        // Verifica si el encabezado está presente y si comienza con "Bearer"
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            // Si no está presente o no tiene el formato adecuado, continúa con la cadena de filtros
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrae el token JWT quitando el prefijo "Bearer "
            final String jwt = authHeader.substring(7);
            // Extrae el nombre de usuario (email) del token JWT
            final String userEmail = jwtService.extractUsername(jwt);

            // Verifica si ya existe una autenticación en el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Si el usuario no está autenticado y el email fue extraído correctamente
            if (userEmail != null && authentication == null) {
                // Carga los detalles del usuario utilizando el email extraído
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Verifica si el token JWT es válido
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Crea un token de autenticación y lo establece en el contexto de seguridad
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Configura detalles adicionales de la solicitud en el token de autenticación
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Establece la autenticación en el contexto de seguridad de Spring
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            // Continúa con la cadena de filtros después de realizar la autenticación
            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            // Si ocurre una excepción, la pasa al resolutor de excepciones para gestionarla
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}