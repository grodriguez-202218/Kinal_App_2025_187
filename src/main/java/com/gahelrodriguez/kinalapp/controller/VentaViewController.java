package com.gahelrodriguez.kinalapp.controller;

import com.gahelrodriguez.kinalapp.entity.Venta;
import com.gahelrodriguez.kinalapp.service.IClienteService;
import com.gahelrodriguez.kinalapp.service.IUsuarioService;
import com.gahelrodriguez.kinalapp.service.IVentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ventas/vista")
public class VentaViewController {

    private final IVentaService    ventaService;
    private final IClienteService  clienteService;
    private final IUsuarioService  usuarioService;

    public VentaViewController(IVentaService ventaService,
                               IClienteService clienteService,
                               IUsuarioService usuarioService) {
        this.ventaService   = ventaService;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String listarVentas(Model model) {
        model.addAttribute("ventas", ventaService.listarTodos());
        model.addAttribute("titulo", "Lista de Ventas");
        return "ventas/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("venta", new Venta());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("titulo", "Nueva Venta");
        model.addAttribute("accion", "guardar");
        return "ventas/formulario";
    }

    @GetMapping("/editar/{codigo}")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String mostrarFormularioEditar(@PathVariable Long codigo, Model model,
                                          RedirectAttributes flash) {
        return ventaService.buscarPorCodigo(codigo)
                .map(venta -> {
                    model.addAttribute("venta", venta);
                    model.addAttribute("clientes", clienteService.listarTodos());
                    model.addAttribute("usuarios", usuarioService.listarTodos());
                    model.addAttribute("titulo", "Editar Venta");
                    model.addAttribute("accion", "actualizar");
                    return "ventas/formulario";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Venta no encontrada.");
                    return "redirect:/ventas/vista";
                });
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String guardar(@ModelAttribute Venta venta, RedirectAttributes flash) {
        try {
            ventaService.guardar(venta);
            flash.addFlashAttribute("exito", "Venta guardada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/ventas/vista";
    }

    @PostMapping("/actualizar/{codigo}")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public String actualizar(@PathVariable Long codigo, @ModelAttribute Venta venta,
                             RedirectAttributes flash) {
        try {
            ventaService.actualizar(codigo, venta);
            flash.addFlashAttribute("exito", "Venta actualizada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/ventas/vista";
    }

    @GetMapping("/eliminar/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long codigo, RedirectAttributes flash) {
        try {
            ventaService.eliminar(codigo);
            flash.addFlashAttribute("exito", "Venta eliminada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/ventas/vista";
    }
}