package com.lavaderosepulveda.app.service;

import com.lavaderosepulveda.app.model.Cita;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String remitente;

    /**
     * Envía un email de confirmación con los detalles de la cita
     *
     * @param cita Información de la cita reservada
     */
    public void enviarEmailConfirmacion(Cita cita) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuración del mensaje
            helper.setFrom(remitente);
            helper.setTo(cita.getEmail());
            helper.setSubject("Confirmación de reserva - Lavadero Sepúlveda");

            // Preparar el contexto para la plantilla
            Context context = new Context(new Locale("es"));
            context.setVariable("cita", cita);
            context.setVariable("fechaFormateada", cita.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("horaFormateada", cita.getHora().format(DateTimeFormatter.ofPattern("HH:mm")));

            // Procesar la plantilla
            String contenido = templateEngine.process("emails/confirmacion-cita", context);
            helper.setText(contenido, true);

            // Enviar el email
            emailSender.send(message);

        } catch (MessagingException e) {
            // Log del error y manejo de excepciones
            System.err.println("Error al enviar email de confirmación: " + e.getMessage());
            // Podría lanzarse una excepción personalizada o registrar en un sistema de logs
        }
    }

    /**
     * Envía un recordatorio de cita un día antes
     * (Este método podría ser invocado por un job programado)
     *
     * @param cita Información de la cita a recordar
     */
    public void enviarRecordatorioCita(Cita cita) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(cita.getEmail());
            helper.setSubject("Recordatorio de cita - Lavadero Sepúlveda");

            Context context = new Context(new Locale("es"));
            context.setVariable("cita", cita);
            context.setVariable("fechaFormateada", cita.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("horaFormateada", cita.getHora().format(DateTimeFormatter.ofPattern("HH:mm")));

            String contenido = templateEngine.process("emails/recordatorio-cita", context);
            helper.setText(contenido, true);

            emailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Error al enviar recordatorio de cita: " + e.getMessage());
        }
    }
}