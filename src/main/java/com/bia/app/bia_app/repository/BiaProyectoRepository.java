package com.bia.app.bia_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bia.app.bia_app.model.BiaProyecto;

import java.util.Optional;

public interface BiaProyectoRepository extends JpaRepository<BiaProyecto, Long> {

    Optional<BiaProyecto> findByIdAndEmpresa_Id(Long id, Long empresaId);
}
