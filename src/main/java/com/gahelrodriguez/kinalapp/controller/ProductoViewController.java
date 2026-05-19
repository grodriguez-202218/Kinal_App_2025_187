package com.gahelrodriguez.kinalapp.controller;

import com.gahelrodriguez.kinalapp.entity.Producto;
import com.gahelrodriguez.kinalapp.service.IProductoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos/vista")
public class ProductoViewController {

    private final IProductoService productoService;

    public ProductoViewController(IProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("titulo", "Lista de Productos");
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("accion", "guardar");
        return "productos/formulario";
    }

    @GetMapping("/editar/{codigo}")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String mostrarFormularioEditar(@PathVariable Long codigo, Model model,
                                          RedirectAttributes flash) {
        return productoService.buscarPorCodigo(codigo)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("titulo", "Editar Producto");
                    model.addAttribute("accion", "actualizar");
                    return "productos/formulario";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Producto no encontrado.");
                    return "redirect:/productos/vista";
                });
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String guardar(@ModelAttribute Producto producto, RedirectAttributes flash) {
        try {
            productoService.guardar(producto);
            flash.addFlashAttribute("exito", "Producto guardado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/productos/vista";
    }

    @PostMapping("/actualizar/{codigo}")
    @PreAuthorize("hasAnyRole('USER','VENDEDOR','ADMIN')")
    public String actualizar(@PathVariable Long codigo,
                             @ModelAttribute Producto producto,
                             RedirectAttributes flash) {
        try {
            productoService.actualizar(codigo, producto);
            flash.addFlashAttribute("exito", "Producto actualizado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/productos/vista";
    }

    @GetMapping("/eliminar/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long codigo, RedirectAttributes flash) {
        try {
            productoService.eliminar(codigo);
            flash.addFlashAttribute("exito",
                    "Producto eliminado (o desactivado si tenía ventas asociadas).");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/productos/vista";
    }
}