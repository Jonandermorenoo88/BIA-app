package com.bia.app.bia_app.controller;

import com.bia.app.bia_app.security.AccesoService;
import com.bia.app.bia_app.security.PasswordPolicy;
import com.bia.app.bia_app.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaController {

    private final AccesoService accesoService;
    private final UsuarioService usuarioService;

    public CuentaController(AccesoService accesoService, UsuarioService usuarioService) {
        this.accesoService = accesoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/empresas";
    }

    @GetMapping("/cuenta/password")
    public String formularioPassword(Model model) {
        model.addAttribute("longitudMinima", PasswordPolicy.LONGITUD_MINIMA);
        return "cambiar_password";
    }

    @PostMapping("/cuenta/password")
    public String cambiarPassword(@RequestParam String actual, @RequestParam String nueva,
                                  @RequestParam String confirmacion, RedirectAttributes flash) {
        String username = accesoService.usuarioActual().getUsername();
        try {
            usuarioService.cambiarPassword(username, actual, nueva, confirmacion);
        } catch (IllegalStateException e) {
            flash.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/cuenta/password";
        }
        flash.addFlashAttribute("flashSuccess", "Contraseña actualizada correctamente");
        return "redirect:/empresas";
    }
}
