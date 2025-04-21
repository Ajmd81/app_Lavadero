package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Cita;
import com.lavaderosepulveda.app.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    // Crear una nueva cita
    public Cita crearCita(Cita cita) {
        // Validar disponibilidad antes de guardar
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new RuntimeException("La hora seleccionada ya está ocupada. Por favor, elija otro horario.");
        }
        return citaRepository.save(cita);
    }

    // Obtener todas las citas
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll();
    }

    // Obtener cita por ID
    public Optional<Cita> obtenerCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    // Actualizar cita existente
    public Cita actualizarCita(Long id, Cita citaActualizada) {
        return citaRepository.findById(id)
                .map(cita -> {
                    cita.setNombre(citaActualizada.getNombre());
                    cita.setTelefono(citaActualizada.getTelefono());
                    cita.setModeloVehiculo(citaActualizada.getModeloVehiculo());
                    cita.setTipoLavado(citaActualizada.getTipoLavado());

                    // Si cambia la fecha u hora, verificar disponibilidad
                    if (!cita.getFecha().equals(citaActualizada.getFecha()) ||
                            !cita.getHora().equals(citaActualizada.getHora())) {

                        if (citaRepository.existsByFechaAndHora(citaActualizada.getFecha(), citaActualizada.getHora())) {
                            throw new RuntimeException("La hora seleccionada ya está ocupada. Por favor, elija otro horario.");
                        }

                        cita.setFecha(citaActualizada.getFecha());
                        cita.setHora(citaActualizada.getHora());
                    }

                    return citaRepository.save(cita);
                })
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + id));
    }

    // Eliminar cita
    public void eliminarCita(Long id) {
        citaRepository.deleteById(id);
    }

    // Obtener citas por fecha
    public List<Cita> obtenerCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    // Obtener citas de un cliente por teléfono
    public List<Cita> obtenerCitasPorTelefono(String telefono) {
        return citaRepository.findByTelefono(telefono);
    }

    // Obtener horarios disponibles para una fecha específica
    // Obtener horarios disponibles para una fecha específica
    public List<LocalTime> obtenerHorariosDisponibles(LocalDate fecha) {
        // Verificar si es domingo (DayOfWeek.SUNDAY es 7)
        if (fecha.getDayOfWeek().getValue() == 7) { // 7 = domingo
            // El negocio está cerrado los domingos
            System.out.println("Fecha solicitada: " + fecha + " (Domingo) - Negocio cerrado");
            return new ArrayList<>(); // Devolver lista vacía
        }

        List<LocalTime> todosLosHorarios = new ArrayList<>();

        // Verificar si es sábado (DayOfWeek.SATURDAY es 6)
        boolean esSabado = fecha.getDayOfWeek().getValue() == 6; // 6 = sábado

        if (esSabado) {
            // Horario especial para sábados: 9:00 a 13:00
            for (int hora = 9; hora < 14; hora++) {
                todosLosHorarios.add(LocalTime.of(hora, 0));
            }
        } else {
            // Horario regular para otros días
            for (int hora = 8; hora < 15; hora++) {
                // Excluir la hora 14:00 si es para la aplicación Android
                if (hora != 14) {
                    todosLosHorarios.add(LocalTime.of(hora, 0));
                }
            }
            for (int hora = 17; hora < 20; hora++) {
                todosLosHorarios.add(LocalTime.of(hora, 0));
            }
        }

        // Crear una lista para almacenar los horarios ocupados
        List<LocalTime> horariosOcupados = new ArrayList<>();

        // Obtener citas existentes para la fecha
        List<Cita> citasExistentes = citaRepository.findByFecha(fecha);

        // Recopilar todos los horarios ocupados
        for (Cita cita : citasExistentes) {
            // Imprimir información para depuración
            System.out.println("Cita existente: " + cita.getFecha() + " a las " + cita.getHora());
            horariosOcupados.add(cita.getHora());
        }

        // Crear la lista de horarios disponibles
        List<LocalTime> horariosDisponibles = new ArrayList<>();

        // Solo añadir horarios que no estén ocupados
        for (LocalTime horario : todosLosHorarios) {
            boolean ocupado = false;
            for (LocalTime horaOcupada : horariosOcupados) {
                if (horario.getHour() == horaOcupada.getHour() &&
                        horario.getMinute() == horaOcupada.getMinute()) {
                    ocupado = true;
                    break;
                }
            }
            if (!ocupado) {
                horariosDisponibles.add(horario);
            }
        }

        // Imprimir información para depuración
        System.out.println("Fecha solicitada: " + fecha);
        System.out.println("Es sábado: " + esSabado);
        System.out.println("Horarios disponibles: " + horariosDisponibles);

        return horariosDisponibles;
    }

    // Verificar si existe una cita en una fecha y hora específica
    public boolean existeCitaEnFechaHora(LocalDate fecha, LocalTime hora) {
        return citaRepository.existsByFechaAndHora(fecha, hora);
    }
}