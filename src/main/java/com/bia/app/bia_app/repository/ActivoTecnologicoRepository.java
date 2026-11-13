package com.bia.app.bia_app.repository;

import com.bia.app.bia_app.model.ActivoTecnologico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivoTecnologicoRepository extends JpaRepository<ActivoTecnologico, Long> {

    Optional<ActivoTecnologico> findByIdAndEmpresa_Id(Long id, Long empresaId);
}
