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
    @Scheduled(cron = "0 */75 * * * *")
    @Override
    public void updateInvoices() {
        long offset = 0;
        Date lastDate = calculateLastDate(DAYS_TO_SUBTRACT);
        boolean hasMoreInvoices;

        do {
            JsonObject jsonResponse = apiGr.getResponseCommon(offset, "cmpoficiales", format.format(lastDate));
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
            case "numero_comprobante" ->
                invoice.setNumberInvoice(detail.getValue().getAsLong());
            case "fecha_emision" ->
                invoice.setInvoice_date(detail.getValue().getAsString());
            case "fecha_presentacion" ->
                invoice.setOficilization_date(detail.getValue().getAsString());
            case "fecha_servicio_pago" ->
                invoice.setExpiration_date(detail.getValue().getAsString());
            case "sucursal" ->
                invoice.setSalesPoint(detail.getValue().getAsString());
            case "importes" ->
                processInvoiceAmounts(detail.getValue().getAsJsonObject(), invoice);
            case "tipo_comprobante" ->
                processInvoiceType(detail.getValue().getAsJsonObject(), invoice);
            case "items" ->
                processInvoiceItems(detail.getValue().getAsJsonObject(), invoice);
            case "cliente" ->
                processInvoiceClient(detail.getValue().getAsJsonObject(), invoice);
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
                case "nombre" ->
                    invoice.setClient_name(c.getValue().getAsString());
                case "tipo_iva" ->
                    invoice.setClient_type(c.getValue().getAsJsonObject().get("valor").getAsString());
                case "localidad" ->
                    invoice.setCity(c.getValue().getAsJsonObject().get("valor").getAsString());
            }
        });
    }
}
