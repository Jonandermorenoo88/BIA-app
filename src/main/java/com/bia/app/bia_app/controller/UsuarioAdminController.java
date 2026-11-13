package com.bia.app.bia_app.controller;

import com.bia.app.bia_app.model.Rol;
import com.bia.app.bia_app.security.AccesoService;
import com.bia.app.bia_app.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Gestión de usuarios. Protegido por SecurityConfig (/admin/** solo ROLE_ADMIN) y por AccesoService. */
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private static final String REDIRECT = "redirect:/admin/usuarios";

    private final UsuarioService usuarioService;
    private final AccesoService accesoService;

    public UsuarioAdminController(UsuarioService usuarioService, AccesoService accesoService) {
        this.usuarioService = usuarioService;
        this.accesoService = accesoService;
    }

    @GetMapping
    public String listar(Model model) {
        accesoService.comprobarAdmin();
        model.addAttribute("usuarios", usuarioService.listar());
        model.addAttribute("empresas", accesoService.empresasVisibles());
        model.addAttribute("roles", Rol.values());
        return "admin/usuarios";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String username, @RequestParam String nombre, @RequestParam Rol rol,
                        @RequestParam(required = false) List<Long> empresaIds, RedirectAttributes flash) {
        accesoService.comprobarAdmin();
        try {
            String temporal = usuarioService.crear(username.trim(), nombre, rol, empresaIds);
            flash.addFlashAttribute("flashSuccess", "Usuario '" + username.trim() + "' creado.");
            flash.addFlashAttribute("passwordTemporal", temporal);
            flash.addFlashAttribute("passwordTemporalUsuario", username.trim());
        } catch (IllegalStateException e) {
            flash.addFlashAttribute("flashError", e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Long id, @RequestParam Rol rol,
                             @RequestParam(defaultValue = "false") boolean activo,
                             @RequestParam(required = false) List<Long> empresaIds, RedirectAttributes flash) {
        accesoService.comprobarAdmin();
        try {
            usuarioService.actualizar(id, accesoService.usuarioActual().getId(), rol, activo, empresaIds);
            flash.addFlashAttribute("flashSuccess", "Usuario actualizado");
        } catch (IllegalStateException e) {
            flash.addFlashAttribute("flashError", e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes flash) {
        accesoService.comprobarAdmin();
        String temporal = usuarioService.resetearPassword(id);
        flash.addFlashAttribute("flashSuccess", "Contraseña reseteada y cuenta desbloqueada.");
        flash.addFlashAttribute("passwordTemporal", temporal);
        return REDIRECT;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        accesoService.comprobarAdmin();
        try {
            usuarioService.eliminar(id, accesoService.usuarioActual().getId());
            flash.addFlashAttribute("flashSuccess", "Usuario eliminado");
        } catch (IllegalStateException e) {
            flash.addFlashAttribute("flashError", e.getMessage());
        }
        return REDIRECT;
    }
}
