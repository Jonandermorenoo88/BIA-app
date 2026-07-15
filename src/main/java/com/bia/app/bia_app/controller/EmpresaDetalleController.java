package com.bia.app.bia_app.controller;

import com.bia.app.bia_app.model.ActivoTecnologico;
import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Persona;
import com.bia.app.bia_app.service.ActivosService;
import com.bia.app.bia_app.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        // Asumiendo que EmpresaService tiene un método obtenerPorId. Voy a crearlo si
        // no existe.
        Empresa empresa = empresaService.listar().stream()
                .filter(e -> e.getId().equals(empresaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        model.addAttribute("empresa", empresa);
        return "empresa_detalle";
    }

    @PostMapping("/{empresaId}/bias/guardar")
    public String crearBia(@PathVariable("empresaId") Long empresaId, com.bia.app.bia_app.model.BiaProyecto bia) {
        bia.setId(null);
        activosService.crearBiaProyecto(empresaId, bia);
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/personas/guardar")
    public String guardarPersona(@PathVariable("empresaId") Long empresaId, Persona persona) {
        persona.setId(null); // Force creation
        activosService.guardarPersona(empresaId, persona);
        return "redirect:/empresas/" + empresaId;
    }

    @PostMapping("/{empresaId}/activos/guardar")
    public String guardarActivo(@PathVariable("empresaId") Long empresaId, ActivoTecnologico activo) {
        activo.setId(null); // Force creation
        activosService.guardarActivoTecnologico(empresaId, activo);
        return "redirect:/empresas/" + empresaId;
    }
}
