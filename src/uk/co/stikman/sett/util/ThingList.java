package uk.co.stikman.sett.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import uk.co.stikman.sett.game.HasId;

public class ThingList<T extends HasId> implements Iterable<T> {
	private Map<Integer, T>	map	= new HashMap<>();
	private final String	name;

	public ThingList(String name) {
		this.name = name;
	}

	public int size() {
		return map.size();
	}

	public T get(int id) {
		T t = map.get(Integer.valueOf(id));
		if (t == null)
			throw new NoSuchElementException(name + " " + id + " does not exist");
		return t;
	}

	public T find(int id) {
		return map.get(Integer.valueOf(id));
	}

	public void put(T obj) {
		map.put(Integer.valueOf(obj.getId()), obj);
	}

	public void remove(T obj) {
		map.remove(Integer.valueOf(obj.getId()));
	}

	public void remove(int id) {
		map.remove(Integer.valueOf(id));
	}

	@Override
	public Iterator<T> iterator() {
		return map.values().iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (T t : map.values()) {
			sb.append(sep).append(t.toString());
			sep = "\n";
		}
		return sb.toString();
	}

}
