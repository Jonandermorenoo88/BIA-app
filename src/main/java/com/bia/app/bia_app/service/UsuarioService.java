package com.bia.app.bia_app.service;

import com.bia.app.bia_app.model.Rol;
import com.bia.app.bia_app.model.Usuario;
import com.bia.app.bia_app.repository.EmpresaRepository;
import com.bia.app.bia_app.repository.UsuarioRepository;
import com.bia.app.bia_app.security.PasswordPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class UsuarioService {

    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, EmpresaRepository empresaRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    /**
     * Crea un usuario con contraseña temporal aleatoria.
     * @return la contraseña temporal, que se muestra una sola vez al administrador.
     */
    public String crear(String username, String nombre, Rol rol, List<Long> empresaIds) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new IllegalStateException("Usuario no válido: 3-50 caracteres (letras, números, punto, guion o guion bajo)");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalStateException("Ya existe un usuario con ese nombre");
        }
        if (nombre == null || nombre.isBlank() || nombre.length() > 100) {
            throw new IllegalStateException("El nombre es obligatorio (máx. 100 caracteres)");
        }
        if (rol == null) {
            throw new IllegalStateException("El rol es obligatorio");
        }
        String temporal = PasswordPolicy.generarTemporal();
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombre(nombre.trim());
        u.setRol(rol);
        u.setPasswordHash(passwordEncoder.encode(temporal));
        u.setDebeCambiarPassword(true);
        asignarEmpresas(u, empresaIds);
        usuarioRepository.save(u);
        return temporal;
    }

    public void actualizar(Long id, Long idAdminActual, Rol rol, boolean activo, List<Long> empresaIds) {
        Usuario u = obtener(id);
        if (rol == null) {
            throw new IllegalStateException("El rol es obligatorio");
        }
        boolean dejaDeSerAdminActivo = u.getRol() == Rol.ADMIN && u.isActivo() && (rol != Rol.ADMIN || !activo);
        if (dejaDeSerAdminActivo && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new IllegalStateException("Debe quedar al menos un administrador activo");
        }
        if (id.equals(idAdminActual) && (!activo || rol != Rol.ADMIN)) {
            throw new IllegalStateException("No puedes quitarte a ti mismo el rol de administrador ni desactivarte");
        }
        u.setRol(rol);
        u.setActivo(activo);
        asignarEmpresas(u, empresaIds);
        usuarioRepository.save(u);
    }

    /** @return nueva contraseña temporal. También desbloquea la cuenta. */
    public String resetearPassword(Long id) {
        Usuario u = obtener(id);
        String temporal = PasswordPolicy.generarTemporal();
        u.setPasswordHash(passwordEncoder.encode(temporal));
        u.setDebeCambiarPassword(true);
        u.setIntentosFallidos(0);
        u.setBloqueadoHasta(null);
        usuarioRepository.save(u);
        return temporal;
    }

    public void eliminar(Long id, Long idAdminActual) {
        Usuario u = obtener(id);
        if (id.equals(idAdminActual)) {
            throw new IllegalStateException("No puedes eliminar tu propio usuario");
        }
        if (u.getRol() == Rol.ADMIN && u.isActivo() && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new IllegalStateException("Debe quedar al menos un administrador activo");
        }
        usuarioRepository.delete(u);
    }

    public void cambiarPassword(String username, String actual, String nueva, String confirmacion) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        if (actual == null || !passwordEncoder.matches(actual, u.getPasswordHash())) {
            throw new IllegalStateException("La contraseña actual no es correcta");
        }
        if (nueva == null || !nueva.equals(confirmacion)) {
            throw new IllegalStateException("La nueva contraseña y su confirmación no coinciden");
        }
        String error = PasswordPolicy.validar(nueva);
        if (error != null) {
            throw new IllegalStateException(error);
        }
        if (passwordEncoder.matches(nueva, u.getPasswordHash())) {
            throw new IllegalStateException("La nueva contraseña debe ser distinta de la actual");
        }
        u.setPasswordHash(passwordEncoder.encode(nueva));
        u.setDebeCambiarPassword(false);
        usuarioRepository.save(u);
    }

    private Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void asignarEmpresas(Usuario u, List<Long> empresaIds) {
        if (u.getRol() == Rol.ADMIN || empresaIds == null) {
            u.setEmpresas(new HashSet<>());
            return;
        }
        u.setEmpresas(new HashSet<>(empresaRepository.findAllById(empresaIds)));
    }
}
