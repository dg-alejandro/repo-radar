package com.reporadar.security;

import com.reporadar.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component//marca esta clase como un componente gestionado por spring, lo cual permita que el propio spring lo inyecte
//en la configuracion de seguridad
public class JwtAuthFilter extends OncePerRequestFilter {//oncePerRequestFilter garantiza que el filtro se ejecute
                                                         //una unica vez

    private final JwtService jwtService;
    private final AppUserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, AppUserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");//busca en la peticion http un encabezado llamado
                                                                  //"authorization", que es donde se envia el token
        //si no hay encabezado o no empieza con la palabra "bearer", el filtro asume que no hay intencion de autenticarse con jwt
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);//deja pasar la peticion al siguiente filtro de la catena
            return;
        }

        String token = authHeader.substring(7);//quitamos "bearer" y el espacio para quedarnos con la cadena de texto del token

        if (jwtService.isTokenValid(token)) {//comprueba que es un token real y vigente
            String email = jwtService.extractEmail(token);//se obtiene el email del usuario
            userRepository.findByEmail(email).ifPresent(user -> {//busca dicho usuario en la bd
                //crea un objeto de identidad reconocido por spring
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);//se inyecta al usuario en el contexto de seguridad
            });
        }

        filterChain.doFilter(request, response);
    }
}
