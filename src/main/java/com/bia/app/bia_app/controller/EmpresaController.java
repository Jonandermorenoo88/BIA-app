package com.bia.app.bia_app.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.service.EmpresaService;
import com.bia.app.bia_app.security.AccesoService;

@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final AccesoService accesoService;

    public EmpresaController(EmpresaService empresaService, AccesoService accesoService) {
        this.empresaService = empresaService;
        this.accesoService = accesoService;
    }

    @GetMapping
    public String verEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.listarVisibles());
        return "empresas";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Empresa empresa, BindingResult result, RedirectAttributes flash) {
        accesoService.comprobarPuedeCrearEmpresas();
        empresa.setId(null);
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error de validación: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas";
        }
        empresaService.guardar(empresa);
        flash.addFlashAttribute("flashSuccess", "Empresa '" + empresa.getNombre() + "' creada correctamente");
        return "redirect:/empresas";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, @Valid Empresa empresa, BindingResult result, RedirectAttributes flash) {
        accesoService.comprobarEscritura(id);
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error de validación: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas";
        }
        empresaService.editar(id, empresa);
        flash.addFlashAttribute("flashSuccess", "Empresa actualizada correctamente");
        return "redirect:/empresas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes flash) {
        accesoService.comprobarAdmin();
        empresaService.eliminar(id);
        flash.addFlashAttribute("flashSuccess", "Empresa eliminada correctamente");
        return "redirect:/empresas";
    }
}