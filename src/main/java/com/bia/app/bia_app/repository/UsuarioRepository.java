package com.bia.app.bia_app.repository;

import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.model.Rol;
import com.bia.app.bia_app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndDebeCambiarPasswordTrue(String username);

    long countByRolAndActivoTrue(Rol rol);

    List<Usuario> findAllByOrderByUsernameAsc();

    @Query("select count(u) from Usuario u join u.empresas e where u.id = :usuarioId and e.id = :empresaId")
    long contarAcceso(@Param("usuarioId") Long usuarioId, @Param("empresaId") Long empresaId);

    @Query("select e from Usuario u join u.empresas e where u.id = :usuarioId order by e.nombre")
    List<Empresa> findEmpresasDeUsuario(@Param("usuarioId") Long usuarioId);

    @Query("select u from Usuario u join u.empresas e where e.id = :empresaId")
    List<Usuario> findByEmpresaId(@Param("empresaId") Long empresaId);
}
