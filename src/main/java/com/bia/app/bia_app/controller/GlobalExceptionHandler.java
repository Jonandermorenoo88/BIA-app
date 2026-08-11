package com.bia.app.bia_app.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Manejo global de excepciones para mostrar páginas de error amigables
 * en vez del Whitelabel Error Page genérico de Spring Boot.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(Model model, IllegalArgumentException ex) {
        model.addAttribute("errorTitulo", "Recurso no encontrado");
        model.addAttribute("errorMensaje", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(Model model, Exception ex) {
        model.addAttribute("errorTitulo", "Error inesperado");
        model.addAttribute("errorMensaje", "Ha ocurrido un error interno. Por favor, inténtalo de nuevo más tarde.");
        return "error";
    }
}
