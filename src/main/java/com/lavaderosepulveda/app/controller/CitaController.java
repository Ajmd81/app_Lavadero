package com.lavaderosepulveda.app.controller;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.model.TipoLavado;
import com.lavaderosepulveda.app.service.CitaService;
import com.lavaderosepulveda.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Controller
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private EmailService emailService;

    // Página principal
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Mostrar formulario para crear una cita
    @GetMapping("/nueva-cita")
    public String mostrarFormulario(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("tiposLavado", TipoLavado.values());
        return "formulario";
    }

    // Procesar el formulario para crear una cita
    @PostMapping("/guardar-cita")
    public String guardarCita(@Valid @ModelAttribute Cita cita, BindingResult bindingResult,
                              Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposLavado", TipoLavado.values());
            return "formulario";
        }

        try {
            Cita citaGuardada = citaService.crearCita(cita);

            // Enviar email de confirmación
            emailService.enviarEmailConfirmacion(citaGuardada);

            redirectAttributes.addFlashAttribute("mensaje", "¡Cita reservada con éxito! Hemos enviado un email de confirmación.");
            redirectAttributes.addFlashAttribute("cita", citaGuardada);
            return "redirect:/confirmacion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/nueva-cita";
        }
    }

    // Página de confirmación
    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "confirmacion";
    }

    // Endpoint para obtener horarios disponibles (usado por AJAX)
    @GetMapping("/horarios-disponibles")
    @ResponseBody
    public List<LocalTime> obtenerHorariosDisponibles(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.obtenerHorariosDisponibles(fecha);
    }

    // Endpoint para ver las citas
    @GetMapping("/admin/listado-citas")
    public String listarTodasLasCitas(Model model) {
        // Obtener todas las citas
        List<Cita> citas = citaService.obtenerTodasLasCitas();

        // Agrupar las citas por fecha y ordenar por hora dentro de cada grupo
        Map<LocalDate, List<Cita>> citasPorFechaDesordenado = citas.stream()
                .collect(Collectors.groupingBy(
                        Cita::getFecha,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(Cita::getHora));
                                    return list;
                                }
                        )
                ));

        // Crear un nuevo mapa ordenado por fecha (de más reciente a más antigua)
        Map<LocalDate, List<Cita>> citasPorFecha = new TreeMap<>(Comparator.reverseOrder());
        citasPorFecha.putAll(citasPorFechaDesordenado);

        // Pasar las citas agrupadas al modelo
        model.addAttribute("citasPorFecha", citasPorFecha);

        return "admin/listado-citas";
    }

    // Endpoint para eliminar las citas
    @GetMapping("/admin/eliminar-cita/{id}")
    public String eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.eliminarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita eliminada con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cita: " + e.getMessage());
        }
        return "redirect:/admin/listado-citas";
    }
}