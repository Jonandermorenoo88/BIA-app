package com.bia.app.bia_app.service;

import com.bia.app.bia_app.model.ActivoTecnologico;
import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Persona;
import com.bia.app.bia_app.model.ProcesoCritico;
import com.bia.app.bia_app.repository.ActivoTecnologicoRepository;
import com.bia.app.bia_app.repository.EmpresaRepository;
import com.bia.app.bia_app.repository.PersonaRepository;
import com.bia.app.bia_app.repository.ProcesoCriticoRepository;
import org.springframework.stereotype.Service;

import com.bia.app.bia_app.model.BiaProyecto;
import com.bia.app.bia_app.repository.BiaProyectoRepository;

@Service
public class ActivosService {

    private final EmpresaRepository empresaRepository;
    private final ProcesoCriticoRepository procesoRepository;
    private final PersonaRepository personaRepository;
    private final ActivoTecnologicoRepository activoRepository;
    private final BiaProyectoRepository biaRepository;

    public ActivosService(EmpresaRepository empresaRepository,
                          ProcesoCriticoRepository procesoRepository,
                          PersonaRepository personaRepository,
                          ActivoTecnologicoRepository activoRepository,
                          BiaProyectoRepository biaRepository) {
        this.empresaRepository = empresaRepository;
        this.procesoRepository = procesoRepository;
        this.personaRepository = personaRepository;
        this.activoRepository = activoRepository;
        this.biaRepository = biaRepository;
    }

    public void guardarProcesoCritico(Long idBia, ProcesoCritico proceso) {
        BiaProyecto bia = biaRepository.findById(idBia)
                .orElseThrow(() -> new IllegalArgumentException("BIA no encontrado"));
        proceso.setBiaProyecto(bia);
        proceso.calcularYSetearCriticidad();
        procesoRepository.save(proceso);
    }

    public void crearBiaProyecto(Long idEmpresa, BiaProyecto bia) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        bia.setEmpresa(empresa);
        biaRepository.save(bia);
    }

    public void guardarPersona(Long idEmpresa, Persona persona) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        persona.setEmpresa(empresa);
        personaRepository.save(persona);
    }

    public void guardarActivoTecnologico(Long idEmpresa, ActivoTecnologico activo) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        activo.setEmpresa(empresa);
        activoRepository.save(activo);
    }

    public void vincularActivoAProceso(Long idProceso, Long idActivo) {
        ProcesoCritico proceso = procesoRepository.findById(idProceso)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado"));
        ActivoTecnologico activo = activoRepository.findById(idActivo)
                .orElseThrow(() -> new IllegalArgumentException("Activo no encontrado"));

        if (!proceso.getActivos().contains(activo)) {
            proceso.getActivos().add(activo);
            procesoRepository.save(proceso);
        }
    }

    public void vincularPersonaAProceso(Long idProceso, Long idPersona) {
        ProcesoCritico proceso = procesoRepository.findById(idProceso)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado"));
        Persona persona = personaRepository.findById(idPersona)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));

        if (!proceso.getPersonas().contains(persona)) {
            proceso.getPersonas().add(persona);
            procesoRepository.save(proceso);
        }
    }
}
