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

}
