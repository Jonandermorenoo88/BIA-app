package com.bia.app.bia_app.model;

/**
 * Roles de la aplicación.
 * <ul>
 *   <li>ADMIN: acceso total, gestiona usuarios y puede eliminar empresas.</li>
 *   <li>CONSULTOR: crea y edita los BIA de las empresas que tiene asignadas.</li>
 *   <li>CLIENTE: solo lectura (dashboard, informe y Excel) de sus empresas asignadas.</li>
 * </ul>
 */
public enum Rol {
    ADMIN("Administrador"),
    CONSULTOR("Consultor"),
    CLIENTE("Cliente (solo lectura)");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
