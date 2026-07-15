package com.bia.app.bia_app.repository;

import com.bia.app.bia_app.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
}
