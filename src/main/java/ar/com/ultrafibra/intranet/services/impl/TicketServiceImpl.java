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
    private static final int DAYS_TO_SUBTRACT = -4;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    @Async
//    @Scheduled(cron = "0 */20 * * * *")
    public void getTickets() {
        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("casos");
        long offset = auxiliar.getValue();
        Date lastDate = getLastDate(DAYS_TO_SUBTRACT);

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

/*
@Slf4j
@Data
@Service
public class TicketServiceImpl implements iTicketService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iTicketDao ticketDao;

    @Autowired
    private iClientDao clientDao;

    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
 
    @Async*/
//    @Scheduled(cron = "0 */20 * * * *")
/* public void getTickets() {
        boolean request = true;
        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("casos");
        long offset = auxiliar.getValue();

        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -4);
        lastDate = calendar.getTime();
        do {
            JsonObject jsonResponse = apiGr.getResponseTicket(offset, "crea", this.format.format(lastDate));
//            JsonObject jsonResponse = apiGr.TEST(28764, "casos"); // PREUBA ELIMINAR LUEGO
            if (jsonResponse != null && jsonResponse.has("casos")) {
                JsonArray jsonArray = jsonResponse.getAsJsonArray("casos");
                for (JsonElement caso : jsonArray) {
                    if (!caso.getAsJsonObject().get("cliente_id").isJsonNull()) {

                        Optional<Client> optionalClient = clientDao.findByIdRealSoft(caso.getAsJsonObject().get("cliente_id").getAsLong());
                        if (optionalClient.isPresent() && !caso.getAsJsonObject().get("asunto").isJsonNull()) {
                            if (!caso.getAsJsonObject().get("asunto").getAsString().equals("CAMBIO DE ESTADO AUTOMÁTICO") && !caso.getAsJsonObject().get("asunto").getAsString().equals("NOVEDADES DE COBRANZAS")) {
                                Ticket ticket;
                                try {
                                    if (ticketDao.existsByIdRealSoftware(caso.getAsJsonObject().get("id").getAsString())) {
                                        Optional<Ticket> o = ticketDao.findByIdRealSoftware(caso.getAsJsonObject().get("id").getAsString());
                                        if (o.isPresent()) {
                                            ticket = o.get();
                                            System.out.println("TICKET A ACTUALIZAR");
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        ticket = new Ticket();
                                        System.out.println("TICKET NUEVO");
                                    }
                                } catch (IncorrectResultSizeDataAccessException ex) {
                                    System.out.println("ERROR: Se encontraron múltiples TICKETS con idRealSoft");
                                    ticketDao.deleteByIdRealSoftware(caso.getAsJsonObject().get("id").getAsString());
                                    // Crear un nuevo ServiceOrder después de eliminar los duplicados
                                    ticket = new Ticket();
                                    System.out.println("Se eliminaron duplicados y se creó un nuevo registro.");
                                }

                                for (Map.Entry<String, JsonElement> entry : caso.getAsJsonObject().entrySet()) {
                                    switch (entry.getKey()) {
                                        case "id":
                                            ticket.setIdRealSoftware(entry.getValue().getAsString());
                                            break;
                                        case "asunto":
                                            ticket.setTicketType(entry.getValue().getAsString());
                                            break;
                                        case "estado":
                                            ticket.setStatus(entry.getValue().getAsString());
                                            break;
                                        case "fecha_creacion":
                                            ticket.setCreation_date(entry.getValue().getAsString());
                                            break;
                                        case "creado_por":
                                            ticket.setCreate_by(entry.getValue().getAsString());
                                            break;
                                        case "ultima_modif":
                                            ticket.setLast_modification_date(entry.getValue().getAsString());
                                            break;
                                        case "modificado_por":
                                            ticket.setModified_by(entry.getValue().getAsString());
                                            break;
                                        case "asignado_a":
                                            if (!entry.getValue().isJsonNull()) {
                                                ticket.setAssigned(this.setGroup(entry.getValue().getAsString()));
                                            } else {
                                                ticket.setAssigned("SIN GRUPO ASIGNADO");
                                            }
                                            break;
                                        case "cliente_id":
                                            ticket.setClient(optionalClient.get());
                                            break;
                                        case "ordenes_de_servicio":
                                            if (!entry.getValue().getAsJsonArray().isEmpty()
                                                    && !entry.toString().equals("ordenes_de_servicio=[]")
                                                    && entry.getValue().getAsJsonArray().size() != 0) {
                                                ticket.setOs_assigned(true);
                                            } else {
                                                System.out.println(entry.toString());
                                                ticket.setOs_assigned(false);
                                            }
                                            break;
                                    }
                                }

                                ticketDao.save(ticket);
                                System.out.println("CASO CARGADO = " + ticket.getTicketType() + " " + ticket.getStatus());

                            }
                        }
                    }
                }
                offset += 100;
                auxiliar.setValue(offset);
                auxiliarDao.save(auxiliar);

            } else {
                System.out.println("FIN CARGA CASOS");
                request = false;
                auxiliar.setValue(0);
                auxiliarDao.save(auxiliar);
                break;
            }
        } while (request);
    }

    private String setGroup(String idGroup) {
        String ret = "";
        switch (idGroup) {
            case "455" ->
                ret = "Comercial";
            case "463" ->
                ret = "Soporte";
            case "465" ->
                ret = "Tecnico";
            case "466" ->
                ret = "Pool de Instalaciones pendiente";
            case "467" ->
                ret = "Contabilidad";
            case "555" ->
                ret = "CUADRILLA 1";
            case "556" ->
                ret = "CUADRILLA 2";
            case "557" ->
                ret = "Administracion - Descuentos y Boniicacione";
            case "572" ->
                ret = "CASO CON ORDEN DE SERVICIO ASIGNADA";
            case "586" ->
                ret = "A RECORDINAR";
            case "591" ->
                ret = "Problemas de IP";
            case "601" ->
                ret = "Administracion";
            case "623" ->
                ret = "CUADRILLA 3";
            case "624" ->
                ret = "CUADRILLA 4";
        }
        return ret;
    }

    public void actualizarTicketManualmente() {
        boolean request = true;
        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("casos");
        long offset = auxiliar.getValue();

        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -4);
        lastDate = calendar.getTime();
        do {
            JsonObject jsonResponse = apiGr.getResponseTicket(offset, "crea", "01-08-2024");
//            JsonObject jsonResponse = apiGr.TEST(28764, "casos"); // PREUBA ELIMINAR LUEGO
            if (jsonResponse != null && jsonResponse.has("casos")) {
                JsonArray jsonArray = jsonResponse.getAsJsonArray("casos");
                for (JsonElement caso : jsonArray) {
                    if (!caso.getAsJsonObject().get("cliente_id").isJsonNull()) {
                        Optional<Client> optionalClient = clientDao.findByIdRealSoft(caso.getAsJsonObject().get("cliente_id").getAsLong());
                        if (optionalClient.isPresent() && !caso.getAsJsonObject().get("asunto").isJsonNull()) {
                            if (!caso.getAsJsonObject().get("asunto").getAsString().equals("CAMBIO DE ESTADO AUTOMÁTICO") && !caso.getAsJsonObject().get("asunto").getAsString().equals("NOVEDADES DE COBRANZAS")) {
                                Ticket ticket;
                                if (ticketDao.existsByIdRealSoftware(caso.getAsJsonObject().get("id").getAsString())) {
                                    Optional<Ticket> o = ticketDao.findByIdRealSoftware(caso.getAsJsonObject().get("id").getAsString());
                                    if (o.isPresent()) {
                                        ticket = o.get();
                                        System.out.println("TICKET A ACTUALIZAR");
                                    } else {
                                        continue;
                                    }
                                } else {
                                    ticket = new Ticket();
                                    System.out.println("TICKET NUEVO");
                                }

                                for (Map.Entry<String, JsonElement> entry : caso.getAsJsonObject().entrySet()) {
                                    switch (entry.getKey()) {
                                        case "id":
                                            ticket.setIdRealSoftware(entry.getValue().getAsString());
                                            break;
                                        case "asunto":
                                            ticket.setTicketType(entry.getValue().getAsString());
                                            break;
                                        case "estado":
                                            ticket.setStatus(entry.getValue().getAsString());
                                            break;
                                        case "fecha_creacion":
                                            ticket.setCreation_date(entry.getValue().getAsString());
                                            break;
                                        case "creado_por":
                                            ticket.setCreate_by(entry.getValue().getAsString());
                                            break;
                                        case "ultima_modif":
                                            ticket.setLast_modification_date(entry.getValue().getAsString());
                                            break;
                                        case "modificado_por":
                                            ticket.setModified_by(entry.getValue().getAsString());
                                            break;
                                        case "asignado_a":
                                            if (!entry.getValue().isJsonNull()) {
                                                ticket.setAssigned(this.setGroup(entry.getValue().getAsString()));
                                            } else {
                                                ticket.setAssigned("SIN GRUPO ASIGNADO");
                                            }
                                            break;
                                        case "cliente_id":
                                            ticket.setClient(optionalClient.get());
                                            break;
                                        case "ordenes_de_servicio":
                                            if (!entry.getValue().getAsJsonArray().isEmpty()
                                                    && !entry.toString().equals("ordenes_de_servicio=[]")
                                                    && entry.getValue().getAsJsonArray().size() != 0) {
                                                ticket.setOs_assigned(true);
                                            } else {
                                                System.out.println(entry.toString());
                                                ticket.setOs_assigned(false);
                                            }
                                            break;
                                    }
                                }
                                ticketDao.save(ticket);
                                System.out.println("CASO CARGADO = " + ticket.getTicketType() + " " + ticket.getStatus());
                            }
                        }
                    }
                }
                offset += 100;
                auxiliar.setValue(offset);
                auxiliarDao.save(auxiliar);

            } else {
                System.out.println("FIN CARGA CASOS");
                request = false;
                auxiliar.setValue(0);
                auxiliarDao.save(auxiliar);
                break;
            }
        } while (request);
    }

}*/
