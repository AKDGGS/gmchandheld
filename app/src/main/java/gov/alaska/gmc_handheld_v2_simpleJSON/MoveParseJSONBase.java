package gov.alaska.gmc_handheld_v2_simpleJSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class MoveParseJSONBase {

    public HashMap<String, String> getBaseNodesFromJSON(JSONObject o) throws JSONException {
        Iterator<?> keys;
        keys = o.keys();

        HashMap<String, String> map = new HashMap<>();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Object val = o.get(key);

            if(key.equals("containerPath")){
                System.out.println("Current Location: " + o.get("containerPath"));
            }

            key = "\"" + key + "\"";
            if (!(val instanceof JSONObject || val instanceof JSONArray)) {
                val = "\"" + val + "\"";
            }
            map.put(key, val.toString());
        }
        return map;
    }
}
