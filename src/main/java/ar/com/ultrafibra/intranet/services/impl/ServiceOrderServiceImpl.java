package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iAuxiliarDao;
import ar.com.ultrafibra.intranet.dao.iClientDao;
import ar.com.ultrafibra.intranet.dao.iServiceOrderDao;
import ar.com.ultrafibra.intranet.entities.Auxiliar;
import ar.com.ultrafibra.intranet.entities.Client;
import ar.com.ultrafibra.intranet.entities.ServiceOrder;
import ar.com.ultrafibra.intranet.entities.Ticket;
import ar.com.ultrafibra.intranet.service.iServiceOrderService;
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
public class ServiceOrderServiceImpl implements iServiceOrderService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iClientDao clientDao;

    @Autowired
    private iServiceOrderDao serviceOrderDao;

    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    @Async
    @Scheduled(cron = "0 */20 * * * *")
    public void getServiceOrders() {
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -4);
        lastDate = calendar.getTime();
        JsonObject jsonResponse = apiGr.getResponseOrderService(this.format.format(lastDate), "m");
//        JsonObject jsonResponse = apiGr.getResponseOrderService("01-11-2024", "m");
        if (jsonResponse != null && !jsonResponse.has("msg")) {
            for (Map.Entry<String, JsonElement> e : jsonResponse.entrySet()) {
                System.out.println("");
                String key = e.getKey();
                JsonObject os = e.getValue().getAsJsonObject();
                ServiceOrder serviceOrder;
                try {
                    Optional<ServiceOrder> o = serviceOrderDao.findByIdRealSoft(key);
                    if (o.isPresent()) {
                        serviceOrder = o.get();
                        System.out.println("YA CREADA = " + serviceOrder.getType());
                    } else {
                        // No se encontró el ServiceOrder, crear uno nuevo
                        serviceOrder = new ServiceOrder();
                        serviceOrder.setIdRealSoft(key);
                        System.out.println("NUEVA");
                    }

                } catch (IncorrectResultSizeDataAccessException ex) {
                    System.out.println("ERROR: Se encontraron múltiples ServiceOrders con idRealSoft = " + key);
                    serviceOrderDao.deleteByIdRealSoft(key);

                    // Crear un nuevo ServiceOrder después de eliminar los duplicados
                    serviceOrder = new ServiceOrder();
                    serviceOrder.setIdRealSoft(key);
                    System.out.println("Se eliminaron duplicados y se creó un nuevo registro.");
                }
                Client client = new Client();
                for (Map.Entry<String, JsonElement> element : os.entrySet()) {
                    serviceOrder = this.setServiceOrder(element, serviceOrder, client);
                }
                serviceOrder.setRegisterDate(new Date());
                serviceOrderDao.save(serviceOrder);
                System.out.println("serviceOrder = " + serviceOrder.getStatus() + " " + serviceOrder.getCity() + " " + serviceOrder.getAffair());
            }
            System.out.println("\n FIN DE CARGAS DE OS");
        } else {
            System.out.println("FIN DE CARGAS DE OS");
        }

    }

    private ServiceOrder setServiceOrder(Map.Entry<String, JsonElement> entry, ServiceOrder serviceOrder, Client client) {
        switch (entry.getKey()) {
            case "estado":
                serviceOrder.setStatus(entry.getValue().getAsString());
                break;
            case "tipo":
                serviceOrder.setType(entry.getValue().getAsString());
                break;
            case "creado":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setCreate_date(entry.getValue().getAsString());
                } else {
                    serviceOrder.setCreate_date("Sin confirmar");
                }
                break;
            case "creado_por":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setCreate_by(entry.getValue().getAsString());
                } else {
                    serviceOrder.setCreate_by("Sin confirmar");
                }
                break;
            case "confirmada_en":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setConfirmed_date(entry.getValue().getAsString());
                } else {
                    serviceOrder.setConfirmed_date("Sin confirmar");
                }
                break;
            case "confirmada_por":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setConfirmed_by(entry.getValue().getAsString());
                } else {
                    serviceOrder.setConfirmed_by("Sin confirmar");
                }
                break;
            case "confeccionada_por":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setMade_by(entry.getValue().getAsString());
                } else {
                    serviceOrder.setMade_by("Sin Terminar");
                }
                break;
            case "cerrada":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setClosed_date(entry.getValue().getAsString());
                } else {
                    serviceOrder.setClosed_date("Sin Cerrar");
                }
                break;
            case "cerrada_por":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setClosed_by(entry.getValue().getAsString());
                } else {
                    serviceOrder.setClosed_by("Sin Cerrar");
                }
                break;
            case "domicilio":
                if (entry.getValue().getAsJsonObject().get("barrio").getAsJsonObject().has("value")) {
                    serviceOrder.setBarrio(entry.getValue().getAsJsonObject().get("barrio").getAsJsonObject().get("value").getAsString());
                } else {
                    serviceOrder.setBarrio("Sin Barrio Establecido");
                }
                break;

            case "cliente":
                if (!entry.getValue().isJsonNull()) {
                    if (clientDao.existsByIdRealSoft(entry.getValue().getAsLong())) {
                        Optional<Client> clientO = clientDao.findByIdRealSoft(entry.getValue().getAsLong());
                        if (clientO.isPresent()) {
                            client = clientO.get();
                            serviceOrder.setCity(client.getCity());
                            serviceOrder.setClient(client);
                        }
                    }
                }
                break;
            case "reclamos":
                if (!entry.getValue().isJsonNull()) {
                    for (Map.Entry<String, JsonElement> element : entry.getValue().getAsJsonObject().entrySet()) {
                        if (element.getValue().getAsJsonObject().has("asunto")) {
                            serviceOrder.setAffair(this.setCaso(element.getValue().getAsJsonObject().get("asunto").getAsString()));
                            serviceOrder.setIdTicket(element.getValue().getAsJsonObject().get("id").getAsString());
                        } else {
                            serviceOrder.setAffair("Sin Caso asociado");
                        }
                    }

                    break;
                } else {
                    System.out.println("RECLAMO NULLLLLLLLL");
                    serviceOrder.setAffair("Sin Caso asociado");
                }
                break;
            case "grupo_id":
                if (!entry.getValue().isJsonNull()) {
                    serviceOrder.setAssigned(this.setGroup(entry.getValue().getAsString()));
                } else {
                    serviceOrder.setAssigned("SIN GRUPO ASIGNADO");
                }
                break;
        }
        return serviceOrder;
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

    private String setCaso(String idCaso) {
        String ret = "";
        switch (idCaso) {
            case "1" ->
                ret = "INSTALACIONES DOMICILIARIAS";
            case "6" ->
                ret = "SIN SERVICIO";
            case "7" ->
                ret = "PRUEBA TIPO DE VALE";
            case "8" ->
                ret = "CAMBIO DE BAJADA";
            case "9" ->
                ret = "AGREGA INTERNET";
            case "10" ->
                ret = "DERIVACION";
            case "11" ->
                ret = "TRASLADO";
            case "12" ->
                ret = "AGREGA TV";
            case "13" ->
                ret = "MALA IMAGEN";
            case "14" ->
                ret = "MOVER ONU";
            case "15" ->
                ret = "RECONEXION";
            case "16" ->
                ret = "SERVICIO CON CORTES";
            case "17" ->
                ret = "SIN ALCANCE";
            case "18" ->
                ret = "CAMBIO DE EQUIPO";
            case "19" ->
                ret = "CONVERSION";
            case "20" ->
                ret = "DESCONEXION";
            case "21" ->
                ret = "OTROS SERVICIOS";
            case "22" ->
                ret = "SINTONIZAR";
            case "24" ->
                ret = "DEVOLUCION DE EQUIPO";
            case "25" ->
                ret = "CONEXIÓN COMBO";
            case "26" ->
                ret = "CONEXIÓN TV";
            case "28" ->
                ret = "CAMBIO DE CONTRASEÑA";
            case "29" ->
                ret = "RETIRO DE EQUIPO";
            case "30" ->
                ret = "TELEVISION";
            case "31" ->
                ret = "DERIVACION A CABLEAR";
            case "32" ->
                ret = "DERIVACION CABLEADO EXISTENTE";
            case "33" ->
                ret = "ASOCIAR ONT";
            case "34" ->
                ret = "INSTALACION SIN CARGO";
            case "35" ->
                ret = "CORPORATIVO";
            case "36" ->
                ret = "INSTALACIÓN CORPORATIVO";
            case "38" ->
                ret = "INTERVENCIÓN TÉCNICA POR CAMBIO DE PLAN";
            case "40" ->
                ret = "INSPECCIÓN PARA RECUPERO Y TRASLADO";
            case "41" ->
                ret = "INSTALACIÓN POR RECUPERO";
            case "49" ->
                ret = "INSPECCIÓN PARA RECUPERO";
            case "50" ->
                ret = "INSPECCIÓN PARA TRASLADO";
            case "68" ->
                ret = "RECUPERO DE CLIENTES";
            case "76" ->
                ret = "RECONEXIÓN INALÁMBRICO";

        }
        return ret;
    }

    public void eliminarTodo() {
        serviceOrderDao.deleteAll();
    }
}
