package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.*;
import ar.com.ultrafibra.intranet.entities.Invoice;
import ar.com.ultrafibra.intranet.service.iInvoiceService;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.*;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
public class InvoiceServiceImpl implements iInvoiceService {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final int OFFSET_INCREMENT = 100;
    private static final int DAYS_TO_SUBTRACT = -4;

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iInvoiceDao invoiceDao;

    private final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

    @Async
  //@Scheduled(cron = "0 */75 * * * *")
    public void updateInvoices() {
        long offset = 0;
        Date lastDate = calculateLastDate(DAYS_TO_SUBTRACT);
        boolean hasMoreInvoices;

     
        do {
            JsonObject jsonResponse = apiGr.getResponseInvoice(offset, format.format(lastDate));
            hasMoreInvoices = processInvoicesResponse(jsonResponse);
            offset += OFFSET_INCREMENT;
        } while (hasMoreInvoices);
    }

    private Date calculateLastDate(int daysToSubtract) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToSubtract);
        return calendar.getTime();
    }

    private boolean processInvoicesResponse(JsonObject jsonResponse) {
        if (jsonResponse == null || !jsonResponse.has("cmpoficiales")) {
            return false;
        }

        JsonObject cmpOficiales = jsonResponse.getAsJsonObject("cmpoficiales");
        for (Map.Entry<String, JsonElement> entry : cmpOficiales.entrySet()) {
            processInvoiceEntry(entry);
        }
        return true;
    }

    private void processInvoiceEntry(Map.Entry<String, JsonElement> entry) {
        String invoiceId = entry.getKey();
        Optional<Invoice> existingInvoice = invoiceDao.findByIdRealSoftware(invoiceId);

        if (existingInvoice.isPresent()) {
            log.info("Factura ya cargada: {} {}", existingInvoice.get().getClient_name(), existingInvoice.get().getNumberInvoice());
            return;
        }

        Invoice invoice = buildInvoice(entry);
        invoiceDao.save(invoice);
        log.info("Nueva factura registrada: {}", invoice);
    }

    private Invoice buildInvoice(Map.Entry<String, JsonElement> entry) {
        Invoice invoice = new Invoice();
        invoice.setIdRealSoftware(entry.getKey());

        JsonObject invoiceDetails = entry.getValue().getAsJsonObject();
        for (Map.Entry<String, JsonElement> detail : invoiceDetails.entrySet()) {
            processInvoiceDetail(detail, invoice);
        }

        return invoice;
    }

    private void processInvoiceDetail(Map.Entry<String, JsonElement> detail, Invoice invoice) {
        switch (detail.getKey()) {
            case "numero_comprobante" -> invoice.setNumberInvoice(detail.getValue().getAsLong());
            case "fecha_emision" -> invoice.setInvoice_date(detail.getValue().getAsString());
            case "fecha_presentacion" -> invoice.setOficilization_date(detail.getValue().getAsString());
            case "fecha_servicio_pago" -> invoice.setExpiration_date(detail.getValue().getAsString());
            case "sucursal" -> invoice.setSalesPoint(detail.getValue().getAsString());
            case "importes" -> processInvoiceAmounts(detail.getValue().getAsJsonObject(), invoice);
            case "tipo_comprobante" -> processInvoiceType(detail.getValue().getAsJsonObject(), invoice);
            case "items" -> processInvoiceItems(detail.getValue().getAsJsonObject(), invoice);
            case "cliente" -> processInvoiceClient(detail.getValue().getAsJsonObject(), invoice);
        }
    }

    private void processInvoiceAmounts(JsonObject amounts, Invoice invoice) {
        amounts.entrySet().forEach(amount -> {
            switch (amount.getKey()) {
                case "total":
                    invoice.setMount_iva(amount.getValue().getAsDouble());
                    break;
                case "gravado":
                    invoice.setMount(amount.getValue().getAsDouble());
                    break;
            }
        });
    }

    private void processInvoiceType(JsonObject type, Invoice invoice) {
        if (type.has("valor")) {
            invoice.setType(type.get("valor").getAsString());
        }
    }

    private void processInvoiceItems(JsonObject items, Invoice invoice) {
        items.entrySet().forEach(item -> {
            item.getValue().getAsJsonObject().entrySet().forEach(detail -> {
                if (!detail.getValue().isJsonObject()) {
                    String description = detail.getValue().getAsString();
                    if (description.contains("Débito")) {
                        invoice.setInDebit(true);
                        invoice.setValue_debit(description.contains("10%") ? "10%" : "5%");
                    } else if (!description.matches(".*(Dto|Mora|Redondeo|IP|Simetría|Por unica vez|Up Grade).*")) {
                        invoice.setProductType(description);
                    }
                }
            });
        });
    }

    private void processInvoiceClient(JsonObject client, Invoice invoice) {
        client.entrySet().forEach(c -> {
            switch (c.getKey()) {
                case "nombre" -> invoice.setClient_name(c.getValue().getAsString());
                case "tipo_iva" -> invoice.setClient_type(c.getValue().getAsJsonObject().get("valor").getAsString());
                case "localidad" -> invoice.setCity(c.getValue().getAsJsonObject().get("valor").getAsString());
            }
        });
    }
}

/*
@Slf4j
@Data
@Service
public class InvoiceServiceImpl implements iInvoiceService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iInvoiceDao invoiceDao;

    private String ofDate = "";
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    
    @Async*/
//    @Scheduled(cron = "0 */75 * * * *")
  /*  public void updateInvoices() {
        boolean request = true;
//        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("cmpoficiales");
        long offset = 0;
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -4);
        lastDate = calendar.getTime();

        do {
            JsonObject jsonResponse = apiGr.getResponseInvoice(offset, this.format.format(lastDate));
            if (jsonResponse != null && jsonResponse.has("cmpoficiales")) {
                JsonObject cmpOficiales = jsonResponse.getAsJsonObject("cmpoficiales");

                // Recorrer los pares clave-valor dentro del JsonObject
                for (Map.Entry<String, JsonElement> entry : cmpOficiales.entrySet()) {
                    Optional<Invoice> inv = invoiceDao.findByIdRealSoftware(entry.getKey());
                    if (!inv.isPresent()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();
                        Invoice invoice = new Invoice();
                        invoice.setIdRealSoftware(key);
                        if (value.isJsonObject()) {
                            JsonObject subObject = value.getAsJsonObject();
                            for (Map.Entry<String, JsonElement> element : subObject.entrySet()) {
                                String keyElement = element.getKey();
                                switch (element.getKey()) {
                                    case "numero_comprobante":
                                        invoice.setNumberInvoice(element.getValue().getAsLong());
                                        break;
                                    case "fecha_emision":
                                        invoice.setInvoice_date(element.getValue().getAsString());
                                        break;
                                    case "fecha_presentacion":
                                        invoice.setOficilization_date(element.getValue().getAsString());
                                        break;
                                    case "fecha_servicio_pago":
                                        invoice.setExpiration_date(element.getValue().getAsString());
                                        break;
                                    case "sucursal":
                                        invoice.setSalesPoint(element.getValue().getAsString());
                                        break;
                                    case "importes":
                                        JsonObject amount = element.getValue().getAsJsonObject();
                                        for (Map.Entry<String, JsonElement> a : amount.entrySet()) {
                                            switch (a.getKey()) {
                                                case "total":
                                                    invoice.setMount_iva(a.getValue().getAsDouble());
                                                    break;
                                                case "gravado":
                                                    if (a.getValue() != null) {
                                                        invoice.setMount(a.getValue().getAsDouble());
                                                    }
                                                    break;
                                            }
                                        }
                                        break;
                                    case "tipo_comprobante":
                                        JsonObject type = element.getValue().getAsJsonObject();
                                        for (Map.Entry<String, JsonElement> t : type.entrySet()) {
                                            switch (t.getKey()) {
                                                case "valor": // TIPO DE FACTURA
                                                    invoice.setType(t.getValue().getAsString());
                                                    break;
                                            }
                                        }
                                        break;
                                    case "items":
                                        JsonObject item = element.getValue().getAsJsonObject();
                                        for (Map.Entry<String, JsonElement> itemElement : item.entrySet()) {
                                            for (Map.Entry<String, JsonElement> i : itemElement.getValue().getAsJsonObject().entrySet()) {
                                                switch (i.getKey()) {
                                                    case "descripcion":
                                                        if (i.getValue().getAsString().contains("Débito") || i.getValue().getAsString().contains("Debito")) {
                                                            invoice.setInDebit(true);
                                                            if (i.getValue().getAsString().contains("10%")) {
                                                                invoice.setValue_debit("10%");
                                                                break;
                                                            } else {
                                                                invoice.setValue_debit("5%");
                                                                break;
                                                            }
                                                        }
                                                        if (!i.getValue().getAsString().contains("Dto") && !i.getValue().getAsString().contains("Mora") 
                                                                && !i.getValue().getAsString().contains("Redondeo") && !i.getValue().getAsString().contains("IP") 
                                                                && !i.getValue().getAsString().contains("Simetría") && !i.getValue().getAsString().contains("Por unica vez")
                                                                && !i.getValue().getAsString().contains("Up Grade")) {
                                                            invoice.setProductType(i.getValue().getAsString());
                                                            break;
                                                        }

                                                }
                                            }
                                        }
                                        break;
                                    case "cliente":
                                        JsonObject client = element.getValue().getAsJsonObject();
                                        for (Map.Entry<String, JsonElement> c : client.entrySet()) {
                                            switch (c.getKey()) {
                                                case "nombre":
                                                    invoice.setClient_name(c.getValue().getAsString());
                                                    break;
                                                case "tipo_iva":
                                                    invoice.setClient_type(c.getValue().getAsJsonObject().get("valor").getAsString());
                                                    break;
                                                case "localidad":
                                                    invoice.setCity(c.getValue().getAsJsonObject().get("valor").getAsString());
                                                    break;
                                            }
                                        }
                                        break;
                                }
                            }
                            invoiceDao.save(invoice);
                            System.out.println("invoice = " + invoice.toString());
                        }
                    } else {
                        System.out.println("FACTURA YA CARGADA " + inv.get().getClient_name() + " " + inv.get().getNumberInvoice());

                    }
                }
                offset += 100;
            } else {
                request = false;
                break;
            }
        } while (request);

    }

}
*/
