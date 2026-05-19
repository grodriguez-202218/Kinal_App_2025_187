package com.gahelrodriguez.kinalapp.controller;

import com.gahelrodriguez.kinalapp.entity.Cliente;
import com.gahelrodriguez.kinalapp.service.IClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes/vista")
public class ClienteViewController {

    private final IClienteService clienteService;

    public ClienteViewController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("titulo", "Lista de Clientes");
        return "clientes/lista";
    }


    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("titulo", "Nuevo Cliente");
        model.addAttribute("accion", "guardar");
        return "clientes/formulario";
    }

    @GetMapping("/editar/{dpi}")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String mostrarFormularioEditar(@PathVariable Long dpi, Model model,
                                          RedirectAttributes flash) {
        return clienteService.busacarPorDPI(dpi)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    model.addAttribute("titulo", "Editar Cliente");
                    model.addAttribute("accion", "actualizar");
                    return "clientes/formulario";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Cliente no encontrado con DPI " + dpi);
                    return "redirect:/clientes/vista";
                });
    }


    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String guardar(@ModelAttribute Cliente cliente,
                          RedirectAttributes flash) {
        try {
            clienteService.guardar(cliente);
            flash.addFlashAttribute("exito", "Cliente guardado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/clientes/vista";
    }

    @PostMapping("/actualizar/{dpi}")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String actualizar(@PathVariable Long dpi,
                             @ModelAttribute Cliente cliente,
                             RedirectAttributes flash) {
        try {
            clienteService.actualizar(dpi, cliente);
            flash.addFlashAttribute("exito", "Cliente actualizado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/clientes/vista";
    }

    @GetMapping("/eliminar/{dpi}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long dpi, RedirectAttributes flash) {
        try {
            clienteService.eliminar(dpi);
            flash.addFlashAttribute("exito",
                    "Cliente eliminado (o desactivado si tenía ventas asociadas).");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/clientes/vista";
    }
}