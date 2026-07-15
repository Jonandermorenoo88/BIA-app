package com.bia.app.bia_app.service;

import org.springframework.stereotype.Service;
import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.repository.EmpresaRepository;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Empresa guardar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }
}
