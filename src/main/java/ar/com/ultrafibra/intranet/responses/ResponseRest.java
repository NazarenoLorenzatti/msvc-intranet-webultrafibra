package ar.com.ultrafibra.intranet.responses;

import java.util.ArrayList;
import java.util.HashMap;

public class ResponseRest {

    private final ArrayList<HashMap<String, String>> metadata = new ArrayList<>();

    public ArrayList<HashMap<String, String>> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String respuesta, String codigo, String info) {
		
		HashMap<String, String> mapeo = new HashMap<>();
		
		mapeo.put("respuesta", respuesta);
		mapeo.put("codigo", codigo);
		mapeo.put("informacion", info);
		
		metadata.add(mapeo);
	}
}
