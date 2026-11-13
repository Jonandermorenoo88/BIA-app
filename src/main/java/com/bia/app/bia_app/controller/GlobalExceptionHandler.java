package com.bia.app.bia_app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Manejo global de excepciones para mostrar páginas de error amigables
 * en vez del Whitelabel Error Page genérico de Spring Boot.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Evita la asignación masiva: un formulario no puede fijar ids, relaciones con otras
     * entidades ni campos calculados (p. ej. enviar activos[0].id=99 para enlazar un
     * activo de otra empresa). Esas relaciones solo se crean desde los servicios.
     */
    @InitBinder
    public void bloquearCamposSensibles(WebDataBinder binder) {
        binder.setDisallowedFields(
                "id", "empresa", "empresa.*", "biaProyecto", "biaProyecto.*",
                "activos*", "personas*", "procesos*", "bias*", "criticidad");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(Model model, AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        model.addAttribute("errorTitulo", "Acceso denegado");
        model.addAttribute("errorMensaje", "No tienes permiso para acceder a este recurso.");
        return "error";
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResource(Model model) {
        model.addAttribute("errorTitulo", "Página no encontrada");
        model.addAttribute("errorMensaje", "La ruta solicitada no existe.");
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model, IllegalArgumentException ex) {
        model.addAttribute("errorTitulo", "Recurso no encontrado");
        model.addAttribute("errorMensaje", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Model model, Exception ex) {
        log.error("Error no controlado", ex);
        model.addAttribute("errorTitulo", "Error inesperado");
        model.addAttribute("errorMensaje", "Ha ocurrido un error interno. Por favor, inténtalo de nuevo más tarde.");
        return "error";
    }
}
