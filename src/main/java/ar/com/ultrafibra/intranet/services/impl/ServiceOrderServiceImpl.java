package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.*;
import ar.com.ultrafibra.intranet.entities.*;
import ar.com.ultrafibra.intranet.service.iServiceOrderService;
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
public class ServiceOrderServiceImpl implements iServiceOrderService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iClientDao clientDao;

    @Autowired
    private iServiceOrderDao serviceOrderDao;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
//    private static final int OFFSET_INCREMENT = 100;
    private static final int DAYS_TO_SUBTRACT = -4;

    private SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

    private static final Map<String, String> CASE_MAP = Map.ofEntries(
            Map.entry("1", "INSTALACIONES DOMICILIARIAS"),
            Map.entry("6", "SIN SERVICIO"),
            Map.entry("7", "PRUEBA TIPO DE VALE"),
            Map.entry("8", "CAMBIO DE BAJADA"),
            Map.entry("9", "AGREGA INTERNET"),
            Map.entry("10", "DERIVACION"),
            Map.entry("11", "TRASLADO"),
            Map.entry("12", "AGREGA TV"),
            Map.entry("13", "MALA IMAGEN"),
            Map.entry("14", "MOVER ONU"),
            Map.entry("15", "RECONEXION"),
            Map.entry("16", "SERVICIO CON CORTES"),
            Map.entry("17", "SIN ALCANCE"),
            Map.entry("18", "CAMBIO DE EQUIPO"),
            Map.entry("19", "CONVERSION"),
            Map.entry("20", "DESCONEXION"),
            Map.entry("21", "OTROS SERVICIOS"),
            Map.entry("22", "SINTONIZAR"),
            Map.entry("24", "DEVOLUCION DE EQUIPO"),
            Map.entry("25", "CONEXIÓN COMBO"),
            Map.entry("26", "CONEXIÓN TV"),
            Map.entry("28", "CAMBIO DE CONTRASEÑA"),
            Map.entry("29", "RETIRO DE EQUIPO"),
            Map.entry("30", "TELEVISION"),
            Map.entry("31", "DERIVACION A CABLEAR"),
            Map.entry("32", "DERIVACION CABLEADO EXISTENTE"),
            Map.entry("33", "ASOCIAR ONT"),
            Map.entry("34", "INSTALACION SIN CARGO"),
            Map.entry("35", "CORPORATIVO"),
            Map.entry("36", "INSTALACIÓN CORPORATIVO"),
            Map.entry("38", "INTERVENCIÓN TÉCNICA POR CAMBIO DE PLAN"),
            Map.entry("40", "INSPECCIÓN PARA RECUPERO Y TRASLADO"),
            Map.entry("41", "INSTALACIÓN POR RECUPERO"),
            Map.entry("49", "INSPECCIÓN PARA RECUPERO"),
            Map.entry("50", "INSPECCIÓN PARA TRASLADO"),
            Map.entry("68", "RECUPERO DE CLIENTES"),
            Map.entry("76", "RECONEXIÓN INALÁMBRICO")
    );

    private static final Map<String, String> GROUP_MAP = Map.ofEntries(
            Map.entry("455", "Comercial"),
            Map.entry("463", "Soporte"),
            Map.entry("465", "Tecnico"),
            Map.entry("466", "Pool de Instalaciones pendiente"),
            Map.entry("467", "Contabilidad"),
            Map.entry("555", "CUADRILLA 1"),
            Map.entry("556", "CUADRILLA 2"),
            Map.entry("557", "Administracion - Descuentos y Bonificaciones"),
            Map.entry("572", "CASO CON ORDEN DE SERVICIO ASIGNADA"),
            Map.entry("586", "A RECORDINAR"),
            Map.entry("591", "Problemas de IP"),
            Map.entry("601", "Administracion"),
            Map.entry("623", "CUADRILLA 3"),
            Map.entry("624", "CUADRILLA 4")
    );

    @Async
//    @Scheduled(cron = "0 */20 * * * *")
    public void getServiceOrders() {
        long offset = 0;
        Date lastDate = calculateLastDate(DAYS_TO_SUBTRACT);
        JsonObject jsonResponse = apiGr.getResponseOrderService(this.format.format(lastDate), "m");
//        JsonObject jsonResponse = apiGr.getResponseOrderService("01-11-2024", "m");
        this.handleServiceOrderResponse(jsonResponse);
    }

    private void handleServiceOrderResponse(JsonObject jsonResponse) {
        if (jsonResponse != null && !jsonResponse.has("msg")) {
            for (Map.Entry<String, JsonElement> e : jsonResponse.entrySet()) {
                ServiceOrder serviceOrder = this.initServiceOrder(e.getKey());
                JsonObject os = e.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> element : os.entrySet()) {
                    this.setServiceOrder(element, serviceOrder);
                }
                this.saveOrderService(serviceOrder);
            }
        }
        log.info("Terminada la Carga de Ordenes de Servicio");
    }

    private void saveOrderService(ServiceOrder serviceOrder) {
        serviceOrder.setRegisterDate(new Date());
        serviceOrderDao.save(serviceOrder);
        log.info("serviceOrder = " + serviceOrder.getStatus() + " " + serviceOrder.getCity() + " " + serviceOrder.getAffair());
    }

    private ServiceOrder initServiceOrder(String id) {
        ServiceOrder serviceOrder;
        try {
            serviceOrder = serviceOrderDao.existsByIdRealSoft(id)
                    ? serviceOrderDao.findByIdRealSoft(id).get()
                    : new ServiceOrder();
            serviceOrder.setIdRealSoft(id);
        } catch (IncorrectResultSizeDataAccessException ex) {
            log.error("ERROR: Se encontraron múltiples ServiceOrders con idRealSoft = " + id);
            serviceOrder = this.deleteDuplicated(id);
        }
        return serviceOrder;
    }

    //Eliminar Services Order Duplicados y crear de nuevo,
    private ServiceOrder deleteDuplicated(String id) {
        serviceOrderDao.deleteByIdRealSoft(id);
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setIdRealSoft(id);
        return serviceOrder;
    }

    private Date calculateLastDate(int daysToSubtract) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToSubtract);
        return calendar.getTime();
    }

    private void setServiceOrder(Map.Entry<String, JsonElement> entry, ServiceOrder serviceOrder) {
        switch (entry.getKey()) {
            case "estado" ->
                serviceOrder.setStatus(entry.getValue().getAsString());
            case "tipo" ->
                serviceOrder.setType(entry.getValue().getAsString());
            case "creado" ->
                serviceOrder.setCreate_date(entry.getValue().isJsonNull() ? "Si Confirmar" : entry.getValue().getAsString());
            case "creado_por" ->
                serviceOrder.setCreate_by(entry.getValue().isJsonNull() ? "Si Confirmar" : entry.getValue().getAsString());
            case "confirmada_en" ->
                serviceOrder.setConfirmed_date(entry.getValue().isJsonNull() ? "Si Confirmar" : entry.getValue().getAsString());
            case "confirmada_por" ->
                serviceOrder.setConfirmed_by(entry.getValue().isJsonNull() ? "Si Confirmar" : entry.getValue().getAsString());
            case "confeccionada_por" ->
                serviceOrder.setMade_by(entry.getValue().isJsonNull() ? "Sin Terminar" : entry.getValue().getAsString());
            case "cerrada" ->
                serviceOrder.setClosed_date(entry.getValue().isJsonNull() ? "Sin Cerrar" : entry.getValue().getAsString());
            case "cerrada_por" ->
                serviceOrder.setClosed_by(entry.getValue().isJsonNull() ? "Sin Cerrar" : entry.getValue().getAsString());
            case "domicilio" ->
                serviceOrder.setBarrio(entry.getValue().getAsJsonObject().get("barrio").getAsJsonObject().has("value")
                        ? entry.getValue().getAsJsonObject().get("barrio").getAsJsonObject().get("value").getAsString()
                        : "Sin Barrio Establecido");
            case "cliente" ->
                this.setClient(entry, serviceOrder);
            case "reclamos" ->
                this.setReclamos(entry, serviceOrder);
            case "grupo_id" ->
                serviceOrder.setAssigned(this.setGroup(entry));
        }
    }

    private void setClient(Map.Entry<String, JsonElement> entry, ServiceOrder serviceOrder) {
        if (!entry.getValue().isJsonNull()) {
            if (clientDao.existsByIdRealSoft(entry.getValue().getAsLong())) {
                Optional<Client> clientO = clientDao.findByIdRealSoft(entry.getValue().getAsLong());
                if (clientO.isPresent()) {
                    serviceOrder.setCity(clientO.get().getCity());
                    serviceOrder.setClient(clientO.get());
                }
            }
        }
    }

    private void setReclamos(Map.Entry<String, JsonElement> entry, ServiceOrder serviceOrder) {
        if (!entry.getValue().isJsonNull()) {
            for (Map.Entry<String, JsonElement> element : entry.getValue().getAsJsonObject().entrySet()) {
                if (element.getValue().getAsJsonObject().has("asunto")) {
                    serviceOrder.setAffair(this.setCase(element));
                    serviceOrder.setIdTicket(element.getValue().getAsJsonObject().get("id").getAsString());
                } else {
                    serviceOrder.setAffair("Sin Caso asociado");
                }
            }
        } else {
            log.warn("No se encontro un Caso Asociado a la Orden de Servicio");
            serviceOrder.setAffair("Sin Caso asociado");
        }
    }

    private String setGroup(Map.Entry<String, JsonElement> entry) {
        if (!entry.getValue().isJsonNull()) {
            return GROUP_MAP.getOrDefault(entry.getValue().getAsString(), "SIN GRUPO ASIGNADO");
        } else {
            return "SIN GRUPO ASIGNADO";
        }
    }

    private String setCase(Map.Entry<String, JsonElement> element) {
        if (!element.getValue().isJsonNull()) {
            return CASE_MAP.getOrDefault(element.getValue().getAsJsonObject().get("asunto").getAsString(), "SIN CASO ASIGNADO");
        } else {
            return "SIN CASO ASIGNADO";
        }
    }

    public void eliminarTodo() {
        serviceOrderDao.deleteAll();
    }
}
