package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.*;
import ar.com.ultrafibra.intranet.entities.*;
import ar.com.ultrafibra.intranet.service.iPaymentService;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.*;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;
import java.util.Calendar;

@Slf4j
@Data
@Service
public class PaymentServiceImpl implements iPaymentService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iPaymentDao paymentDao;

    @Autowired
    private iAplicationDao aplicationDao;

    @Autowired
    private iItemDao itemDao;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final int DAYS_OFFSET = -8;
    private static final int ITEMS_BATCH_SIZE = 100;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    @Async
    @Scheduled(cron = "0 */60 * * * *")
    @Override
    public void getPayments() {
        processPayments(new Date(), DAYS_OFFSET);
    }

    @Async
    @Scheduled(cron = "0 00 02 * * *", zone = "America/Argentina/Buenos_Aires")
    @Override
    public void updatePayments() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        processPayments(calendar.getTime(), 0);
    }

    private void processPayments(Date startDate, int offsetDays) {
        boolean hasMoreData;
        long offset = 0;
        Date processingDate = adjustDate(startDate, offsetDays);

        do {
            JsonObject jsonResponse = apiGr.getResponseCommon(offset, "recibos", dateFormatter.format(processingDate));
            hasMoreData = handlePaymentsResponse(jsonResponse);
            offset += ITEMS_BATCH_SIZE;
        } while (hasMoreData);
        log.info("FINALIZADO Se Cargaron "+ offset + " Recibos ");
    }

    private Date adjustDate(Date date, int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        return calendar.getTime();
    }

    private boolean handlePaymentsResponse(JsonObject jsonResponse) {
        if (jsonResponse == null || !jsonResponse.has("recibos")) {
            return false;
        }

        JsonObject recibos = jsonResponse.getAsJsonObject("recibos");
        for (Map.Entry<String, JsonElement> entry : recibos.entrySet()) {
            processPayment(entry.getKey(), entry.getValue());
        }

        return true;
    }

    private void processPayment(String id, JsonElement value) {
        Payment payment = paymentDao.existsByIdRealSoftware(id)
                ? paymentDao.findByIdRealSoftware(id)
                : new Payment();

        payment.setIdRealSoftware(id);

        if (value.isJsonObject()) {
            setPaymentDetails(value.getAsJsonObject(), payment);
            setDeclaredPayment(payment);
            setCanceledPayment(payment);
            paymentDao.save(payment);
            log.info("Processed payment: {}", payment.getClient_name()
                    + " Cancelado? " + payment.isCanceled()
                    + " Declarado? " + payment.isDeclarated()
                    + " Fecha de Pago: " + payment.getPayment_date());
        }
    }

    private void setPaymentDetails(JsonObject details, Payment payment) {
        details.entrySet().forEach(entry -> {
            switch (entry.getKey()) {
                case "fecha_recibo" -> payment.setPayment_date(entry.getValue().getAsString());
                case "recaudador" -> payment.setCollector(entry.getValue().getAsString());
                case "observaciones" -> payment.setObservations(entry.getValue().getAsString());
                case "aplicaciones" -> payment.setAplications(processAplications(entry.getValue().getAsJsonObject(), payment));
                case "items" -> payment.setItems(processItems(entry.getValue().getAsJsonObject(), payment));
                case "cliente" -> processClientDetails(entry.getValue().getAsJsonObject(), payment);
                default -> log.debug("Unhandled payment detail: {}", entry.getKey());
            }
        });
    }

    private List<Aplication> processAplications(JsonObject aplications, Payment payment) {
        List<Aplication> list = new ArrayList<>();

        aplications.entrySet().forEach(entry -> {
            Aplication aplication = aplicationDao.existsByIdRealSoftware(entry.getKey())
                    ? aplicationDao.findByIdRealSoftware(entry.getKey()).orElse(new Aplication())
                    : new Aplication();

            aplication.setIdRealSoftware(entry.getKey());
            populateAplicationDetails(entry.getValue().getAsJsonObject(), aplication);
            aplication.setPayment(payment);

            list.add(aplication);

            if (payment.getPayment_id() != null) {
                aplicationDao.save(aplication);
            }
        });

        return list;
    }

    private void populateAplicationDetails(JsonObject details, Aplication aplication) {
        details.entrySet().forEach(entry -> {
            switch (entry.getKey()) {
                case "fecha" -> aplication.setDate_of_aplication(entry.getValue().getAsString());
                case "importe" -> aplication.setAmount(entry.getValue().getAsDouble());
                case "tipo" -> aplication.setType(entry.getValue().getAsString());
                case "sucursal" -> aplication.setSales_point(entry.getValue().getAsString());
                case "numero" -> aplication.setNumber_comprobant(entry.getValue().getAsString());
            }
        });
    }

    private List<Item> processItems(JsonObject items, Payment payment) {
        List<Item> list = new ArrayList<>();

        items.entrySet().forEach(entry -> {
            Item item = itemDao.existsByIdRealSoftware(entry.getKey())
                    ? itemDao.findByIdRealSoftware(entry.getKey()).orElse(new Item())
                    : new Item();

            item.setIdRealSoftware(entry.getKey());
            populateItemDetails(entry.getValue().getAsJsonObject(), item);
            item.setPayment(payment);

            list.add(item);

            if (payment.getPayment_id() != null) {
                itemDao.save(item);
            }
        });

        return list;
    }

    private void populateItemDetails(JsonObject details, Item item) {
        details.entrySet().forEach(entry -> {
            switch (entry.getKey()) {
                case "destino" -> item.setMeans_of_payment(entry.getValue().getAsString());
                case "importe" -> item.setAmount(entry.getValue().getAsDouble());
                case "tipo" -> item.setType(entry.getValue().getAsString());
            }
        });
    }

    private void processClientDetails(JsonObject client, Payment payment) {
        client.entrySet().forEach(entry -> {
            switch (entry.getKey()) {
                case "cliente_id" -> payment.setClientIdRealSoftware(entry.getValue().getAsString());
                case "nombre" -> payment.setClient_name(entry.getValue().getAsString());
                case "localidad" -> payment.setClient_city(entry.getValue().getAsJsonObject().get("valor").getAsString());
            }
        });
    }

    private void setDeclaredPayment(Payment payment) {
        if (!payment.getItems().isEmpty()) {
            for (Item item : payment.getItems()) {
                if (item.getMeans_of_payment() != null) {
                    if (item.getMeans_of_payment().contains("X")) {
                        payment.setDeclarated(false);
                        break;
                    }
                }
            }
        }
    }

    private void setCanceledPayment(Payment payment) {
        if (!payment.getAplications().isEmpty()) {
            for (Aplication aplication : payment.getAplications()) {
                if (aplication.getType().contains("ARX")) {
                    payment.setCanceled(true);
                    break;
                }
            }
        }
    }
}

