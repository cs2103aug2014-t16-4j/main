package logic.google;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

public class CacheMap extends HashMap<String, List<JSONObject>>{ 
	public void put(String key, JSONObject obj) {
        List<JSONObject> current = get(key);
        if (current == null) {
            current = new ArrayList<JSONObject>();
            super.put(key, current);
        }
        current.add(obj);
    }
}
