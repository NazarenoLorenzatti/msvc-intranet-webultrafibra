package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.*;
import ar.com.ultrafibra.intranet.entities.*;
import ar.com.ultrafibra.intranet.service.iAplicationsHasInvoicesService;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.*;
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

