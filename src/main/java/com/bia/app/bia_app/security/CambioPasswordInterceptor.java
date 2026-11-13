package com.bia.app.bia_app.security;

import com.bia.app.bia_app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Si el usuario tiene una contraseña temporal, solo le deja ir a la pantalla de cambio de contraseña.
 */
@Configuration
public class CambioPasswordInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    static final String RUTA_CAMBIO = "/cuenta/password";

    private final UsuarioRepository usuarioRepository;

    public CambioPasswordInterceptor(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && usuarioRepository.existsByUsernameAndDebeCambiarPasswordTrue(auth.getName())) {
            response.sendRedirect(request.getContextPath() + RUTA_CAMBIO);
            return false;
        }
        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .excludePathPatterns(RUTA_CAMBIO, "/login", "/logout", "/css/**", "/error");
    }
}
