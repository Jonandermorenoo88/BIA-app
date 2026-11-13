package com.bia.app.bia_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Rol;
import com.bia.app.bia_app.model.Usuario;
import com.bia.app.bia_app.repository.EmpresaRepository;
import com.bia.app.bia_app.repository.UsuarioRepository;
import com.bia.app.bia_app.security.AccesoService;

import java.util.List;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AccesoService accesoService;

    public EmpresaService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
                          AccesoService accesoService) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.accesoService = accesoService;
    }

    /** Crea la empresa. Si la crea un consultor, se le asigna automáticamente. */
    public Empresa guardar(Empresa empresa) {
        Empresa guardada = empresaRepository.save(empresa);
        Usuario actual = accesoService.usuarioActual();
        if (actual.getRol() != Rol.ADMIN) {
            Usuario u = usuarioRepository.findById(actual.getId()).orElseThrow();
            u.getEmpresas().add(guardada);
            usuarioRepository.save(u);
        }
        return guardada;
    }

    /** Solo las empresas que el usuario conectado puede ver. */
    @Transactional(readOnly = true)
    public List<Empresa> listarVisibles() {
        return accesoService.empresasVisibles();
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
        // Quitar la empresa de los usuarios que la tienen asignada (tabla usuario_empresa)
        for (Usuario u : usuarioRepository.findByEmpresaId(id)) {
            u.getEmpresas().remove(empresa);
            usuarioRepository.save(u);
        }
        empresaRepository.delete(empresa);
    }
}
