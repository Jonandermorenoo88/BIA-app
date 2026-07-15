package com.bia.app.bia_app.model;

import jakarta.persistence.*;

@Entity
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String sector;
    private String tamaño;

    // getters y setters
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

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getTamaño() {
        return tamaño;
    }

    public void setTamaño(String tamaño) {
        this.tamaño = tamaño;
    }

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Persona> personas = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BiaProyecto> bias = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ActivoTecnologico> activos = new java.util.ArrayList<>();

    public java.util.List<BiaProyecto> getBias() {
        return bias;
    }

    public void setBias(java.util.List<BiaProyecto> bias) {
        this.bias = bias;
    }

    public java.util.List<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(java.util.List<Persona> personas) {
        this.personas = personas;
    }

    public java.util.List<ActivoTecnologico> getActivos() {
        return activos;
    }

    public void setActivos(java.util.List<ActivoTecnologico> activos) {
        this.activos = activos;
    }
}