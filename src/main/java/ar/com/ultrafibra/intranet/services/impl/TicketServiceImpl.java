package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.*;
import ar.com.ultrafibra.intranet.entities.*;
import ar.com.ultrafibra.intranet.service.iTicketService;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.*;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
public class TicketServiceImpl implements iTicketService {

    @Autowired
    private RequestsApiGestionReal apiGestionReal;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iTicketDao ticketDao;

    @Autowired
    private iClientDao clientDao;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final int DAYS_TO_SUBTRACT = -3;
    private static final String TYPE_DATE_M = "modi";
    private static final String TYPE_DATE_C = "crea";
    private String typeDate = TYPE_DATE_C;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    @Async
    @Scheduled(cron = "0 */20 * * * *")
    @Override
    public void getTickets() {
        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("casos");
        long offset = auxiliar.getValue();
        Date lastDate = getLastDate(DAYS_TO_SUBTRACT);
        this.toggleTypeDate();
        while (true) {
            JsonObject response = apiGestionReal.getResponseTicket(offset, "crea", dateFormat.format(lastDate));

            if (response == null || !response.has("casos")) {
                handleNoMoreTickets(auxiliar);
                break;
            }

            JsonArray tickets = response.getAsJsonArray("casos");
            processTickets(tickets);

            offset += 100;
            auxiliar.setValue(offset);
            auxiliarDao.save(auxiliar);
        }
    }
    
        private void toggleTypeDate() {
        typeDate = TYPE_DATE_M.equals(typeDate) ? TYPE_DATE_C : TYPE_DATE_M;
    }

    private void processTickets(JsonArray tickets) {
        for (JsonElement ticketElement : tickets) {
            JsonObject ticketJson = ticketElement.getAsJsonObject();

            if (isValidTicket(ticketJson)) {
                Optional<Client> optionalClient = clientDao.findByIdRealSoft(ticketJson.get("cliente_id").getAsLong());
                optionalClient.ifPresent(client -> processTicket(ticketJson, client));
            }
        }
    }

    private void processTicket(JsonObject ticketJson, Client client) {
        if (isIgnorableTicket(ticketJson)) {
            return;
        }

        Ticket ticket = getOrCreateTicket(ticketJson);
        mapTicketFields(ticketJson, ticket, client);
        ticketDao.save(ticket);

        log.info("Ticket saved: {} - {}", ticket.getTicketType(), ticket.getStatus());
    }

    private Ticket getOrCreateTicket(JsonObject ticketJson) {
        String ticketId = ticketJson.get("id").getAsString();

        try {
            return ticketDao.findByIdRealSoftware(ticketId)
                    .orElseGet(() -> {
                        log.info("Creating new ticket");
                        return new Ticket();
                    });
        } catch (IncorrectResultSizeDataAccessException ex) {
            log.error("Duplicate tickets found for idRealSoftware: {}. Deleting duplicates.", ticketId);
            ticketDao.deleteByIdRealSoftware(ticketId);
            return new Ticket();
        }
    }

    private void mapTicketFields(JsonObject ticketJson, Ticket ticket, Client client) {
        ticket.setIdRealSoftware(ticketJson.get("id").getAsString());
        ticket.setTicketType(ticketJson.get("asunto").getAsString());
        ticket.setStatus(ticketJson.get("estado").getAsString());
        ticket.setCreation_date(ticketJson.get("fecha_creacion").getAsString());
        ticket.setCreate_by(ticketJson.get("creado_por").getAsString());
        ticket.setLast_modification_date(ticketJson.get("ultima_modif").getAsString());
        ticket.setModified_by(ticketJson.get("modificado_por").getAsString());
        ticket.setAssigned(getGroup(ticketJson.get("asignado_a")));
        ticket.setClient(client);
        ticket.setOs_assigned(isOsAssigned(ticketJson));
    }

    private boolean isValidTicket(JsonObject ticketJson) {
        return !ticketJson.get("cliente_id").isJsonNull();
    }

    private boolean isIgnorableTicket(JsonObject ticketJson) {
        String asunto = ticketJson.get("asunto").getAsString();
        return "CAMBIO DE ESTADO AUTOMÁTICO".equals(asunto) || "NOVEDADES DE COBRANZAS".equals(asunto);
    }

    private boolean isOsAssigned(JsonObject ticketJson) {
        JsonArray osArray = ticketJson.getAsJsonArray("ordenes_de_servicio");
        return osArray != null && !osArray.isEmpty();
    }

    private String getGroup(JsonElement groupIdElement) {
        if (groupIdElement == null || groupIdElement.isJsonNull()) {
            return "SIN GRUPO ASIGNADO";
        }

        return switch (groupIdElement.getAsString()) {
            case "455" -> "Comercial";
            case "463" -> "Soporte";
            case "465" -> "Tecnico";
            case "466" -> "Pool de Instalaciones pendiente";
            case "467" -> "Contabilidad";
            case "555" -> "CUADRILLA 1";
            case "556" -> "CUADRILLA 2";
            case "557" -> "Administracion - Descuentos y Bonificaciones";
            case "572" -> "CASO CON ORDEN DE SERVICIO ASIGNADA";
            case "586" -> "A RECORDAR";
            case "591" -> "Problemas de IP";
            case "601" -> "Administracion";
            case "623" -> "CUADRILLA 3";
            case "624" -> "CUADRILLA 4";
            default -> "SIN GRUPO ASIGNADO";
        };
    }

    private Date getLastDate(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        return calendar.getTime();
    }

    private void handleNoMoreTickets(Auxiliar auxiliar) {
        log.info("No more tickets to process.");
        auxiliar.setValue(0);
        auxiliarDao.save(auxiliar);
    }

}
