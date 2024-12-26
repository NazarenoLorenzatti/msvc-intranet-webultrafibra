package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iAplicationDao;
import ar.com.ultrafibra.intranet.dao.iAuxiliarDao;
import ar.com.ultrafibra.intranet.dao.iItemDao;
import ar.com.ultrafibra.intranet.dao.iPaymentDao;
import ar.com.ultrafibra.intranet.entities.Aplication;
import ar.com.ultrafibra.intranet.entities.Auxiliar;
import ar.com.ultrafibra.intranet.entities.Invoice;
import ar.com.ultrafibra.intranet.entities.Item;
import ar.com.ultrafibra.intranet.entities.Payment;
import ar.com.ultrafibra.intranet.service.iPaymentService;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Optional;

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

    private String ofDate = "";
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    @Async
    @Scheduled(cron = "0 */60 * * * *")
    public void getPayments() throws InterruptedException {
        boolean request = true;
//        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("cmpoficiales");
        long offset = 0;
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        lastDate = calendar.getTime();

        do {
            JsonObject jsonResponse = apiGr.getResponse(offset, "recibos", this.format.format(lastDate));
//            JsonObject jsonResponse = apiGr.getResponse(offset, "recibos", "01-12-2024");
            if (jsonResponse != null && jsonResponse.has("recibos")) {
                JsonObject recibos = jsonResponse.getAsJsonObject("recibos");

                // Recorrer los pares clave-valor dentro del JsonObject
                for (Map.Entry<String, JsonElement> entry : recibos.entrySet()) {
                    if (!paymentDao.existsByIdRealSoftware(entry.getKey())) {
                        Payment payment = new Payment();
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();
                        payment.setIdRealSoftware(key);
                        if (value.isJsonObject()) {
                            payment = setPayment(value.getAsJsonObject(), payment);
                            paymentDao.save(payment);
                            System.out.println("INSERT payment = " + payment.getPayment_date() + " " + payment.getClient_name());
                        }

                    } else {
                        System.out.println("PAGO YA CARGADO = ");
                    }
                }
                offset += 100;
            } else {
                request = false;
                break;
            }
        } while (request);

    }

    @Async
    @Scheduled(cron = "0 00 02 * * *", zone = "America/Argentina/Buenos_Aires")
    public void updatePayments() throws InterruptedException {
        boolean request = true;
        long offset = 0;
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        lastDate = calendar.getTime();

        do {
            JsonObject jsonResponse = apiGr.getResponse(offset, "recibos", this.format.format(lastDate));
//            JsonObject jsonResponse = apiGr.getResponse(offset, "recibos", "20-11-2024");
            if (jsonResponse != null && jsonResponse.has("recibos")) {
                JsonObject recibos = jsonResponse.getAsJsonObject("recibos");

                // Recorrer los pares clave-valor dentro del JsonObject
                for (Map.Entry<String, JsonElement> entry : recibos.entrySet()) {
                    Payment payment;
                    String key = entry.getKey();
                    if (paymentDao.existsByIdRealSoftware(entry.getKey())) {
                        payment = paymentDao.findByIdRealSoftware(key);
                        System.out.println("ACTUALIZADO");
                    } else {
                        payment = new Payment();
                        payment.setIdRealSoftware(key);
                        System.out.println("NO ESTABA CARGADO");
                    }
                    JsonElement value = entry.getValue();
                    if (value.isJsonObject()) {
                        payment = setPayment(value.getAsJsonObject(), payment);
                        paymentDao.save(payment);
                        System.out.println("PAGO = " + payment.getPayment_date() + " " + payment.getClient_name());
                    }
                }
                offset += 100;
            } else {
                request = false;
                break;
            }
        } while (request);
    }

    private Payment setPayment(JsonObject subObject, Payment payment) {
        for (Map.Entry<String, JsonElement> element : subObject.entrySet()) {
            switch (element.getKey()) {
                case "fecha_recibo":
                    payment.setPayment_date(element.getValue().getAsString());
                    break;
                case "recaudador":
                    payment.setCollector(element.getValue().getAsString());
                    break;
                case "observaciones":
                    payment.setObservations(element.getValue().getAsString());
                    break;
                case "aplicaciones":
                    JsonObject aplcations = element.getValue().getAsJsonObject();
                    List<Aplication> listAplication = new ArrayList();
                    Aplication aplicationPayment;
                    for (Map.Entry<String, JsonElement> aplicationElement : aplcations.entrySet()) {

                        if (aplicationDao.existsByIdRealSoftware(aplicationElement.getKey())) {
                            Optional<Aplication> o = aplicationDao.findByIdRealSoftware(aplicationElement.getKey());
                            if (o.isPresent()) {
                                aplicationPayment = o.get();
                            } else {
                                continue;
                            }
                        } else {
                            aplicationPayment = new Aplication();
                        }
                        aplicationPayment.setIdRealSoftware(aplicationElement.getKey());
                        for (Map.Entry<String, JsonElement> a : aplicationElement.getValue().getAsJsonObject().entrySet()) {
                            switch (a.getKey()) {
                                case "fecha":
                                    aplicationPayment.setDate_of_aplication(a.getValue().getAsString());
                                    break;
                                case "importe":
                                    aplicationPayment.setAmount(a.getValue().getAsDouble());
                                    break;
                                case "tipo":
                                    if (a.getValue().getAsString().equals("ARX")) {
                                        payment.setCanceled(true);
                                    }
                                    if (a.getValue().getAsString().equals("FX")) {
                                        payment.setDeclarated(false);
                                    }
                                    aplicationPayment.setType(a.getValue().getAsString());
                                    break;
                                case "sucursal":
                                    aplicationPayment.setSales_point(a.getValue().getAsString());
                                    break;
                                case "numero":
                                    aplicationPayment.setNumber_comprobant(a.getValue().getAsString());
                                    break;
                            }
                        }
                        aplicationPayment.setPayment(payment);
                        listAplication.add(aplicationPayment);
                        if (payment.getPayment_id() != null) {
                            aplicationDao.save(aplicationPayment);
                        }
                    }
                    payment.setAplications(listAplication);
                    break;
                case "items":
                    JsonObject item = element.getValue().getAsJsonObject();
                    List<Item> list = new ArrayList();
                    Item itemPayment;
                    for (Map.Entry<String, JsonElement> itemElement : item.entrySet()) {

                        if (itemDao.existsByIdRealSoftware(itemElement.getKey())) {
                            Optional<Item> o = itemDao.findByIdRealSoftware(itemElement.getKey());
                            if (o.isPresent()) {
                                itemPayment = o.get();
                            } else {
                                continue;
                            }
                        } else {
                            itemPayment = new Item();
                        }
                        itemPayment.setIdRealSoftware(itemElement.getKey());
                        for (Map.Entry<String, JsonElement> i : itemElement.getValue().getAsJsonObject().entrySet()) {
                            switch (i.getKey()) {
                                case "destino":
                                    itemPayment.setMeans_of_payment(i.getValue().getAsString());
                                    break;
                                case "importe":
                                    itemPayment.setAmount(i.getValue().getAsDouble());
                                    break;
                                case "tipo":
                                    itemPayment.setType(i.getValue().getAsString());
                                    break;
                            }
                        }
                        itemPayment.setPayment(payment);
                        list.add(itemPayment);
                        if (payment.getPayment_id() != null) {
                            itemDao.save(itemPayment);
                        }
                    }
                    payment.setItems(list);
                    break;
                case "cliente":
                    JsonObject client = element.getValue().getAsJsonObject();
                    for (Map.Entry<String, JsonElement> c : client.entrySet()) {
                        switch (c.getKey()) {
                            case "cliente_id":
                                System.out.println("payment = " + c.getValue().getAsString());
                                payment.setClientIdRealSoftware(c.getValue().getAsString());
                            case "nombre":
                                payment.setClient_name(c.getValue().getAsString());
                                break;
                            case "localidad":
                                payment.setClient_city(c.getValue().getAsJsonObject().get("valor").getAsString());
                                break;
                        }
                    }
                    break;
            }
        }
        return payment;
    }
}