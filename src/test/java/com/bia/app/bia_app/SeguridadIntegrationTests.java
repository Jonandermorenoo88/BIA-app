package com.bia.app.bia_app;

import com.bia.app.bia_app.model.*;
import com.bia.app.bia_app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica autenticación, roles y aislamiento de datos entre empresas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadIntegrationTests {

    @Autowired MockMvc mvc;
    @Autowired EmpresaRepository empresaRepo;
    @Autowired BiaProyectoRepository biaRepo;
    @Autowired ProcesoCriticoRepository procesoRepo;
    @Autowired ActivoTecnologicoRepository activoRepo;
    @Autowired PersonaRepository personaRepo;
    @Autowired UsuarioRepository usuarioRepo;
    @Autowired PasswordEncoder encoder;
    @Autowired TransactionTemplate tx;

    Empresa empresaA, empresaB;
    BiaProyecto biaA, biaB;
    ProcesoCritico procesoA;
    ActivoTecnologico activoA, activoB;

    @BeforeEach
    void datos() {
        tx.executeWithoutResult(s -> {
            usuarioRepo.findAll().forEach(u -> { u.getEmpresas().clear(); usuarioRepo.save(u); });
            usuarioRepo.deleteAll(usuarioRepo.findAll().stream().filter(u -> !u.getUsername().equals("admin")).toList());
            procesoRepo.deleteAll();
            biaRepo.deleteAll();
            activoRepo.deleteAll();
            personaRepo.deleteAll();
            empresaRepo.deleteAll();
        });

        empresaA = empresa("Empresa A");
        empresaB = empresa("Empresa B");
        biaA = bia(empresaA);
        biaB = bia(empresaB);
        activoA = activo(empresaA, "ERP A");
        activoB = activo(empresaB, "ERP B");
        procesoA = proceso(biaA);
        proceso(biaB);

        usuario("consultorA", Rol.CONSULTOR, empresaA);
        usuario("clienteA", Rol.CLIENTE, empresaA);
        usuario("jefe", Rol.ADMIN);
    }

    // ---------- Autenticación ----------

    @Test
    void sinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/empresas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginCorrectoYErroneo() throws Exception {
        mvc.perform(formLogin("/login").user("consultorA").password("Contrasena-Segura-123"))
                .andExpect(authenticated().withUsername("consultorA"));
        mvc.perform(formLogin("/login").user("consultorA").password("incorrecta"))
                .andExpect(unauthenticated());
    }

    @Test
    void bloqueoTrasCincoIntentosFallidos() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(formLogin("/login").user("clienteA").password("mala-" + i));
        }
        // Ni con la contraseña buena entra mientras dura el bloqueo
        mvc.perform(formLogin("/login").user("clienteA").password("Contrasena-Segura-123"))
                .andExpect(unauthenticated());
        assertThat(usuarioRepo.findByUsername("clienteA").orElseThrow().isBloqueado()).isTrue();
    }

    @Test
    void postSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/empresas/guardar").with(como("consultorA", Rol.CONSULTOR))
                        .param("nombre", "X").param("sector", "Y").param("tamano", "1"))
                .andExpect(status().isForbidden());
    }

    // ---------- Aislamiento entre empresas ----------

    @Test
    void consultorSoloVeSusEmpresas() throws Exception {
        mvc.perform(get("/empresas").with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Empresa A")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Empresa B"))));
    }

    @Test
    void consultorNoPuedeVerEmpresaAjena() throws Exception {
        mvc.perform(get("/empresas/" + empresaB.getId()).with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/empresas/" + empresaB.getId() + "/bias/" + biaB.getId() + "/informe-ejecutivo")
                        .with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/empresas/" + empresaB.getId() + "/bias/" + biaB.getId() + "/exportar-excel")
                        .with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void noSePuedeLeerBiaAjenoCambiandoSoloElIdDelBia() throws Exception {
        // Empresa propia en la URL, pero BIA de otra empresa (IDOR)
        mvc.perform(get("/empresas/" + empresaA.getId() + "/bias/" + biaB.getId())
                        .with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().isNotFound());
    }

    @Test
    void noSePuedeVincularActivoDeOtraEmpresa() throws Exception {
        mvc.perform(post("/empresas/" + empresaA.getId() + "/bias/" + biaA.getId()
                        + "/procesos/" + procesoA.getId() + "/vincular-activo")
                        .param("idActivo", activoB.getId().toString())
                        .with(como("consultorA", Rol.CONSULTOR)).with(csrf()))
                .andExpect(status().isNotFound());
        assertThat(procesoRepo.findByActivos_Id(activoB.getId())).isEmpty();
    }

    @Test
    void noSePuedenInyectarRelacionesAlCrearProceso() throws Exception {
        mvc.perform(post("/empresas/" + empresaA.getId() + "/bias/" + biaA.getId() + "/procesos/guardar")
                        .param("nombre", "Proceso inyectado").param("descripcion", "d")
                        .param("rtoHoras", "4").param("rpo", "1h").param("impacto", "3").param("probabilidad", "3")
                        .param("activos[0].id", activoB.getId().toString())
                        .with(como("consultorA", Rol.CONSULTOR)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(procesoRepo.findByActivos_Id(activoB.getId())).isEmpty();
    }

    @Test
    void consultorPuedeTrabajarEnSuEmpresa() throws Exception {
        mvc.perform(post("/empresas/" + empresaA.getId() + "/bias/" + biaA.getId()
                        + "/procesos/" + procesoA.getId() + "/vincular-activo")
                        .param("idActivo", activoA.getId().toString())
                        .with(como("consultorA", Rol.CONSULTOR)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(procesoRepo.findByActivos_Id(activoA.getId())).hasSize(1);
    }

    // ---------- Roles ----------

    @Test
    void clienteEsSoloLectura() throws Exception {
        mvc.perform(get("/empresas/" + empresaA.getId() + "/bias/" + biaA.getId() + "/informe-ejecutivo")
                        .with(como("clienteA", Rol.CLIENTE)))
                .andExpect(status().isOk());
        mvc.perform(post("/empresas/" + empresaA.getId() + "/bias/" + biaA.getId() + "/procesos/" + procesoA.getId() + "/eliminar")
                        .with(como("clienteA", Rol.CLIENTE)).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(procesoRepo.findById(procesoA.getId())).isPresent();
    }

    @Test
    void soloAdminGestionaUsuariosYEliminaEmpresas() throws Exception {
        mvc.perform(get("/admin/usuarios").with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/empresas/" + empresaA.getId() + "/eliminar")
                        .with(como("consultorA", Rol.CONSULTOR)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/usuarios").with(como("jefe", Rol.ADMIN)))
                .andExpect(status().isOk());
        mvc.perform(post("/empresas/" + empresaB.getId() + "/eliminar")
                        .with(como("jefe", Rol.ADMIN)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(empresaRepo.findById(empresaB.getId())).isEmpty();
    }

    @Test
    void contrasenaTemporalObligaACambiarla() throws Exception {
        tx.executeWithoutResult(s -> {
            Usuario u = usuarioRepo.findByUsername("consultorA").orElseThrow();
            u.setDebeCambiarPassword(true);
            usuarioRepo.save(u);
        });
        mvc.perform(get("/empresas").with(como("consultorA", Rol.CONSULTOR)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cuenta/password"));
    }

    // ---------- helpers ----------

    private RequestPostProcessor como(String username, Rol rol) {
        return user(username).roles(rol.name());
    }

    private Empresa empresa(String nombre) {
        Empresa e = new Empresa();
        e.setNombre(nombre);
        e.setSector("Test");
        e.setTamano("10");
        return empresaRepo.save(e);
    }

    private BiaProyecto bia(Empresa e) {
        BiaProyecto b = new BiaProyecto();
        b.setNombre("BIA " + e.getNombre());
        b.setDescripcion("desc");
        b.setAnio("2026");
        b.setEmpresa(e);
        return biaRepo.save(b);
    }

    private ActivoTecnologico activo(Empresa e, String nombre) {
        ActivoTecnologico a = new ActivoTecnologico();
        a.setNombre(nombre);
        a.setTipo("Servidor");
        a.setEmpresa(e);
        return activoRepo.save(a);
    }

    private ProcesoCritico proceso(BiaProyecto b) {
        ProcesoCritico p = new ProcesoCritico();
        p.setNombre("Proceso " + b.getNombre());
        p.setDescripcion("desc");
        p.setRtoHoras(4);
        p.setRpo("1h");
        p.setImpacto(4);
        p.setProbabilidad(4);
        p.calcularYSetearCriticidad();
        p.setBiaProyecto(b);
        return procesoRepo.save(p);
    }

    private void usuario(String username, Rol rol, Empresa... empresas) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombre(username);
        u.setRol(rol);
        u.setPasswordHash(encoder.encode("Contrasena-Segura-123"));
        u.setEmpresas(new java.util.HashSet<>(Set.of(empresas)));
        usuarioRepo.save(u);
    }
}
