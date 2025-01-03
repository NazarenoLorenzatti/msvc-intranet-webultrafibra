package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.iAplicationsHasInvoicesDao;
import ar.com.ultrafibra.intranet.dao.iInvoiceDao;
import ar.com.ultrafibra.intranet.dao.iPaymentDao;
import ar.com.ultrafibra.intranet.entities.Aplication;
import ar.com.ultrafibra.intranet.entities.AplicationsHasInvoices;
import ar.com.ultrafibra.intranet.entities.Invoice;
import ar.com.ultrafibra.intranet.entities.Payment;
import ar.com.ultrafibra.intranet.service.iAplicationsHasInvoicesService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AplicationsHasInvoicesServiceImpl implements iAplicationsHasInvoicesService {

    @Autowired
    private iAplicationsHasInvoicesDao aplicationsHasInvoiceDao;

    @Autowired
    private iInvoiceDao invoiceDao;

    @Autowired
    private iPaymentDao paymentDao;

    private static final String DATE_FORMAT = "MM-yyyy";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(DATE_FORMAT);

    @Async
    @Override
    @Scheduled(cron = "0 00 04 * * *", zone = "America/Argentina/Buenos_Aires")
    public void saveAplicacionHasInvoice() {
        List<Payment> payments = fetchPaymentsForCurrentMonth();

        if (payments.isEmpty()) {
            log.info("No se encontraron Pagos realizados en el mes Corriente");
            return;
        }

        for (Payment payment : payments) {
            processPayment(payment);
        }

        log.info("Finalizada la asociacion de Pagos y Facturas.");
    }

    private List<Payment> fetchPaymentsForCurrentMonth() {
        String currentMonth = FORMAT.format(new Date());
        return paymentDao.findByMonthYear(currentMonth);
    }

    private void processPayment(Payment payment) {
        if (payment.getAplications().isEmpty()) {
            log.info("No se encontro ninguna aplicacion para el pago con ID: {}", payment.getIdRealSoftware());
            return;
        }

        for (Aplication application : payment.getAplications()) {
            processApplication(payment, application);
        }
    }

    private void processApplication(Payment payment, Aplication application) {
        AplicationsHasInvoices aplicationHasInvoice = findOrCreateAplicationsHasInvoice(payment);

        mapApplicationToEntity(application, payment, aplicationHasInvoice);
        mapInvoiceToEntity(application, aplicationHasInvoice);

        aplicationsHasInvoiceDao.save(aplicationHasInvoice);
        log.info("Se guardo la aplicacion al comprobante Numero: {}", aplicationHasInvoice.getNumber_comprobant());
    }

    private AplicationsHasInvoices findOrCreateAplicationsHasInvoice(Payment payment) {
        return aplicationsHasInvoiceDao.findByIdRealSoftPayment(payment.getIdRealSoftware())
                .orElseGet(AplicationsHasInvoices::new);
    }

    private void mapApplicationToEntity(Aplication application, Payment payment, AplicationsHasInvoices aplicationHasInvoice) {
        aplicationHasInvoice.setAmount(application.getAmount());
        aplicationHasInvoice.setIdRealSoftPayment(payment.getIdRealSoftware());
        aplicationHasInvoice.setSales_point(application.getSales_point());
        aplicationHasInvoice.setType(application.getType());
        aplicationHasInvoice.setApplication_date(application.getDate_of_aplication());
        aplicationHasInvoice.setPayment_date(payment.getPayment_date());
    }

    private void mapInvoiceToEntity(Aplication application, AplicationsHasInvoices aplicationHasInvoice) {
        int salesPointNumber = Integer.parseInt(application.getSales_point());
        long numberInvoice = Long.parseLong(application.getNumber_comprobant());

        Optional<Invoice> optionalInvoice = invoiceDao.findByTypeAndSalesPointAndNumberInvoice(
                application.getType(), String.valueOf(salesPointNumber), numberInvoice);

        if (optionalInvoice.isPresent()) {
            Invoice invoice = optionalInvoice.get();
            aplicationHasInvoice.setIdRealSoftInvoice(invoice.getIdRealSoftware());
            aplicationHasInvoice.setInvoice_date(invoice.getInvoice_date());
            aplicationHasInvoice.setNumber_comprobant(String.valueOf(invoice.getNumberInvoice()));
            aplicationHasInvoice.setApplied(true);
            log.info("Invoice found for application: type={}, salesPoint={}, number={}",
                    application.getType(), salesPointNumber, numberInvoice);
        } else {
            aplicationHasInvoice.setApplied(false);
            log.warn("Invoice not found for application: type={}, salesPoint={}, number={}",
                    application.getType(), salesPointNumber, numberInvoice);
        }
    }
}






/*
@Slf4j
@Data
@Service
public class AplicationsHasInvoicesServiceImpl implements iAplicationsHasInvoicesService {

    @Autowired
    private iAplicationsHasInvoicesDao aplicationsHasInvoiceDao;

    @Autowired
    private iInvoiceDao invoiceDao;

    @Autowired
    private iPaymentDao paymentDao;

    private SimpleDateFormat format = new SimpleDateFormat("MM-yyyy");

    @Async
    @Override
    @Scheduled(cron = "0 00 04 * * *", zone = "America/Argentina/Buenos_Aires")
    public void saveAplicacionHasInvoice() {
        Date month = new Date();

        List<Payment> payments = paymentDao.findByMonthYear(this.format.format(month));
        if (!payments.isEmpty()) {
            for (Payment p : payments) {
                if (!p.getAplications().isEmpty()) {
                    for (Aplication a : p.getAplications()) {
                        AplicationsHasInvoices aplicationHasInvoice;
                        if (!aplicationsHasInvoiceDao.existsByIdRealSoftPayment(p.getIdRealSoftware())) {
                            aplicationHasInvoice = new AplicationsHasInvoices();
                        } else {
                            Optional<AplicationsHasInvoices> o = aplicationsHasInvoiceDao.findByIdRealSoftPayment(p.getIdRealSoftware());
                            if (o.isPresent()) {
                                aplicationHasInvoice = o.get();
                            } else {
                                continue;
                            }
                        }
                        aplicationHasInvoice.setAmount(a.getAmount());
                        aplicationHasInvoice.setIdRealSoftPayment(p.getIdRealSoftware());
                        aplicationHasInvoice.setSales_point(a.getSales_point());
                        aplicationHasInvoice.setType(a.getType());
                        aplicationHasInvoice.setApplication_date(a.getDate_of_aplication());
                        aplicationHasInvoice.setPayment_date(p.getPayment_date());
                        
                        int salesPointNumber = Integer.valueOf(a.getSales_point());
                        long numberInvoice = Long.valueOf(a.getNumber_comprobant());
                        Optional<Invoice> optionalInvoice = invoiceDao.findByTypeAndSalesPointAndNumberInvoice(a.getType(), String.valueOf(salesPointNumber), numberInvoice);
                        System.out.println(a.getType() + String.valueOf(salesPointNumber) + numberInvoice);
                        if (optionalInvoice.isPresent()) {
                            System.out.println("FACTURA ENCONTRADA \n");
                            aplicationHasInvoice.setIdRealSoftInvoice(optionalInvoice.get().getIdRealSoftware());
                            aplicationHasInvoice.setInvoice_date(optionalInvoice.get().getInvoice_date());
                            aplicationHasInvoice.setNumber_comprobant(String.valueOf(optionalInvoice.get().getNumberInvoice()));
                            aplicationHasInvoice.setApplied(true);
                        } else {
                            aplicationHasInvoice.setApplied(false);
                            System.out.println("No SE ENCONTRO \n");
                        }
                        aplicationsHasInvoiceDao.save(aplicationHasInvoice);
                        System.out.println("Guardado " + aplicationHasInvoice.getNumber_comprobant());

                    }
                }
            }
            
            System.out.println("TERMINADOOOOO");
        }

    }

}*/
