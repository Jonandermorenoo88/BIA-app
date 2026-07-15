package com.bia.app.bia_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bia.app.bia_app.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}