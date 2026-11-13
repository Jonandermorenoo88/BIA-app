package com.bia.app.bia_app.security;

import com.bia.app.bia_app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Protección contra fuerza bruta: tras {@value #MAX_INTENTOS} contraseñas erróneas seguidas
 * la cuenta queda bloqueada {@value #MINUTOS_BLOQUEO} minutos.
 */
@Component
public class LoginIntentosListener {

    static final int MAX_INTENTOS = 5;
    static final int MINUTOS_BLOQUEO = 15;

    private static final Logger log = LoggerFactory.getLogger(LoginIntentosListener.class);

    private final UsuarioRepository usuarioRepository;

    public LoginIntentosListener(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener
    @Transactional
    public void onFallo(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        usuarioRepository.findByUsername(username).ifPresent(u -> {
            int intentos = u.getIntentosFallidos() + 1;
            if (intentos >= MAX_INTENTOS) {
                u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
                u.setIntentosFallidos(0);
                log.warn("Cuenta '{}' bloqueada {} min por {} intentos fallidos", username, MINUTOS_BLOQUEO, MAX_INTENTOS);
            } else {
                u.setIntentosFallidos(intentos);
            }
            usuarioRepository.save(u);
        });
        log.info("Login fallido para usuario '{}'", username);
    }

    @EventListener
    @Transactional
    public void onExito(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        usuarioRepository.findByUsername(username).ifPresent(u -> {
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }
}
