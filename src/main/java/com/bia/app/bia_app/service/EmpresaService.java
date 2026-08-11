package com.bia.app.bia_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.repository.EmpresaRepository;

import java.util.List;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Empresa guardar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empresa obtenerPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + id));
    }

    public Empresa editar(Long id, Empresa datos) {
        Empresa empresa = obtenerPorId(id);
        empresa.setNombre(datos.getNombre());
        empresa.setSector(datos.getSector());
        empresa.setTamano(datos.getTamano());
        return empresaRepository.save(empresa);
    }

    public void eliminar(Long id) {
        Empresa empresa = obtenerPorId(id);
        empresaRepository.delete(empresa);
    }
}
