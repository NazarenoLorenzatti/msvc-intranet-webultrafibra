package ar.com.ultrafibra.intranet.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class RequestsApiGestionReal extends CreatePassword {

    public JsonObject getResponseCommon(long offset, String action, String date) {
        String body = String.format("""
            {
                "action": "%s",
                "cantidad": 100,
                "fecha_desde": "%s",
                "offset": %d
            }
            """, action, date, offset);
        return this.getJsonObjectResponse(this.getResponse(body));
    }

    public JsonObject getResponseClient(String date, String type_date, long offset) {
        String body = String.format("""
            {
                "action": "clientes_consulta",
                "fecha_tipo": "%s",
                "fecha_desde": "%s",
                "offset": %d
            }
            """, type_date, date, offset);
        return this.getJsonObjectResponse(this.getResponse(body));
    }

    public JsonObject getResponseContract(long idClient) {
        String body = String.format("""
            {
                "action": "contrato",
                "incluye_bajas": "S",
                "cli_id": %d
            }
            """, idClient);
        return this.getJsonObjectResponse(this.getResponse(body));
    }

    public JsonObject getResponseUpdateContract(String date, String type_date) {
        String body = String.format("""
            {
                "action": "contratos",
                "fecha_tipo": "%s",
                "fecha_desde": "%s"
            }
            """, type_date, date);
        return this.getJsonObjectResponse(this.getResponse(body));
    }
    
    public JsonObject getResponseTicket(long offset, String typeDate, String date) {
        String body = String.format("""
            {
                "action": "casos",
                "fecha_tipo": "%s",
                "fecha_desde": "%s",
                "offset": %d
            }
            """, typeDate, date, offset);

        return this.getJsonObjectResponseAlternative(this.getResponse(body));
    }
     
    public JsonObject getResponseOrderService(String date, String type_date) {
        String body = String.format("""
            {
                "action": "ordenesdeservicio",
                "fecha_tipo": "%s",
                "fecha_desde": "%s"
            }
            """, type_date, date);
        return this.getJsonObjectResponseAlternative(this.getResponse(body));
    }
  
    // Unificacion de Metodos //
    private HttpResponse<JsonNode> getResponse(String body) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body(body)
                    .asJson();

            if (response.getStatus() == 200) {
                return response;
            }
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private JsonObject getJsonObjectResponse(HttpResponse<JsonNode> response) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
        String errorValue = jsonObject.get("error").getAsString();
        if (errorValue.equals("0")) {
            return jsonObject;
        }
        return null;
    }

    private JsonObject getJsonObjectResponseAlternative(HttpResponse<JsonNode> response) {
        if (response != null) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            if (!jsonObject.isEmpty()) {
                return jsonObject;
            }
        }
        return null;
    }

}
