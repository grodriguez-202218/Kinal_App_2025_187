package com.gahelrodriguez.kinalapp.controller;

import com.gahelrodriguez.kinalapp.entity.Usuario;
import com.gahelrodriguez.kinalapp.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RegistroController {

    // Roles permitidos en el registro público
    private static final List<String> ROLES_VALIDOS = List.of("USER", "VENDEDOR", "ADMIN");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    public RegistroController(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String rol,
            Model model) {

        // Validar que el username no exista
        if (usuarioRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error",
                    "El username '" + username + "' ya está en uso.");
            return "registro";
        }

        // Validar rol
        if (!ROLES_VALIDOS.contains(rol)) {
            model.addAttribute("error", "Rol no válido. Elige USER, VENDEDOR o ADMIN.");
            return "registro";
        }

        // Validar longitud mínima de contraseña
        if (password == null || password.trim().length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "registro";
        }

        // Crear y guardar el usuario con contraseña encriptada (BCrypt)
        Usuario nuevo = new Usuario();
        nuevo.setUsername(username.trim());
        nuevo.setPassword(passwordEncoder.encode(password)); // ← BCrypt automático
        nuevo.setEmail(email.trim());
        nuevo.setRol(rol);
        nuevo.setEstado(1L); // activo por defecto

        usuarioRepository.save(nuevo);

        return "redirect:/login?registrado";
    }
}