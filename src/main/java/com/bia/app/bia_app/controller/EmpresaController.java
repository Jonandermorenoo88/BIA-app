package com.bia.app.bia_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bia.app.bia_app.model.Empresa;
import com.bia.app.bia_app.service.EmpresaService;

@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public String verEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.listar());
        return "empresas";
    }

    @PostMapping("/guardar")
    public String guardar(Empresa empresa) {
        empresaService.guardar(empresa);
        return "redirect:/empresas";
    }
}