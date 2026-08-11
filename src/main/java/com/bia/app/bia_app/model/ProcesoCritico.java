package com.bia.app.bia_app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ProcesoCritico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proceso es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El RTO es obligatorio")
    @Min(value = 0, message = "El RTO no puede ser negativo")
    private Integer rtoHoras; // Recovery Time Objective en Horas

    @NotBlank(message = "El RPO es obligatorio")
    private String rpo; // Recovery Point Objective

    @NotNull(message = "El impacto es obligatorio")
    @Min(value = 1, message = "El impacto mínimo es 1")
    @Max(value = 5, message = "El impacto máximo es 5")
    private Integer impacto; // 1 (Bajo) a 5 (Muy Alto)

    @NotNull(message = "La probabilidad es obligatoria")
    @Min(value = 1, message = "La probabilidad mínima es 1")
    @Max(value = 5, message = "La probabilidad máxima es 5")
    private Integer probabilidad; // 1 (Rara) a 5 (Casi Cierta)

    private Integer criticidad; // Autocalculado (Impacto + Urgencia) 2 a 10

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bia_proyecto_id")
    private BiaProyecto biaProyecto;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "proceso_activo",
        joinColumns = @JoinColumn(name = "proceso_id"),
        inverseJoinColumns = @JoinColumn(name = "activo_id")
    )
    private List<ActivoTecnologico> activos = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "proceso_persona",
        joinColumns = @JoinColumn(name = "proceso_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> personas = new ArrayList<>();

    // Métodos lógicos
    public int calcularUrgencia() {
        if (rtoHoras == null) return 1;
        if (rtoHoras <= 1) return 5;
        if (rtoHoras <= 4) return 4;
        if (rtoHoras <= 8) return 3;
        if (rtoHoras <= 24) return 2;
        return 1;
    }

    public void calcularYSetearCriticidad() {
        int urgencia = calcularUrgencia();
        int imp = this.impacto != null ? this.impacto : 1;
        this.criticidad = imp + urgencia;
    }

    // ===== LÓGICA DE RIESGO CENTRALIZADA =====

    /**
     * Riesgo total = Impacto x Probabilidad (escala 1-25).
     */
    public int getRiesgoTotal() {
        int imp = this.impacto != null ? this.impacto : 1;
        int prob = this.probabilidad != null ? this.probabilidad : 1;
        return imp * prob;
    }

    /**
     * Clasificación textual del riesgo según umbrales únicos.
     * CRITICO: 15-25 | ALTO: 6-14 | MEDIO_BAJO: 1-5
     */
    public String getNivelRiesgo() {
        int riesgo = getRiesgoTotal();
        if (riesgo >= 15) return "CRITICO";
        if (riesgo >= 6) return "ALTO";
        return "MEDIO_BAJO";
    }

    public boolean isCritico() {
        return "CRITICO".equals(getNivelRiesgo());
    }

    public boolean isAlto() {
        return "ALTO".equals(getNivelRiesgo());
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getRtoHoras() {
        return rtoHoras;
    }

    public void setRtoHoras(Integer rtoHoras) {
        this.rtoHoras = rtoHoras;
    }

    public String getRpo() {
        return rpo;
    }

    public void setRpo(String rpo) {
        this.rpo = rpo;
    }

    public Integer getImpacto() {
        return impacto;
    }

    public void setImpacto(Integer impacto) {
        this.impacto = impacto;
    }

    public Integer getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Integer probabilidad) {
        this.probabilidad = probabilidad;
    }

    public Integer getCriticidad() {
        return criticidad;
    }

    public void setCriticidad(Integer criticidad) {
        this.criticidad = criticidad;
    }

    public BiaProyecto getBiaProyecto() {
        return biaProyecto;
    }

    public void setBiaProyecto(BiaProyecto biaProyecto) {
        this.biaProyecto = biaProyecto;
    }

    public List<ActivoTecnologico> getActivos() {
        return activos;
    }

    public void setActivos(List<ActivoTecnologico> activos) {
        this.activos = activos;
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(List<Persona> personas) {
        this.personas = personas;
    }
}
