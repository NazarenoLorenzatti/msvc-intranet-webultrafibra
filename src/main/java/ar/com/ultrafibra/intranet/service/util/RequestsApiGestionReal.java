package ar.com.ultrafibra.intranet.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class RequestsApiGestionReal extends CreatePassword {

    public JsonObject getResponse(long offset, String action, String date) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{\r\n \"action\": \"" + action + "\", \r\n \"cantidad\": 100,\r\n \"fecha_desde\": \""+date+"\",\r\n \"offset\": " + offset + "\r\n}\r\n")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();
            if (errorValue.equals("0")) {
                return jsonObject;
            }
        }
        return null;
    }

    public JsonObject getResponseInvoice(long offset, String ofDate) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{\r\n \"action\": \"cmpoficiales\", \r\n \"cantidad\": 100,\r\n \"fecha_desde\": \"" + ofDate + "\",\r\n \"offset\": " + offset + "\r\n  }\r\n")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();
            if (errorValue.equals("0")) {
                return jsonObject;
            }
        }
        return null;
    }
    
     public JsonObject getResponseClient(String date, String type_date, long offset) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{ \r\n \"action\": \"clientes_consulta\",\r\n \"fecha_tipo\": \""+type_date+"\",\r\n \"fecha_desde\": \""+date+"\",\r\n \"offset\": "+offset+"\r\n} \r\n")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();
            if (errorValue.equals("0")) {
                return jsonObject;
            }
        }
        return null;
    }

  
    public JsonObject getResponseContract(long idClient) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{ \r\n    \"action\": \"contrato\",\r\n    \"incluye_bajas\":\"S\",\r\n    \"cli_id\": "+idClient+"\r\n}")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();
            if (errorValue.equals("0")) {
                return jsonObject;
            }
        }
        return null;
    }
    
    public JsonObject getResponseUpdateContract(String date, String type_date) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{ \r\n \"action\": \"contratos\",\r\n \"fecha_tipo\": \""+type_date+"\",\r\n \"fecha_desde\": \""+date+"\"\r\n} \r\n")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();
            if (errorValue.equals("0")) {
                return jsonObject;
            }
        }
        return null;
    }

    public JsonObject getResponseTicket(long offset, String typeDate, String date) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{\r\n\"action\": \"casos\", \r\n\"fecha_tipo\": \""+typeDate+"\",\r\n\"fecha_desde\": \""+date+"\",\r\n\"offset\":"+offset+"\r\n}")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            if (response.getStatus() == 200) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
                return jsonObject;
            } else {
                return null;
            }
        }
        return null;

    }

    public JsonObject getResponseOrderService(String date, String type_date) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{ \r\n \"action\": \"ordenesdeservicio\",\r\n \"fecha_tipo\": \""+type_date+"\",\r\n \"fecha_desde\": \""+date+"\"\r\n} \r\n")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            if (!jsonObject.isEmpty()) {
                return jsonObject;
            }
        }

        return null;
    }

    public JsonObject getResponseTicket(long cliId) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body("{\r\n \"action\": \"casos\", \r\n\"caso_id\": " + cliId + "\r\n}")
                    .asJson();
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (response != null) {
            if (response.getStatus() == 200) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
                return jsonObject;
            } else {
                return null;
            }
        }
        return null;
    }

}
