package com.bia.app.bia_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;

import com.bia.app.bia_app.model.BiaProyecto;
import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.ProcesoCritico;
import com.bia.app.bia_app.repository.BiaProyectoRepository;
import com.bia.app.bia_app.repository.EmpresaRepository;
import com.bia.app.bia_app.service.ActivosService;
import com.bia.app.bia_app.service.ExcelExportService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/empresas/{empresaId}/bias")
public class BiaDashboardController {

    private final BiaProyectoRepository biaRepository;
    private final EmpresaRepository empresaRepository;
    private final ActivosService activosService;
    private final ExcelExportService excelExportService;

    public BiaDashboardController(BiaProyectoRepository biaRepository, EmpresaRepository empresaRepository, ActivosService activosService, ExcelExportService excelExportService) {
        this.biaRepository = biaRepository;
        this.empresaRepository = empresaRepository;
        this.activosService = activosService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/{biaId}/exportar-excel")
    public ResponseEntity<InputStreamResource> exportarExcel(@PathVariable("biaId") Long biaId) throws IOException {
        BiaProyecto bia = biaRepository.findById(biaId)
                .orElseThrow(() -> new IllegalArgumentException("BIA no encontrado"));
                
        ByteArrayInputStream in = excelExportService.exportBiaToExcel(bia);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Matriz_BIA_" + bia.getNombre().replace(" ", "_").replaceAll("[^a-zA-Z0-9_]", "") + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/{biaId}")
    public String verDashboardBia(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId, Model model) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        BiaProyecto bia = biaRepository.findById(biaId)
                .orElseThrow(() -> new IllegalArgumentException("BIA no encontrado"));
                
        model.addAttribute("empresa", empresa);
        model.addAttribute("bia", bia);
        return "bia_dashboard";
    }

    @GetMapping("/{biaId}/informe-ejecutivo")
    public String generarInformeEjecutivo(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId, Model model) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        BiaProyecto bia = biaRepository.findById(biaId)
                .orElseThrow(() -> new IllegalArgumentException("BIA no encontrado"));
                
        int totalProcesos = bia.getProcesos().size();
        long procesosCriticos = bia.getProcesos().stream()
                .filter(p -> (p.getImpacto() != null ? p.getImpacto() : 1) * (p.getProbabilidad() != null ? p.getProbabilidad() : 1) >= 15)
                .count();
        long procesosAltos = bia.getProcesos().stream()
                .filter(p -> {
                    int r = (p.getImpacto() != null ? p.getImpacto() : 1) * (p.getProbabilidad() != null ? p.getProbabilidad() : 1);
                    return r >= 6 && r < 15;
                })
                .count();
                
        // Empleados SPOF (Asignados a multiples procesos en este BIA)
        java.util.List<com.bia.app.bia_app.model.Persona> spofPersonas = new java.util.ArrayList<>();
        java.util.Map<com.bia.app.bia_app.model.Persona, Integer> conteoPersonas = new java.util.HashMap<>();
        for (ProcesoCritico p : bia.getProcesos()) {
            for (com.bia.app.bia_app.model.Persona per : p.getPersonas()) {
                conteoPersonas.put(per, conteoPersonas.getOrDefault(per, 0) + 1);
            }
        }
        for (java.util.Map.Entry<com.bia.app.bia_app.model.Persona, Integer> entry : conteoPersonas.entrySet()) {
            if (entry.getValue() > 1) spofPersonas.add(entry.getKey());
        }
        
        // Activos SPOF
        java.util.List<com.bia.app.bia_app.model.ActivoTecnologico> spofActivos = new java.util.ArrayList<>();
        java.util.Map<com.bia.app.bia_app.model.ActivoTecnologico, Integer> conteoActivos = new java.util.HashMap<>();
        for (ProcesoCritico p : bia.getProcesos()) {
            for (com.bia.app.bia_app.model.ActivoTecnologico act : p.getActivos()) {
                conteoActivos.put(act, conteoActivos.getOrDefault(act, 0) + 1);
            }
        }
        for (java.util.Map.Entry<com.bia.app.bia_app.model.ActivoTecnologico, Integer> entry : conteoActivos.entrySet()) {
            if (entry.getValue() > 1) spofActivos.add(entry.getKey());
        }

        model.addAttribute("empresa", empresa);
        model.addAttribute("bia", bia);
        model.addAttribute("totalProcesos", totalProcesos);
        model.addAttribute("procesosCriticos", procesosCriticos);
        model.addAttribute("procesosAltos", procesosAltos);
        model.addAttribute("spofPersonas", spofPersonas);
        model.addAttribute("spofActivos", spofActivos);
        
        return "informe_ejecutivo";
    }

    @PostMapping("/{biaId}/procesos/guardar")
    public String guardarProceso(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId, ProcesoCritico proceso) {
        proceso.setId(null);
        activosService.guardarProcesoCritico(biaId, proceso);
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/vincular-activo")
    public String vincularActivo(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId, @PathVariable Long idProceso, @RequestParam Long idActivo) {
        activosService.vincularActivoAProceso(idProceso, idActivo);
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/vincular-persona")
    public String vincularPersona(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId, @PathVariable Long idProceso, @RequestParam Long idPersona) {
        activosService.vincularPersonaAProceso(idProceso, idPersona);
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }
}
