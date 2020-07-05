package uk.co.stikman.sett.svr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NetSession {

	private Map<String, Object> objects = Collections.synchronizedMap(new HashMap<>());

	@SuppressWarnings("unchecked")
	public <T> T getObject(String key, Class<T> typ) {
		Object o = objects.get(key);
		if (o == null)
			return null;
		return (T) o;
	}

}
