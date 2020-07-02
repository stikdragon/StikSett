package uk.co.stikman.sett.gfx.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import uk.co.stikman.utils.math.Vector2i;

public class Vector2iList implements Iterable<Vector2i> {
	private Vector2i[]	vectors;
	private int			increment	= 100;
	private int			size		= 0;

	public Vector2iList() {
		vectors = new Vector2i[increment];
		for (int i = 0; i < vectors.length; ++i)
			vectors[i] = new Vector2i();
	}

	private void ensureSize(int size) {
		if (vectors.length < size) {
			Vector2i[] tmp = new Vector2i[size + increment];
			System.arraycopy(vectors, 0, tmp, 0, this.size);
			for (int i = this.size; i < tmp.length; ++i)
				tmp[i] = new Vector2i();
			vectors = tmp;
		}
	}

	public void add(int x, int y) {
		ensureSize(size + 1);
		vectors[size].set(x, y);
		++size;
	}

	public void clear() {
		size = 0;
	}

	public Vector2i get(int idx) {
		return vectors[idx];
	}

	public int size() {
		return size;
	}

	@Override
	public Iterator<Vector2i> iterator() {
		if (size == 0)
			return Collections.emptyIterator();
		return Arrays.stream(vectors, 0, size - 1).iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (Vector2i v : this) {
			sb.append(v).append(sep);
			sep = "\n";
		}
		return sb.toString();
	}
}
