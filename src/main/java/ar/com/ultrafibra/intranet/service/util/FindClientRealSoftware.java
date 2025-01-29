package ar.com.ultrafibra.intranet.service.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.*;
import org.springframework.stereotype.Service;

@Service
public class FindClientRealSoftware extends CreatePassword {
    public HttpResponse<JsonNode> getClientRealSoft(String number) {
        HttpResponse<JsonNode> documentTypeMapResponse = getListDocumentType();
        if (documentTypeMapResponse != null) {
            Map<String, String> documentTypeMap = getDocumentTypeMap(documentTypeMapResponse);
            for (Map.Entry<String, String> entry : documentTypeMap.entrySet()) {
                String body = String.format("""
                        {
                            "action": "cliente",
                            "tipo_documento": "%s",
                            "nro_documento": "%s"
                        } """, entry.getKey(), number);

                return this.getResponse(body);
            }
        }
        return null;
    }

    // Obtengo la tabla de tipos de Documentos, Para soicitar el cliente a la Api de GR ya que necesita un body con 2 parametros, Tipo y Numero de documento.
    private HttpResponse<JsonNode> getListDocumentType() {
        String body = String.format("""
            {
                "action": "tabref",
                "tabla": "tipodocumento"
            }
            """);
        return this.getResponse(body);
    }

    private Map<String, String> getDocumentTypeMap(HttpResponse<JsonNode> documentTypeMapResponse) {
        if (documentTypeMapResponse.getBody().getObject().has("tabla")) {
            return null;
        }
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(documentTypeMapResponse.getBody().toString(), JsonObject.class);
        JsonElement clientJson = jsonObject.get("tabla");
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> documentTypeMap = gson.fromJson(clientJson, mapType);
        return documentTypeMap;
    }

    private HttpResponse<JsonNode> getResponse(String body) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, createPassword())
                    .body(body)
                    .asJson();
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();
            if (response.getStatus() == 200 && errorValue.equals("0")) {
                return response;
            }
        } catch (UnirestException ex) {
            Logger.getLogger(RequestsApiGestionReal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
