package ar.com.ultrafibra.intranet.services.impl;

import ar.com.ultrafibra.intranet.dao.*;
import ar.com.ultrafibra.intranet.entities.*;
import ar.com.ultrafibra.intranet.service.*;
import ar.com.ultrafibra.intranet.service.util.RequestsApiGestionReal;
import com.google.gson.*;
import jakarta.transaction.Transactional;
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
public class ClientServiceImpl implements iClientService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iClientDao clientDao;

    @Autowired
    private iContractDao contractDao;

    @Autowired
    private iContractHistoryDao contractHistoryDao;

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TYPE_DATE_M = "m";
    private static final String TYPE_DATE_C = "c";
    private static final String STATUS_INACTIVE = "SIN ESTABLECER";

    private SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
    private String typeDate = TYPE_DATE_M;

    @Async
//    @Scheduled(cron = "0 */15 * * * *")
    public void getClients() {
        processClientData(TYPE_DATE_C, 0);
    }

    @Async
//    @Scheduled(cron = "0 0 */12 * * *")
    public void updateClients() {
        toggleTypeDate();
        processClientData(typeDate, 0);
    }

    @Async
//    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void updateContracts() {
        Date lastDate = calculateLastDateForContracts();
        JsonObject jsonResponse = apiGr.getResponseUpdateContract(format.format(lastDate), TYPE_DATE_M);

        if (jsonResponse != null) {
            JsonArray contracts = jsonResponse.get("contratos").getAsJsonArray();
            if (contracts != null) {
                processContracts(contracts);
            }
        }
    }

    @Async
    // Ejecutar el último día de cada mes a las 00:00
    @Scheduled(cron = "0 0 0 L * ?", zone = "America/Argentina/Buenos_Aires")
    public void setHistoric() {
        List<Contract> contracts = contractDao.findAll();
        for (Contract c : contracts) {
            ContractHistory his = new ContractHistory();
            his.setIdRealSoft(c.getIdRealSoft());
            his.setContract_name(c.getContract_name());
            his.setComming(c.getComming());
            his.setCity(c.getCity());
            his.setLat(c.getLat());
            his.setLon(c.getLon());
            his.setBaja(c.isBaja());
            his.setDate_of_baja(c.getDate_of_baja());
            his.setReason_baja(c.getReason_baja());
            his.setClient(c.getClient());
            his.setPre_invoice(c.getPre_invoice());
            his.setRegistrationDate(new Date());
            his.setPending_installation(c.isPending_installation());
            his.setWallet(c.getClient().getWallet());
            contractHistoryDao.save(his);
            log.info("Historico Guardado = " + his.getContract_name());
        }
    }

    private void toggleTypeDate() {
        typeDate = TYPE_DATE_M.equals(typeDate) ? TYPE_DATE_C : TYPE_DATE_M;
    }

    
    /* PROCESAR IFORMACION DEL CLIENTE */
    private void processClientData(String typeDate, long offset) {
        Date lastDate = calculateLastDate();
        boolean request = true;

        do {
            JsonObject jsonResponse = apiGr.getResponseClient(format.format(lastDate), typeDate, offset);
            if (jsonResponse != null && jsonResponse.has("clientes")) {
                JsonObject clientes = jsonResponse.get("clientes").getAsJsonObject();
                processClients(clientes);
                offset += 100;
            } else {
                request = false;
            }
        } while (request);
    }

    private Date calculateLastDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        return calendar.getTime();
    }

    private void processClients(JsonObject clientes) {
        for (Map.Entry<String, JsonElement> element : clientes.entrySet()) {
            long clientId = Long.valueOf(element.getKey());
            if (!clientDao.existsByIdRealSoft(clientId)) {
                Client client = mapClientData(element);
                List<Contract> contracts = getContracts(client);
                if (!contracts.isEmpty()) {
                    client.setContracts(contracts);
                }
                clientDao.save(client);
                logClient(client);
            } else {
                logClientAlreadyLoaded();
            }
        }
    }

    private Client mapClientData(Map.Entry<String, JsonElement> element) {
        Client client = new Client();
        client.setIdRealSoft(Long.valueOf(element.getKey()));
        JsonObject clientData = element.getValue().getAsJsonObject();

        for (Map.Entry<String, JsonElement> e : clientData.entrySet()) {
            switch (e.getKey()) {
                case "documento" ->
                    client.setDni(e.getValue().getAsString());
                case "nombre" ->
                    client.setName(e.getValue().getAsString());
                case "domicilio" ->
                    mapAddress(client, e.getValue().getAsJsonObject());
                case "estado" ->
                    mapStatus(client, e.getValue().getAsJsonObject());
                case "debito_automatico" ->
                    client.setAutomatic_debt(e.getValue().getAsString().equals("1"));
                case "corporativo" ->
                    client.setCorpo(e.getValue().getAsString().equals("1"));
                case "cartera" ->
                    mapWallet(client, e.getValue());
            }
        }
        return client;
    }

    private void mapAddress(Client client, JsonObject addressData) {
        if (!addressData.get("direccion").isJsonNull()) {
            client.setAddress(addressData.get("direccion").getAsString());
        }
        if (!addressData.get("localidad").isJsonNull()) {
            client.setCity(addressData.get("localidad").getAsJsonObject().get("valor").getAsString());
        }
        if (!addressData.get("barrio").isJsonNull()) {
            client.setDistrict(addressData.get("barrio").getAsJsonObject().get("valor").getAsString());
        }
    }

    private void mapStatus(Client client, JsonObject statusData) {
        if (!statusData.get("valor").isJsonNull()) {
            client.setStatus(statusData.get("valor").getAsString());
        }
    }

    private void mapWallet(Client client, JsonElement walletData) {
        if (walletData.isJsonNull()) {
            client.setWallet(STATUS_INACTIVE);
        } else {
            String walletValue = walletData.getAsJsonObject().get("valor").getAsString();
            client.setWallet(walletValue != null ? walletValue : STATUS_INACTIVE);
        }
    }

    private void logClient(Client client) {
        log.info("Cliente cargado: {} con ID RealSoft {}", client.getName(), client.getIdRealSoft());
    }

    private void logClientAlreadyLoaded() {
        log.info("Cliente ya estaba cargado.");
    }

    
    
    /* PROCESAMIENTO DE INFORMACION DE LOS CONTRATOS */
    private Date calculateLastDateForContracts() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        return calendar.getTime();
    }

    private void processContracts(JsonArray contracts) {
        for (JsonElement element : contracts) {
            processContract(element.getAsJsonObject());
        }
    }

    private void processContract(JsonObject contractData) {
        Optional<Client> clientOptional = clientDao.findByIdRealSoft(contractData.get("cliente_id").getAsLong());
        if (clientOptional.isPresent()) {
            Client client = clientOptional.get();
            Contract contract = findOrCreateContract(contractData);
            updateContractDetails(contract, contractData, client);
            contractDao.save(contract);
            log.info("Contrato actualizado: {} con ID RealSoft {}", contract.getContract_name(), contract.getIdRealSoft());
        } else {
            log.info("Cliente no encontrado para contrato.");
        }
    }

    private Contract findOrCreateContract(JsonObject contractData) {
        String contractId = contractData.get("id").getAsString();
        if (contractDao.existsByIdRealSoft(contractId)) {
            return contractDao.findByIdRealSoft(contractId).orElse(new Contract());
        }
        return new Contract();
    }

    private void updateContractDetails(Contract contract, JsonObject contractData, Client client) {
        for (Map.Entry<String, JsonElement> entry : contractData.entrySet()) {
            switch (entry.getKey()) {
                case "id" ->
                    contract.setIdRealSoft(entry.getValue().getAsString());
                case "nombre" ->
                    contract.setContract_name(entry.getValue().getAsString());
                case "estado" ->
                    setContractStatus(contract, entry.getValue().getAsString());
                case "inicio" ->
                    contract.setComming(entry.getValue().getAsString());
                case "lat" ->
                    contract.setLat(entry.getValue().getAsString());
                case "lng" ->
                    contract.setLon(entry.getValue().getAsString());
                case "prefactura" ->
                    contract.setPre_invoice(entry.getValue().getAsString());
                case "baja" ->
                    setContractBaja(contract, entry.getValue().getAsString(), client);
                case "motivo_baja" ->
                    contract.setReason_baja(entry.getValue().getAsString());
                case "vendedor" ->
                    contract.setSeller(entry.getValue().getAsString());
                case "observaciones" ->
                    contract.setObservations(entry.getValue().getAsString());
            }
        }
        contract.setClient(client);
    }

    private void setContractStatus(Contract contract, String status) {
        if ("Pendiente de Instalación".equals(status)) {
            contract.setPending_installation(true);
        } else {
            contract.setPending_installation(false);
        }
    }

    private void setContractBaja(Contract contract, String baja, Client client) {
        if (!baja.isEmpty()) {
            contract.setBaja(true);
            contract.setDate_of_baja(baja);
            contract.setClient(client);
        } else {
            contract.setBaja(false);
        }
    }

    private List<Contract> getContracts(Client client) {
        JsonObject jsonResponse = apiGr.getResponseContract(client.getIdRealSoft());
        if (jsonResponse == null) {
            return Collections.emptyList();
        }

        JsonArray contratos = jsonResponse.get("contratos").getAsJsonArray();
        if (contratos == null) {
            return Collections.emptyList();
        }

        List<Contract> contracts = new ArrayList<>();
        for (JsonElement element : contratos) {
            Contract contract = getContract(element);
            if (contract != null) {
                contract.setClient(client);
                contracts.add(contract);
                log.info("Contrato Cargado: {} con ID RealSoft {}", contract.getContract_name(), contract.getIdRealSoft());
            }
        }
        return contracts;
    }

    private Contract getContract(JsonElement element) {
        String contractId = element.getAsJsonObject().get("id").getAsString();
        Contract contract = findOrCreateContract(contractId);

        if (contract == null) {
            return null;
        }

        setContractFields(contract, element.getAsJsonObject());
        return contract;
    }

    private Contract findOrCreateContract(String contractId) {
        if (contractDao.existsByIdRealSoft(contractId)) {
            return contractDao.findByIdRealSoft(contractId).orElse(new Contract());
        }
        return new Contract();
    }

    private void setContractFields(Contract contract, JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            switch (entry.getKey()) {
                case "id" ->
                    contract.setIdRealSoft(entry.getValue().getAsString());
                case "nombre" ->
                    contract.setContract_name(entry.getValue().getAsString());
                case "inicio" ->
                    contract.setComming(entry.getValue().getAsString());
                case "localidad" ->
                    contract.setCity(entry.getValue().getAsString());
                case "lat" ->
                    contract.setLat(entry.getValue().isJsonNull() ? "Sin Cargar" : entry.getValue().getAsString());
                case "lng" ->
                    contract.setLon(entry.getValue().isJsonNull() ? "Sin Cargar" : entry.getValue().getAsString());
                case "prefactura" ->
                    contract.setPre_invoice(entry.getValue().getAsString());
                case "baja" ->
                    setBaja(contract, entry.getValue());
                case "motivo_baja" ->
                    contract.setReason_baja(entry.getValue().getAsString());
                case "vendedor" ->
                    contract.setSeller(entry.getValue().getAsString());
                case "observaciones" ->
                    contract.setObservations(entry.getValue().isJsonNull() ? "Sin Cargar" : entry.getValue().getAsString());
            }
        }
    }

    private void setBaja(Contract contract, JsonElement value) {
        if (!value.getAsString().isEmpty()) {
            contract.setBaja(true);
            contract.setDate_of_baja(value.getAsString());
            log.info("FECHA DE BAJA " + value.getAsString());
        }
    }

}

/*
@Slf4j
@Data
@Service
public class ClientServiceImpl implements iClientService {

    @Autowired
    private RequestsApiGestionReal apiGr;

    @Autowired
    private iAuxiliarDao auxiliarDao;

    @Autowired
    private iClientDao clientDao;

    @Autowired
    private iContractDao contractDao;

    @Autowired
    private iContractHistoryDao contractHistoryDao;

    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    private String typeDate = "m";

    @Async*/
//    @Scheduled(cron = "0 */15 * * * *")
/*public void getClients() throws InterruptedException {
        boolean request = true;
//        Auxiliar auxiliar = auxiliarDao.findByAuxiliarKey("cmpoficiales");
        long offset = 0;
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -60);
        lastDate = calendar.getTime();

        do {
            JsonObject jsonResponse = apiGr.getResponseClient(this.format.format(lastDate), "c", offset);
            if (jsonResponse != null && jsonResponse.has("clientes")) {
                JsonObject clientes = jsonResponse.get("clientes").getAsJsonObject();
                for (Map.Entry<String, JsonElement> element : clientes.entrySet()) {
                    if (!clientDao.existsByIdRealSoft(Long.valueOf(element.getKey()))) {
                        Client client = new Client();
                        client.setIdRealSoft(Long.valueOf(element.getKey()));
                        for (Map.Entry<String, JsonElement> e : element.getValue().getAsJsonObject().entrySet()) {
                            switch (e.getKey()) {
                                case "documento" ->
                                    client.setDni(e.getValue().getAsString());
                                case "nombre" ->
                                    client.setName(e.getValue().getAsString());
                                case "domicilio" -> {
                                    if (!e.getValue().getAsJsonObject().get("direccion").isJsonNull()) {
                                        client.setAddress(e.getValue().getAsJsonObject().get("direccion").getAsString());
                                    }
                                    if (!e.getValue().getAsJsonObject().get("localidad").isJsonNull() || !e.getValue().getAsJsonObject().get("localidad").getAsJsonObject().get("valor").isJsonNull()) {
                                        client.setCity(e.getValue().getAsJsonObject().get("localidad").getAsJsonObject().get("valor").getAsString());
                                    }
                                    if (!e.getValue().getAsJsonObject().get("barrio").isJsonNull() || !e.getValue().getAsJsonObject().get("barrio").getAsJsonObject().get("valor").isJsonNull()) {
                                        client.setDistrict(e.getValue().getAsJsonObject().get("barrio").getAsJsonObject().get("valor").getAsString());
                                    }
                                }
                                case "estado" -> {
                                    if (!e.getValue().getAsJsonObject().get("valor").isJsonNull()) {
                                        client.setStatus(e.getValue().getAsJsonObject().get("valor").getAsString());
                                    }
                                }
                                case "debito_automatico" -> {
                                    if (e.getValue().getAsString().equals("0")) {
                                        client.setAutomatic_debt(false);
                                    } else {
                                        client.setAutomatic_debt(true);
                                    }
                                }
                                case "corporativo" -> {
                                    if (e.getValue().getAsString().equals("0")) {
                                        client.setCorpo(false);
                                    } else {
                                        client.setCorpo(true);
                                    }
                                }
                                case "cartera" -> {
                                    if (!e.getValue().isJsonNull()) {
                                        if (!e.getValue().getAsJsonObject().get("valor").isJsonNull()) {
                                            System.out.println("cliente " + client.getDni() + " " + client.getName());
                                            client.setWallet(e.getValue().getAsJsonObject().get("valor").getAsString());
                                        } else {
                                            client.setWallet("SIN ESTABLECER O ERROR");
                                        }
                                    } else {
                                        client.setWallet("SIN ESTABLECER");
                                    }
                                }
                            }
                        }
                        List<Contract> contracts = getContracts(client);
                        if (!contracts.isEmpty()) {
                            client.setContracts(this.getContracts(client));
                        }

                        clientDao.save(client);
                        System.out.println("cliente CARGADO = " + client.getName() + " Id GR " + client.getIdRealSoft());

                    } else {
                        System.out.println("CLIENTE YA ESTABA CARGADO");
                    }

                }
                offset += 100;
            } else {
                request = false;
                break;
            }
        } while (request);
    }

    @Async*/
//    @Scheduled(cron = "0 0 */12 * * *")
/*public void updateClients() {
        if (this.typeDate.equals("m")) {
            this.typeDate = "c";
        } else {
            this.typeDate = "m";
        }

        boolean request = true;
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        lastDate = calendar.getTime();

        int offset = 0;
        do {
            JsonObject jsonResponse = apiGr.getResponseClient(this.format.format(lastDate), this.typeDate, offset);
//            JsonObject jsonResponse = apiGr.getResponseClient("01-11-2024", "m", offset);
            if (jsonResponse != null && jsonResponse.has("clientes")) {
                JsonObject clientes = jsonResponse.get("clientes").getAsJsonObject();
                for (Map.Entry<String, JsonElement> element : clientes.entrySet()) {

                    Optional<Client> o = clientDao.findByIdRealSoft(Long.valueOf(element.getKey()));

                    if (o.isPresent()) {
                        Client client = o.get();
                        for (Map.Entry<String, JsonElement> e : element.getValue().getAsJsonObject().entrySet()) {
                            switch (e.getKey()) {
                                case "nombre" ->
                                    client.setName(e.getValue().getAsString());
                                case "domicilio" -> {
                                    if (!e.getValue().getAsJsonObject().get("localidad").isJsonNull() || !e.getValue().getAsJsonObject().get("localidad").getAsJsonObject().get("valor").isJsonNull()) {
                                        System.out.println("CLIENTE " + client.getName() + " LOCALIDAD " + e.getValue().getAsJsonObject().get("localidad").getAsJsonObject().get("valor").getAsString());
                                        client.setCity(e.getValue().getAsJsonObject().get("localidad").getAsJsonObject().get("valor").getAsString());
                                    }
                                    if (!e.getValue().getAsJsonObject().get("barrio").isJsonNull() || !e.getValue().getAsJsonObject().get("barrio").getAsJsonObject().get("valor").isJsonNull()) {
                                        client.setDistrict(e.getValue().getAsJsonObject().get("barrio").getAsJsonObject().get("valor").getAsString());
                                    }
                                }
                                case "estado" -> {
                                    if (!e.getValue().getAsJsonObject().get("valor").isJsonNull()) {
                                        client.setStatus(e.getValue().getAsJsonObject().get("valor").getAsString());
                                    }

                                }
                                case "debito_automatico" -> {
                                    if (e.getValue().getAsString().equals("0")) {
                                        client.setAutomatic_debt(false);
                                    } else {
                                        client.setAutomatic_debt(true);
                                    }
                                }
                                case "corporativo" -> {
                                    if (e.getValue().getAsString().equals("0")) {
                                        client.setCorpo(false);
                                    } else {
                                        client.setCorpo(true);
                                    }
                                }
                                case "cartera" -> {
                                    if (!e.getValue().isJsonNull()) {
                                        if (!e.getValue().getAsJsonObject().get("valor").isJsonNull()) {
                                            client.setWallet(e.getValue().getAsJsonObject().get("valor").getAsString());
                                        } else {
                                            client.setWallet("SIN ESTABLECER");
                                        }
                                    } else {
                                        client.setWallet("SIN ESTABLECER");
                                    }
                                }
                            }
                        }
                        List<Contract> contracts = getContracts(client);
                        if (!contracts.isEmpty()) {
                            client.setContracts(this.getContracts(client));
                        }
                        clientDao.save(client);
                        System.out.println("cliente Actualizado = " + client.getName() + " Id GR " + client.getIdRealSoft());

                    } else {
                        System.out.println("no se encontro el cliente");
                    }
                }
                offset += 100;
            } else {
                System.out.println("FIN DE ACTUALIZACION DE CLIENTES");
                request = false;
                break;
            }
        } while (request);
    }

    @Async*/
//    @Scheduled(cron = "0 0 */6 * * *")
/* @Transactional
    public void updateContracts() {
        Date lastDate = new Date();
        Calendar calendar = Calendar.getInstance();  // Obtiene la fecha actual
        calendar.setTime(lastDate);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        lastDate = calendar.getTime();
        JsonObject jsonResponse = apiGr.getResponseUpdateContract(this.format.format(lastDate), "m");
//        JsonObject jsonResponse = apiGr.getResponseUpdateContract("01-11-2024", "m");
        if (jsonResponse != null) {
            JsonArray contratos = jsonResponse.get("contratos").getAsJsonArray();
            if (contratos != null) {
                // Recorrer los pares clave-valor dentro del JsonObject
                for (JsonElement element : contratos) {
                    Contract contract;
                    Optional<Client> c = clientDao.findByIdRealSoft(element.getAsJsonObject().get("cliente_id").getAsLong());
                    if (c.isPresent()) {
                        Client clientSetStatus = c.get();
                        if (contractDao.existsByIdRealSoft(element.getAsJsonObject().get("id").getAsString())) {
                            Optional<Contract> o = contractDao.findByIdRealSoft(element.getAsJsonObject().get("id").getAsString());
                            if (o.isPresent()) {
                                contract = o.get();
                            } else {
                                contract = new Contract();
                            }
                        } else {
                            contract = new Contract();
                        }

                        for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
                            String keyElement = e.getKey();
                            switch (e.getKey()) {
                                case "id":
                                    contract.setIdRealSoft(e.getValue().getAsString());
                                    break;
                                case "nombre":
                                    contract.setContract_name(e.getValue().getAsString());
                                    break;
                                case "estado":
                                    System.out.println("ESTADO = " + e.getValue().getAsString());
                                    if (e.getValue().getAsString().equals("Pendiente de Instalación")) {
                                        contract.setPending_installation(true);
                                    } else {
                                        contract.setPending_installation(false);
                                    }
                                    break;
                                case "inicio":
                                    contract.setComming(e.getValue().getAsString());
                                    break;
                                case "lat":
                                    contract.setLat(e.getValue().getAsString());
                                    break;
                                case "lng":
                                    contract.setLon(e.getValue().getAsString());
                                    break;
                                case "prefactura":
                                    contract.setPre_invoice(e.getValue().getAsString());
                                    if (e.getValue().getAsString().equals("Si")) {
                                        clientSetStatus.setWallet("Cliente x");
                                    }
                                    break;
                                case "baja":
                                    if (!e.getValue().getAsString().equals("")) {
                                        contract.setBaja(true);
                                        contract.setDate_of_baja(e.getValue().getAsString());
                                        clientSetStatus.setStatus("Baja");
                                        System.out.println("FECHA DE BAJA " + e.getValue().getAsString());
                                    } else {
                                        contract.setBaja(false);
                                        contract.setDate_of_baja("");
                                        contract.setReason_baja("");
                                        clientSetStatus.setStatus("Activo");
                                    }
                                    break;
                                case "motivo_baja":
                                    contract.setReason_baja(e.getValue().getAsString());
                                    break;
                                case "vendedor":
                                    contract.setSeller(e.getValue().getAsString());
                                    break;
                                case "observaciones":
                                    if (!e.getValue().isJsonNull()) {
                                        contract.setObservations(e.getValue().getAsString());
                                    }
                                    break;
                            }
                        }

                        if (contract.getCity() == null) {
                            contract.setCity(c.get().getCity());
                        }
                        clientDao.save(clientSetStatus);
                        contract.setClient(c.get());
                        contractDao.save(contract);
                        System.out.println("Contrato Actualizado" + contract.getContract_name() + "  ID GR " + contract.getIdRealSoft());
                    } else {
                        System.out.println("CLIENTE NO CARGADO");
                    }
                }
                System.out.println("FINALIZADA LA ACTUALIZACION DE CONTRATOS");
            }
        }
    }

    private List<Contract> getContracts(Client client) {
        JsonObject jsonResponse = apiGr.getResponseContract(client.getIdRealSoft());
        List<Contract> contracts = new ArrayList();
        if (jsonResponse != null) {
            JsonArray contratos = jsonResponse.get("contratos").getAsJsonArray();
            if (contratos != null) {
                // Recorrer los pares clave-valor dentro del JsonObject
                for (JsonElement element : contratos) {
                    Contract contract;
//                    Optional<Client> c = clientDao.findByIdRealSoft(element.getAsJsonObject().get("cliente_id").getAsLong());
//                    if () {
                        if (contractDao.existsByIdRealSoft(element.getAsJsonObject().get("id").getAsString())) {
                            Optional<Contract> o = contractDao.findByIdRealSoft(element.getAsJsonObject().get("id").getAsString());
                            if (o.isPresent()) {
                                contract = o.get();
                            } else {
                                contract = new Contract();
                            }
                        } else {
                            contract = new Contract();
                        }

                        for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
                            String keyElement = e.getKey();
                            switch (e.getKey()) {
                                case "id":
                                    contract.setIdRealSoft(e.getValue().getAsString());
                                    break;
                                case "nombre":
                                    contract.setContract_name(e.getValue().getAsString());
                                    break;
                                case "inicio":
                                    contract.setComming(e.getValue().getAsString());
                                    break;
                                case "localidad":
                                    contract.setCity(e.getValue().getAsString());
                                    break;
                                case "lat":
                                    if (!e.getValue().isJsonNull()) {
                                        contract.setLat(e.getValue().getAsString());
                                    }
                                    break;
                                case "lng":
                                    if (!e.getValue().isJsonNull()) {
                                        contract.setLon(e.getValue().getAsString());
                                    }
                                    break;
                                case "prefactura":
                                    contract.setPre_invoice(e.getValue().getAsString());
                                    break;
                                case "baja":
                                    if (!e.getValue().getAsString().equals("")) {
                                        contract.setBaja(true);
                                        contract.setDate_of_baja(e.getValue().getAsString());
                                        System.out.println("FECHA DE BAJA " + e.getValue().getAsString());
                                    }
                                    break;
                                case "motivo_baja":
                                    contract.setReason_baja(e.getValue().getAsString());
                                    break;
                                case "vendedor":
                                    contract.setSeller(e.getValue().getAsString());
                                    break;
                                case "observaciones":
                                    if (!e.getValue().isJsonNull()) {
                                        contract.setObservations(e.getValue().getAsString());
                                    }
                                    break;
                            }
                        }
                        contract.setClient(client);
                        contracts.add(contract);
                        System.out.println("Contrato Cargado" + contract.getContract_name() + "  ID GR " + contract.getIdRealSoft());
                    }
                }
            }
//        }
         return contracts;
    }

    @Async
    // Ejecutar el último día de cada mes a las 00:00
    @Scheduled(cron = "0 0 0 L * ?", zone = "America/Argentina/Buenos_Aires")
    public void setHistoric() {
        List<Contract> contracts = contractDao.findAll();
        for (Contract c : contracts) {
            ContractHistory his = new ContractHistory();
            his.setIdRealSoft(c.getIdRealSoft());
            his.setContract_name(c.getContract_name());
            his.setComming(c.getComming());
            his.setCity(c.getCity());
            his.setLat(c.getLat());
            his.setLon(c.getLon());
            his.setBaja(c.isBaja());
            his.setDate_of_baja(c.getDate_of_baja());
            his.setReason_baja(c.getReason_baja());
            his.setClient(c.getClient());
            his.setPre_invoice(c.getPre_invoice());
            his.setRegistrationDate(new Date());
            his.setPending_installation(c.isPending_installation());
            his.setWallet(c.getClient().getWallet());

            contractHistoryDao.save(his);
            System.out.println("Historico Guardado = " + his.getContract_name());

        }
    }

}*/
