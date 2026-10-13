package odk.malicompetences.backend.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
<<<<<<< HEAD
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
=======
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
<<<<<<< HEAD
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
=======
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

<<<<<<< HEAD
    // Ajout de @Lazy pour retarder l'initialisation du service de base de données au démarrage de Tomcat
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
=======
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
<<<<<<< HEAD
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Ignorer le filtrage JWT pour les routes publiques d'authentification
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/") || path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

=======
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
<<<<<<< HEAD

        try {
            if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Object userIdObj = jwtService.extractUtilisateurId(token);
                if (userIdObj != null) {
                    String userIdStr = userIdObj.toString();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userIdStr);
                    if (userDetails != null && userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
=======
        if (!jwtService.isValid(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    jwtService.extractUtilisateurId(token).toString());
            if (userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
            }
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
