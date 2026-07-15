package com.bia.app.bia_app.model;

import jakarta.persistence.*;

@Entity
public class ProcesoCritico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    
    private Integer rtoHoras; // Recovery Time Objective en Horas
    private String rpo; // Recovery Point Objective
    
    private Integer impacto; // 1 (Bajo) a 5 (Muy Alto)
    private Integer probabilidad; // 1 (Rara) a 5 (Casi Cierta)
    private Integer criticidad; // Autocalculado (Impacto + Urgencia) 2 a 10

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bia_proyecto_id")
    private BiaProyecto biaProyecto;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "proceso_activo",
        joinColumns = @JoinColumn(name = "proceso_id"),
        inverseJoinColumns = @JoinColumn(name = "activo_id")
    )
    private java.util.List<ActivoTecnologico> activos = new java.util.ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "proceso_persona",
        joinColumns = @JoinColumn(name = "proceso_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private java.util.List<Persona> personas = new java.util.ArrayList<>();

    public java.util.List<ActivoTecnologico> getActivos() {
        return activos;
    }

    public void setActivos(java.util.List<ActivoTecnologico> activos) {
        this.activos = activos;
    }

    public java.util.List<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(java.util.List<Persona> personas) {
        this.personas = personas;
    }
}
