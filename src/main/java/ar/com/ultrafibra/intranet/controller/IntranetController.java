package ar.com.ultrafibra.intranet.controller;

import ar.com.ultrafibra.intranet.dao.iRolesDao;
import ar.com.ultrafibra.intranet.dao.iUserDao;
import ar.com.ultrafibra.intranet.entities.Role;
import ar.com.ultrafibra.intranet.entities.User;
import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import ar.com.ultrafibra.intranet.responses.UserPortalWebResponseRest;
import ar.com.ultrafibra.intranet.responses.UserResponseRest;
import ar.com.ultrafibra.intranet.service.iAplicationsHasInvoicesService;
import ar.com.ultrafibra.intranet.service.iClientService;
import ar.com.ultrafibra.intranet.service.iInvoiceService;
import ar.com.ultrafibra.intranet.service.iLoginService;
import ar.com.ultrafibra.intranet.service.iPaymentService;
import ar.com.ultrafibra.intranet.service.iServiceOrderService;
import ar.com.ultrafibra.intranet.service.iUserPortalWebService;
import ar.com.ultrafibra.intranet.services.impl.AplicationsHasInvoicesServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.ClientServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.InvoiceServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.LoginServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.PaymentServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.ServiceOrderServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.TicketServiceImpl;
import ar.com.ultrafibra.intranet.services.impl.UserPortalWebServiceImpl;
import com.google.gson.JsonObject;
import jakarta.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {
    "http://localhost:4200",
    "http://localhost:8003",
    "https://119.8.72.246",
    "https://119.8.72.246:8003",
    "https://ultrafibra.com.ar",
    "https://ultrafibra.com.ar:8003",
    "*"})
@RequestMapping("/intranet/restringed")
public class IntranetController {

    @Autowired
    private iClientService clientService;

    @Autowired
    private iPaymentService paymentService;
    
    @Autowired
    private iInvoiceService invoiceService;
    @Autowired
    private iServiceOrderService orderService;

    @Autowired
    private iLoginService loginService;

    @Autowired
    private iAplicationsHasInvoicesService aplicationsHasInvoceService;

    @Autowired
    private iUserDao userDao;

    @Autowired
    private iRolesDao roleDao;

    @Autowired
    private iUserPortalWebService portalWeb;

    @GetMapping(path = "/test")
    @PermitAll
    public void test() throws Exception {

//        clientService.getClients();
//        clientService.updateClients();
            clientService.updateContracts();
//        clientService.setHistoric();
//        aplicationsHasInvoceService.saveAplicacionHasInvoice();
//           ticketService.getTickets();
//        invoiceService.updateInvoices();
//        paymentService.getPayments();
//        paymentService.updatePayments();
//        orderService.getServiceOrders();
//        orderService.updateServiceOrders();

    }

}
