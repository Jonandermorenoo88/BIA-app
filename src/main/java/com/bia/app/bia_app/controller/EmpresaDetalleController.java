package com.bia.app.bia_app.controller;

import com.bia.app.bia_app.model.ActivoTecnologico;
import com.bia.app.bia_app.model.BiaProyecto;
import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Persona;
import com.bia.app.bia_app.service.ActivosService;
import com.bia.app.bia_app.service.EmpresaService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empresas")
public class EmpresaDetalleController {

    private final EmpresaService empresaService;
    private final ActivosService activosService;

    public EmpresaDetalleController(EmpresaService empresaService, ActivosService activosService) {
        this.empresaService = empresaService;
        this.activosService = activosService;
    }

    @GetMapping("/{empresaId}")
    public String verDetalleEmpresa(@PathVariable("empresaId") Long empresaId, Model model) {
        Empresa empresa = empresaService.obtenerPorId(empresaId);
        model.addAttribute("empresa", empresa);
        return "empresa_detalle";
    }

    @PostMapping("/{empresaId}/bias/guardar")
    public String crearBia(@PathVariable("empresaId") Long empresaId, @Valid BiaProyecto bia,
                           BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas/" + empresaId;
        }
        bia.setId(null);
        activosService.crearBiaProyecto(empresaId, bia);
        flash.addFlashAttribute("flashSuccess", "Proyecto BIA '" + bia.getNombre() + "' creado correctamente");
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/bias/{biaId}/eliminar")
    public String eliminarBia(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                              RedirectAttributes flash) {
        activosService.eliminarBiaProyecto(biaId);
        flash.addFlashAttribute("flashSuccess", "Proyecto BIA eliminado correctamente");
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/personas/guardar")
    public String guardarPersona(@PathVariable("empresaId") Long empresaId, @Valid Persona persona,
                                 BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas/" + empresaId;
        }
        persona.setId(null);
        activosService.guardarPersona(empresaId, persona);
        flash.addFlashAttribute("flashSuccess", "Persona '" + persona.getNombre() + "' añadida correctamente");
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/personas/{personaId}/eliminar")
    public String eliminarPersona(@PathVariable("empresaId") Long empresaId, @PathVariable("personaId") Long personaId,
                                  RedirectAttributes flash) {
        activosService.eliminarPersona(personaId);
        flash.addFlashAttribute("flashSuccess", "Persona eliminada correctamente");
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/activos/guardar")
    public String guardarActivo(@PathVariable("empresaId") Long empresaId, @Valid ActivoTecnologico activo,
                                BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas/" + empresaId;
        }
        activo.setId(null);
        activosService.guardarActivoTecnologico(empresaId, activo);
        flash.addFlashAttribute("flashSuccess", "Activo '" + activo.getNombre() + "' añadido correctamente");
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/activos/{activoId}/eliminar")
    public String eliminarActivo(@PathVariable("empresaId") Long empresaId, @PathVariable("activoId") Long activoId,
                                 RedirectAttributes flash) {
        activosService.eliminarActivo(activoId);
        flash.addFlashAttribute("flashSuccess", "Activo eliminado correctamente");
        return "redirect:/empresas/" + empresaId;
    }
}
