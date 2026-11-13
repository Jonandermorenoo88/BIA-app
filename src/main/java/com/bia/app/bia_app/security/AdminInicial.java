package com.bia.app.bia_app.security;

import com.bia.app.bia_app.model.Rol;
import com.bia.app.bia_app.model.Usuario;
import com.bia.app.bia_app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer administrador cuando la tabla de usuarios está vacía.
 * Usuario y contraseña se leen de BIA_ADMIN_USERNAME / BIA_ADMIN_PASSWORD.
 * Si no se define la contraseña, se genera una aleatoria, se muestra UNA vez
 * en el log de arranque y se obliga a cambiarla en el primer acceso.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        @Value("${bia.admin.username:admin}") String username,
                        @Value("${bia.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        boolean generada = password == null || password.isBlank();
        String pwd = generada ? PasswordPolicy.generarTemporal() : password;

        Usuario admin = new Usuario();
        admin.setUsername(username);
        admin.setNombre("Administrador");
        admin.setRol(Rol.ADMIN);
        admin.setPasswordHash(passwordEncoder.encode(pwd));
        admin.setDebeCambiarPassword(generada);
        usuarioRepository.save(admin);

        if (generada) {
            log.warn("""

                    =====================================================================
                     Primer arranque: creado el usuario administrador '{}'
                     Contraseña temporal: {}
                     Se pedirá cambiarla en el primer inicio de sesión.
                    =====================================================================""",
                    username, pwd);
        } else {
            log.info("Primer arranque: creado el usuario administrador '{}' con la contraseña de BIA_ADMIN_PASSWORD", username);
        }
    }
}
