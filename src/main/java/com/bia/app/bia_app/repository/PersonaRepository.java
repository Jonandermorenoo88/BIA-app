package com.bia.app.bia_app.repository;

import com.bia.app.bia_app.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByIdAndEmpresa_Id(Long id, Long empresaId);
}
