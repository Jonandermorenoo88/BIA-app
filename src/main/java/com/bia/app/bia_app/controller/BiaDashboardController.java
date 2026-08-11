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
import com.bia.app.bia_app.service.BiaAnalysisService;
import com.bia.app.bia_app.service.BiaAnalysisService.BiaResumen;
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
    private final BiaAnalysisService biaAnalysisService;

    public BiaDashboardController(BiaProyectoRepository biaRepository, EmpresaRepository empresaRepository,
                                  ActivosService activosService, ExcelExportService excelExportService,
                                  BiaAnalysisService biaAnalysisService) {
        this.biaRepository = biaRepository;
        this.empresaRepository = empresaRepository;
        this.activosService = activosService;
        this.excelExportService = excelExportService;
        this.biaAnalysisService = biaAnalysisService;
    }

    @GetMapping("/{biaId}/exportar-excel")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("biaId") Long biaId) throws IOException {

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

        // Lógica de análisis delegada al servicio
        BiaResumen resumen = biaAnalysisService.calcularResumen(bia);

        model.addAttribute("empresa", empresa);
        model.addAttribute("bia", bia);
        model.addAttribute("totalProcesos", resumen.totalProcesos());
        model.addAttribute("procesosCriticos", resumen.procesosCriticos());
        model.addAttribute("procesosAltos", resumen.procesosAltos());
        model.addAttribute("spofPersonas", resumen.spofPersonas());
        model.addAttribute("spofActivos", resumen.spofActivos());

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
