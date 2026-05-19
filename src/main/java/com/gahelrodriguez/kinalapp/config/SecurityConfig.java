package com.gahelrodriguez.kinalapp.config;

import com.gahelrodriguez.kinalapp.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Marca esta clase como una clase de configuración de Spring.
// Spring la escanea al arrancar y registra todos los @Bean que contiene.
@Configuration

// Activa la seguridad web de Spring Security para la aplicación.
@EnableWebSecurity

// Habilita el uso de @PreAuthorize en los controllers,
// lo que permite proteger métodos individuales por rol o permiso.
@EnableMethodSecurity
public class SecurityConfig {

    // Servicio personalizado que implementa UserDetailsService.
    // Spring Security lo usa para buscar el usuario en la BD durante el login.
    private final UsuarioDetailsService usuarioDetailsService;

    // Inyección del servicio por constructor (buena práctica recomendada por Spring).
    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    //  BEAN: PasswordEncoder
    // Configura BCrypt como el algoritmo para hashear contraseñas.
    // El parámetro 10 indica la "fuerza" (número de rondas de hashing).
    // Nunca se guarda la contraseña en texto plano; siempre se compara el hash.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // BEAN: DaoAuthenticationProvider
    // Conecta el sistema de autenticación de Spring con la base de datos.
    // Le indicamos:
    //   - dónde buscar usuarios (usuarioDetailsService)
    //   - cómo verificar la contraseña (passwordEncoder)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // BEAN: AuthenticationManager
    // Es el "gestor principal" de autenticación.
    // Lo obtenemos desde la configuración de Spring para que ya incluya
    // automáticamente el DaoAuthenticationProvider definido arriba.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // BEAN: SecurityFilterChain
    // Define todas las reglas de seguridad HTTP:
    //   - qué rutas son públicas o requieren autenticación/rol
    //   - cómo funciona el login y logout
    //   - qué rutas ignoran la protección CSRF
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Registra el provider de autenticación configurado arriba.
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // RUTAS PÚBLICAS: accesibles sin iniciar sesión.
                        // Incluye recursos estáticos (CSS, JS, imágenes),
                        // la página de registro y las variantes de la página de login.
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/registro", "/login", "/login?error", "/login?logout"
                        ).permitAll()

                        // SOLO ADMIN: gestión de usuarios del sistema.
                        // Ningún otro rol puede acceder a estas rutas.
                        .requestMatchers("/usuarios/**", "/usuarios/vista/**")
                        .hasRole("ADMIN")

                        // ADMIN y VENDEDOR: gestión de ventas y sus detalles.
                        // El rol USER no tiene acceso a estas secciones.
                        .requestMatchers("/ventas/**", "/ventas/vista/**", "/detalles/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")

                        // ADMIN, VENDEDOR y USER: pueden ver clientes y productos.
                        // Las acciones más sensibles (eliminar, editar) se restringen
                        // con @PreAuthorize directamente en cada método del controller.
                        .requestMatchers("/clientes/**", "/productos/**")
                        .hasAnyRole("ADMIN", "VENDEDOR", "USER")

                        // DASHBOARD y HOME: cualquier usuario autenticado puede acceder,
                        // sin importar cuál sea su rol.
                        .requestMatchers("/", "/dashboard").authenticated()

                        // CUALQUIER OTRA RUTA no listada arriba también requiere
                        // que el usuario haya iniciado sesión.
                        .anyRequest().authenticated()
                )

                // CONFIGURACIÓN DEL LOGIN
                .formLogin(form -> form
                        // URL de la página de login personalizada (no la de Spring por defecto).
                        .loginPage("/login")

                        // Nombre del campo del formulario HTML donde va el usuario.
                        .usernameParameter("username")

                        // Nombre del campo del formulario HTML donde va la contraseña.
                        .passwordParameter("password")

                        // Si el login es exitoso, siempre redirige al home ("/").
                        // El segundo parámetro "true" fuerza esta redirección
                        // incluso si el usuario intentaba acceder a otra página.
                        .defaultSuccessUrl("/", true)

                        // Si las credenciales son incorrectas, vuelve al login
                        // con el parámetro ?error para mostrar el mensaje de fallo.
                        .failureUrl("/login?error")

                        // La página de login en sí es pública (accesible sin sesión).
                        .permitAll()
                )

                // CONFIGURACIÓN DEL LOGOUT
                .logout(logout -> logout
                        // URL que dispara el cierre de sesión (normalmente un botón en el menú).
                        .logoutUrl("/logout")

                        // Después de cerrar sesión, redirige al login con ?logout
                        // para mostrar un mensaje de confirmación.
                        .logoutSuccessUrl("/login?logout")

                        // Invalida la sesión del servidor al hacer logout.
                        .invalidateHttpSession(true)

                        // Elimina la cookie JSESSIONID del navegador,
                        // evitando que se reutilice la sesión anterior.
                        .deleteCookies("JSESSIONID")

                        .permitAll()
                )

                // EXCEPCIONES CSRF
                // Spring Security activa protección CSRF por defecto para formularios.
                // Aquí la desactivamos para estas rutas porque probablemente son
                // llamadas desde JavaScript/AJAX o Postman sin el token CSRF.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/clientes",   "/clientes/**",
                                "/productos",  "/productos/**",
                                "/ventas",     "/ventas/**",
                                "/usuarios",   "/usuarios/**",
                                "/detalles",   "/detalles/**"
                        )
                );

        // Construye y devuelve la cadena de filtros de seguridad configurada.
        return http.build();
    }
}