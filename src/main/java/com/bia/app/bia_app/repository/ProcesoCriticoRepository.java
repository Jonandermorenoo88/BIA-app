package com.bia.app.bia_app.repository;

import com.bia.app.bia_app.model.ProcesoCritico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcesoCriticoRepository extends JpaRepository<ProcesoCritico, Long> {

    Optional<ProcesoCritico> findByIdAndBiaProyecto_IdAndBiaProyecto_Empresa_Id(Long id, Long biaId, Long empresaId);

    List<ProcesoCritico> findByActivos_Id(Long activoId);

    List<ProcesoCritico> findByPersonas_Id(Long personaId);
}
