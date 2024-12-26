package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iAuxiliarDao;
import ar.com.ultrafibra.intranet.dao.iInvoiceDao;
import ar.com.ultrafibra.intranet.entities.Invoice;
import ar.com.ultrafibra.intranet.service.iInvoiceService;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    
    @Async
    @Scheduled(cron = "0 */75 * * * *")
    public void updateInvoices() {
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

    public void limpiarDuplicados() {

    }

    public void limpiarTodo() {
        invoiceDao.deleteAll();
    }

}
