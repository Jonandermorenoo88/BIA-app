package com.bia.app.bia_app.security;

import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Rol;
import com.bia.app.bia_app.model.Usuario;
import com.bia.app.bia_app.repository.EmpresaRepository;
import com.bia.app.bia_app.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Punto único de control de acceso por empresa (aislamiento entre clientes).
 * Toda operación sobre datos de una empresa debe pasar por aquí antes de ejecutarse.
 */
@Service
@Transactional(readOnly = true)
public class AccesoService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    public AccesoService(UsuarioRepository usuarioRepository, EmpresaRepository empresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
    }

    public Optional<Usuario> usuarioActualOpcional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsername(auth.getName()).filter(Usuario::isActivo);
    }

    public Usuario usuarioActual() {
        return usuarioActualOpcional()
                .orElseThrow(() -> new AccessDeniedException("Sesión no válida"));
    }

    /** Lanza AccessDeniedException si el usuario no puede ver la empresa. */
    public void comprobarLectura(Long empresaId) {
        Usuario u = usuarioActual();
        if (u.getRol() == Rol.ADMIN) {
            return;
        }
        if (empresaId == null || usuarioRepository.contarAcceso(u.getId(), empresaId) == 0) {
            throw new AccessDeniedException("No tienes acceso a esta empresa");
        }
    }

    /** Lanza AccessDeniedException si el usuario no puede modificar la empresa. */
    public void comprobarEscritura(Long empresaId) {
        comprobarLectura(empresaId);
        if (!usuarioActual().isPuedeEditar()) {
            throw new AccessDeniedException("Tu rol es de solo lectura");
        }
    }

    public void comprobarPuedeCrearEmpresas() {
        if (!usuarioActual().isPuedeEditar()) {
            throw new AccessDeniedException("Tu rol es de solo lectura");
        }
    }

    public void comprobarAdmin() {
        if (usuarioActual().getRol() != Rol.ADMIN) {
            throw new AccessDeniedException("Solo un administrador puede realizar esta acción");
        }
    }

    /** Empresas que el usuario actual puede ver. */
    public List<Empresa> empresasVisibles() {
        Usuario u = usuarioActual();
        if (u.getRol() == Rol.ADMIN) {
            return empresaRepository.findAll(Sort.by("nombre"));
        }
        return usuarioRepository.findEmpresasDeUsuario(u.getId());
    }
}
