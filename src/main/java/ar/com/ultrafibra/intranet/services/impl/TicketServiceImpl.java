package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iAuxiliarDao;
import ar.com.ultrafibra.intranet.dao.iClientDao;
import ar.com.ultrafibra.intranet.dao.iTicketDao;
import ar.com.ultrafibra.intranet.entities.Auxiliar;
import ar.com.ultrafibra.intranet.entities.Client;
import ar.com.ultrafibra.intranet.entities.Ticket;
import ar.com.ultrafibra.intranet.service.iTicketService;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
 
    @Async
    @Scheduled(cron = "0 */20 * * * *")
    public void getTickets() {
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

    /*
    Metodo para actualizar manualmente hasta que quede todo en produccion
     */
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

}
