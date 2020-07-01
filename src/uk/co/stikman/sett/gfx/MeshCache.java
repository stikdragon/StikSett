package uk.co.stikman.sett.gfx;

import java.util.HashMap;
import java.util.Map;

import uk.co.stikman.sett.gfx.util.StringTable;


public class MeshCache {
	public class Entry {
		int			hits;
		PolyMesh	mesh;

		public Entry() {
		}

		public int getHits() {
			return hits;
		}

		public PolyMesh getMesh() {
			return mesh;
		}

		public void setMesh(PolyMesh mesh) {
			this.mesh = mesh;
		}

	}

	private Map<Object, Entry> cache = new HashMap<>();

	public Entry get(Object key) {
		Entry e = cache.get(key);
		if (e == null)
			cache.put(key, e = new Entry());
		e.hits++;
		return e;
	}

	@Override
	public String toString() {
		StringTable st = new StringTable("Object", "Count", "Source");
		for (java.util.Map.Entry<Object, Entry> e : cache.entrySet())
			st.add(e.getKey().toString(), Integer.toString(e.getValue().getHits()), e.getValue().getMesh().toString());
		return st.toString();
	}

}
