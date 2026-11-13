package com.bia.app.bia_app.service;

import com.bia.app.bia_app.model.ActivoTecnologico;
import com.bia.app.bia_app.model.BiaProyecto;
import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Persona;
import com.bia.app.bia_app.model.ProcesoCritico;
import com.bia.app.bia_app.repository.ActivoTecnologicoRepository;
import com.bia.app.bia_app.repository.BiaProyectoRepository;
import com.bia.app.bia_app.repository.EmpresaRepository;
import com.bia.app.bia_app.repository.PersonaRepository;
import com.bia.app.bia_app.repository.ProcesoCriticoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operaciones sobre BIA, procesos, personas y activos.
 * Todas reciben el id de empresa (y de BIA cuando aplica) y verifican que cada
 * recurso pertenece a esa empresa, para que no se pueda leer ni modificar datos
 * de otro cliente cambiando un id en la URL (IDOR).
 * El permiso del usuario sobre la empresa lo comprueba antes AccesoService.
 */
@Service
@Transactional
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

    // ===== CONSULTA CON VERIFICACIÓN DE PERTENENCIA =====

    @Transactional(readOnly = true)
    public BiaProyecto obtenerBia(Long idEmpresa, Long idBia) {
        return biaRepository.findByIdAndEmpresa_Id(idBia, idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("BIA no encontrado"));
    }

    private ProcesoCritico obtenerProceso(Long idEmpresa, Long idBia, Long idProceso) {
        return procesoRepository.findByIdAndBiaProyecto_IdAndBiaProyecto_Empresa_Id(idProceso, idBia, idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado"));
    }

    private ActivoTecnologico obtenerActivo(Long idEmpresa, Long idActivo) {
        return activoRepository.findByIdAndEmpresa_Id(idActivo, idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Activo no encontrado"));
    }

    private Persona obtenerPersona(Long idEmpresa, Long idPersona) {
        return personaRepository.findByIdAndEmpresa_Id(idPersona, idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
    }

    private Empresa obtenerEmpresa(Long idEmpresa) {
        return empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
    }

    // ===== PROCESOS =====

    public void guardarProcesoCritico(Long idEmpresa, Long idBia, ProcesoCritico proceso) {
        BiaProyecto bia = obtenerBia(idEmpresa, idBia);
        proceso.setBiaProyecto(bia);
        proceso.calcularYSetearCriticidad();
        procesoRepository.save(proceso);
    }

    public void editarProceso(Long idEmpresa, Long idBia, Long idProceso, ProcesoCritico datos) {
        ProcesoCritico proceso = obtenerProceso(idEmpresa, idBia, idProceso);
        proceso.setNombre(datos.getNombre());
        proceso.setDescripcion(datos.getDescripcion());
        proceso.setRtoHoras(datos.getRtoHoras());
        proceso.setRpo(datos.getRpo());
        proceso.setImpacto(datos.getImpacto());
        proceso.setProbabilidad(datos.getProbabilidad());
        proceso.calcularYSetearCriticidad();
        procesoRepository.save(proceso);
    }

    public void eliminarProceso(Long idEmpresa, Long idBia, Long idProceso) {
        ProcesoCritico proceso = obtenerProceso(idEmpresa, idBia, idProceso);
        proceso.getBiaProyecto().getProcesos().remove(proceso);
        procesoRepository.delete(proceso);
    }

    // ===== BIA PROYECTOS =====

    public void crearBiaProyecto(Long idEmpresa, BiaProyecto bia) {
        bia.setEmpresa(obtenerEmpresa(idEmpresa));
        biaRepository.save(bia);
    }

    public void eliminarBiaProyecto(Long idEmpresa, Long idBia) {
        BiaProyecto bia = obtenerBia(idEmpresa, idBia);
        bia.getEmpresa().getBias().remove(bia);
        biaRepository.delete(bia);
    }

    // ===== PERSONAS =====

    public void guardarPersona(Long idEmpresa, Persona persona) {
        persona.setEmpresa(obtenerEmpresa(idEmpresa));
        personaRepository.save(persona);
    }

    public void eliminarPersona(Long idEmpresa, Long idPersona) {
        Persona persona = obtenerPersona(idEmpresa, idPersona);
        // Quitar primero las vinculaciones con procesos (tabla proceso_persona)
        for (ProcesoCritico p : procesoRepository.findByPersonas_Id(idPersona)) {
            p.getPersonas().remove(persona);
        }
        persona.getEmpresa().getPersonas().remove(persona);
        personaRepository.delete(persona);
    }

    // ===== ACTIVOS =====

    public void guardarActivoTecnologico(Long idEmpresa, ActivoTecnologico activo) {
        activo.setEmpresa(obtenerEmpresa(idEmpresa));
        activoRepository.save(activo);
    }

    public void eliminarActivo(Long idEmpresa, Long idActivo) {
        ActivoTecnologico activo = obtenerActivo(idEmpresa, idActivo);
        // Quitar primero las vinculaciones con procesos (tabla proceso_activo)
        for (ProcesoCritico p : procesoRepository.findByActivos_Id(idActivo)) {
            p.getActivos().remove(activo);
        }
        activo.getEmpresa().getActivos().remove(activo);
        activoRepository.delete(activo);
    }

    // ===== VINCULACIÓN / DESVINCULACIÓN =====

    public void vincularActivoAProceso(Long idEmpresa, Long idBia, Long idProceso, Long idActivo) {
        ProcesoCritico proceso = obtenerProceso(idEmpresa, idBia, idProceso);
        ActivoTecnologico activo = obtenerActivo(idEmpresa, idActivo);
        if (!proceso.getActivos().contains(activo)) {
            proceso.getActivos().add(activo);
            procesoRepository.save(proceso);
        }
    }

    public void desvincularActivoDeProceso(Long idEmpresa, Long idBia, Long idProceso, Long idActivo) {
        ProcesoCritico proceso = obtenerProceso(idEmpresa, idBia, idProceso);
        proceso.getActivos().removeIf(a -> a.getId().equals(idActivo));
        procesoRepository.save(proceso);
    }

    public void vincularPersonaAProceso(Long idEmpresa, Long idBia, Long idProceso, Long idPersona) {
        ProcesoCritico proceso = obtenerProceso(idEmpresa, idBia, idProceso);
        Persona persona = obtenerPersona(idEmpresa, idPersona);
        if (!proceso.getPersonas().contains(persona)) {
            proceso.getPersonas().add(persona);
            procesoRepository.save(proceso);
        }
    }

    public void desvincularPersonaDeProceso(Long idEmpresa, Long idBia, Long idProceso, Long idPersona) {
        ProcesoCritico proceso = obtenerProceso(idEmpresa, idBia, idProceso);
        proceso.getPersonas().removeIf(p -> p.getId().equals(idPersona));
        procesoRepository.save(proceso);
    }
}
