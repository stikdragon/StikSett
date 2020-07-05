package uk.co.stikman.sett.svr;

import java.util.HashMap;
import java.util.Map;

public class DestHandlers {
	private Map<String, Destination> handlers = new HashMap<>();
	
	public void add(Destination d) {
		handlers.put(d.getName(), d);
	}
	
	public Destination get(String key) {
		return handlers.get(key);
	}
}
