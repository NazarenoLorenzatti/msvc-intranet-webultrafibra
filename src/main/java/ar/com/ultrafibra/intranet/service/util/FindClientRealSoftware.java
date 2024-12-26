package ar.com.ultrafibra.intranet.service.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FindClientRealSoftware extends CreatePassword{
    
    public HttpResponse<JsonNode> getClientRealSoft(String number) {
        HttpResponse<JsonNode> documentTypeMapResponse = getListDocumentType();
        if (documentTypeMapResponse != null) {
            if (documentTypeMapResponse.getStatus() == 200) {
                if (documentTypeMapResponse.getBody().getObject().has("tabla")) {
                    Map<String, String> documentTypeMap = new HashMap();
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(documentTypeMapResponse.getBody().toString(), JsonObject.class);
                    JsonElement clientJson = jsonObject.get("tabla");
                    Type mapType = new TypeToken<Map<String, String>>() {
                    }.getType();
                    documentTypeMap = gson.fromJson(clientJson, mapType);

                    for (Map.Entry<String, String> entry : documentTypeMap.entrySet()) {
                        try {
                            HttpResponse<JsonNode> response = Unirest.post("https://api.gestionreal.com.ar/")
                                    .header("Content-Type", "application/json")
                                    .basicAuth(this.username, createPassword())
                                    .body("{ \r\n \"action\": \"cliente\", \r\n\"tipo_documento\":" + entry.getKey() + ", \r\n \"nro_documento\": " + number + " \r\n} \r\n")
                                    .asJson();

                            Gson gsonResp = new Gson();
                            JsonObject jsonObjectResp = gsonResp.fromJson(response.getBody().toString(), JsonObject.class);
                            String errorValue = jsonObjectResp.get("error").getAsString();

                            if (errorValue.equals("0")) {
                                return response;
                            }
                        } catch (UnirestException ex) {
//                            Logger.getLogger(ClientServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    // Obtengo la tabla de tipos de Documentos, Para soicitar el cliente a la Api de GR ya que necesita un body con 2 parametros, Tipo y Numero de documento.
    private HttpResponse<JsonNode> getListDocumentType() {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://api.gestionreal.com.ar/")
                    .header("Content-Type", "application/json")
                    .basicAuth(this.username, this.createPassword())
                    .body("{\r\n \"action\": \"tabref\",\r\n \"tabla\": \"tipodocumento\"\r\n}\r\n")
                    .asJson();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
            String errorValue = jsonObject.get("error").getAsString();

            if (errorValue.equals("0")) {
                return response;
            }

        } catch (UnirestException ex) {
//            Logger.getLogger(ClientServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }

}
