package com.bia.app.bia_app.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String guardarProceso(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                 @Valid ProcesoCritico proceso, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
        }
        proceso.setId(null);
        activosService.guardarProcesoCritico(biaId, proceso);
        flash.addFlashAttribute("flashSuccess", "Proceso '" + proceso.getNombre() + "' creado correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/editar")
    public String editarProceso(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                @PathVariable("idProceso") Long idProceso,
                                @Valid ProcesoCritico proceso, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("flashError", "Error: " + result.getFieldError().getDefaultMessage());
            return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
        }
        activosService.editarProceso(idProceso, proceso);
        flash.addFlashAttribute("flashSuccess", "Proceso '" + proceso.getNombre() + "' actualizado correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/eliminar")
    public String eliminarProceso(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                  @PathVariable("idProceso") Long idProceso, RedirectAttributes flash) {
        activosService.eliminarProceso(idProceso);
        flash.addFlashAttribute("flashSuccess", "Proceso eliminado correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/vincular-activo")
    public String vincularActivo(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                 @PathVariable Long idProceso, @RequestParam Long idActivo, RedirectAttributes flash) {
        activosService.vincularActivoAProceso(idProceso, idActivo);
        flash.addFlashAttribute("flashSuccess", "Activo vinculado correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/desvincular-activo")
    public String desvincularActivo(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                    @PathVariable Long idProceso, @RequestParam Long idActivo, RedirectAttributes flash) {
        activosService.desvincularActivoDeProceso(idProceso, idActivo);
        flash.addFlashAttribute("flashSuccess", "Activo desvinculado correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/vincular-persona")
    public String vincularPersona(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                  @PathVariable Long idProceso, @RequestParam Long idPersona, RedirectAttributes flash) {
        activosService.vincularPersonaAProceso(idProceso, idPersona);
        flash.addFlashAttribute("flashSuccess", "Persona vinculada correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }

    @PostMapping("/{biaId}/procesos/{idProceso}/desvincular-persona")
    public String desvincularPersona(@PathVariable("empresaId") Long empresaId, @PathVariable("biaId") Long biaId,
                                     @PathVariable Long idProceso, @RequestParam Long idPersona, RedirectAttributes flash) {
        activosService.desvincularPersonaDeProceso(idProceso, idPersona);
        flash.addFlashAttribute("flashSuccess", "Persona desvinculada correctamente");
        return "redirect:/empresas/" + empresaId + "/bias/" + biaId;
    }
}
