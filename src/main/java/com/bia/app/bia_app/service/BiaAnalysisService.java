package com.bia.app.bia_app.service;

import com.bia.app.bia_app.model.ActivoTecnologico;
import com.bia.app.bia_app.model.BiaProyecto;
import com.bia.app.bia_app.model.Persona;
import com.bia.app.bia_app.model.ProcesoCritico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que centraliza la lógica de análisis del BIA:
 * conteo de procesos por nivel de riesgo y detección de SPOFs.
 */
@Service
@Transactional(readOnly = true)
public class BiaAnalysisService {

    /**
     * DTO inmutable con los resultados del análisis de un BIA.
     */
    public record BiaResumen(
            int totalProcesos,
            long procesosCriticos,
            long procesosAltos,
            List<Persona> spofPersonas,
            List<ActivoTecnologico> spofActivos
    ) {}

    /**
     * Calcula el resumen ejecutivo de un BIA: totales, clasificación de riesgo y SPOFs.
     */
    public BiaResumen calcularResumen(BiaProyecto bia) {
        List<ProcesoCritico> procesos = bia.getProcesos();
        int totalProcesos = procesos.size();

        // Clasificación de riesgo centralizada en ProcesoCritico (getNivelRiesgo/isCritico/isAlto)
        long procesosCriticos = procesos.stream()
                .filter(ProcesoCritico::isCritico)
                .count();
        long procesosAltos = procesos.stream()
                .filter(ProcesoCritico::isAlto)
                .count();

        // Empleados SPOF (Asignados a múltiples procesos en este BIA)
        List<Persona> spofPersonas = detectarSpofPersonas(procesos);

        // Activos SPOF
        List<ActivoTecnologico> spofActivos = detectarSpofActivos(procesos);

        return new BiaResumen(totalProcesos, procesosCriticos, procesosAltos, spofPersonas, spofActivos);
    }

    private List<Persona> detectarSpofPersonas(List<ProcesoCritico> procesos) {
        Map<Persona, Integer> conteo = new HashMap<>();
        for (ProcesoCritico p : procesos) {
            for (Persona per : p.getPersonas()) {
                conteo.merge(per, 1, Integer::sum);
            }
        }
        List<Persona> spof = new ArrayList<>();
        for (Map.Entry<Persona, Integer> entry : conteo.entrySet()) {
            if (entry.getValue() > 1) {
                spof.add(entry.getKey());
            }
        }
        return spof;
    }

    private List<ActivoTecnologico> detectarSpofActivos(List<ProcesoCritico> procesos) {
        Map<ActivoTecnologico, Integer> conteo = new HashMap<>();
        for (ProcesoCritico p : procesos) {
            for (ActivoTecnologico act : p.getActivos()) {
                conteo.merge(act, 1, Integer::sum);
            }
        }
        List<ActivoTecnologico> spof = new ArrayList<>();
        for (Map.Entry<ActivoTecnologico, Integer> entry : conteo.entrySet()) {
            if (entry.getValue() > 1) {
                spof.add(entry.getKey());
            }
        }
        return spof;
    }
}
