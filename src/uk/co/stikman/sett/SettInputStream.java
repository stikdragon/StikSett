package uk.co.stikman.sett;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import uk.co.stikman.sett.game.IsSerializable;
import uk.co.stikman.utils.StikDataInputStream;

public class SettInputStream extends StikDataInputStream {

	private final Map<Class<?>, Supplier<Object>> xtors = new HashMap<>();

	public SettInputStream(InputStream src) {
		super(src);
	}

	public <T extends IsSerializable> T readObject(Class<T> type) {
	}

	public List<String> readStringList() {
	}

	@SuppressWarnings("unchecked")
	public <T extends IsSerializable> void addObjectConstructor(Class<T> type, Supplier<T> xtor) {
		if (!Modifier.isAbstract(type.getModifiers()))
			throw new IllegalArgumentException("Class [" + type + "] cannot be abstract");
		xtors.put(type, (Supplier<Object>) xtor);
	}

}
