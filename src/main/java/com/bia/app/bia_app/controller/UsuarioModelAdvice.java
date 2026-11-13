package com.bia.app.bia_app.controller;

import com.bia.app.bia_app.model.Usuario;
import com.bia.app.bia_app.security.AccesoService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone en todas las vistas el usuario conectado y sus permisos para
 * mostrar u ocultar botones. El control real se hace en el servidor (AccesoService).
 */
@ControllerAdvice
public class UsuarioModelAdvice {

    private final AccesoService accesoService;

    public UsuarioModelAdvice(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @ModelAttribute
    public void datosUsuario(Model model) {
        Usuario u = accesoService.usuarioActualOpcional().orElse(null);
        model.addAttribute("usuarioActual", u);
        model.addAttribute("esAdmin", u != null && u.isAdmin());
        model.addAttribute("puedeEditar", u != null && u.isPuedeEditar());
    }
}
