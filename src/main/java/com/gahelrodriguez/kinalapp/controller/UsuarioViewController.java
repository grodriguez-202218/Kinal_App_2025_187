package com.gahelrodriguez.kinalapp.controller;

import com.gahelrodriguez.kinalapp.entity.Usuario;
import com.gahelrodriguez.kinalapp.service.IUsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios/vista")
public class UsuarioViewController {

    private final IUsuarioService usuarioService;

    public UsuarioViewController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("titulo", "Lista de Usuarios");
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("titulo", "Nuevo Usuario");
        model.addAttribute("accion", "guardar");
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioEditar(@PathVariable Long codigo, Model model,
                                          RedirectAttributes flash) {
        return usuarioService.buscarPorCodigo(codigo)
                .map(usuario -> {
                    model.addAttribute("usuario", usuario);
                    model.addAttribute("titulo", "Editar Usuario");
                    model.addAttribute("accion", "actualizar");
                    return "usuarios/formulario";
                })
                .orElseGet(() -> {
                    flash.addFlashAttribute("error", "Usuario no encontrado.");
                    return "redirect:/usuarios/vista";
                });
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute Usuario usuario, RedirectAttributes flash) {
        try {
            usuarioService.guardar(usuario);
            flash.addFlashAttribute("exito", "Usuario guardado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/usuarios/vista";
    }

    @PostMapping("/actualizar/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizar(@PathVariable Long codigo, @ModelAttribute Usuario usuario,
                             RedirectAttributes flash) {
        try {
            usuarioService.actualizar(codigo, usuario);
            flash.addFlashAttribute("exito", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/usuarios/vista";
    }

    @GetMapping("/eliminar/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long codigo, RedirectAttributes flash) {
        try {
            usuarioService.eliminar(codigo);
            flash.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/usuarios/vista";
    }
}