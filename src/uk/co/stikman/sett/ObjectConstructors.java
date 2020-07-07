package uk.co.stikman.sett;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import uk.co.stikman.sett.game.IsSerializable;

public class ObjectConstructors {
	private final Map<Class<?>, Supplier<IsSerializable>>	xtors		= new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T extends IsSerializable> void add(Class<T> type, Supplier<T> xtor) {
		if (Modifier.isAbstract(type.getModifiers()))
			throw new IllegalArgumentException("Class [" + type + "] cannot be abstract");
		xtors.put(type, (Supplier<IsSerializable>) xtor);
	}

	public Supplier<IsSerializable> find(Class<?> cls) {
		return xtors.get(cls);
	}
	
}
