//@author A0117993R
package logic.google;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

// Custom HashMap to support same key multiple values 
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
