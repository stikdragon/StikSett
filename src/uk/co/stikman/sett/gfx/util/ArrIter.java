package uk.co.stikman.sett.gfx.util;

/**
 * <p>
 * Lets you iterate over an array, optionally skipping items if they match the
 * SkipFunc you give it.
 * <p>
 * 
 * <pre>
 * ArrIter&lt;String&gt; iter = new ArrIter&lt;&gt;(array);
 * while (iter.hasNext())
 * 	System.out.println(iter.next());
 * </pre>
 * 
 * @author stik
 *
 * @param <T>
 */
public class ArrIter<T> {
	private T[]			arr;
	private int			idx	= -1;
	private SkipFunc<T>	skip;
	private int last = -1;

	public interface SkipFunc<T> {
		boolean skip(T s);
	}

	public ArrIter(T[] arr) {
		this.arr = arr;
	}

	public ArrIter(T[] arr, SkipFunc<T> skipfunc) {
		this.arr = arr;
		this.skip = skipfunc;
	}

	public boolean hasNext() {
		int i = idx;
		for (;;) {
			++i;
			if (i >= arr.length)
				return false;
			if (!skip(arr[i]))
				return true;
		}
	}

	protected boolean skip(T t) {
		if (skip == null)
			return false;
		return skip.skip(t);
	}

	public T next() {
		last = idx;
		for (;;) {
			++idx;
			if (idx >= arr.length)
				throw new IndexOutOfBoundsException();
			if (!skip(arr[idx]))
				return arr[idx];
		}
	}

	/**
	 * Returns <code>null</code> if {@link #hasNext()} would return
	 * <code>false</code>
	 * 
	 * @return
	 */
	public T peek() {
		int i = idx;
		for (;;) {
			++i;
			if (i >= arr.length)
				return null;
			if (!skip(arr[i]))
				return arr[i];
		}
	}

	public int position() {
		return idx + 1;
	}

	public void rewind() {
		if (last == -1)
			throw new IllegalStateException("Iterator has alreayd been rewound once, or is at the start");
		idx = last;
		last = -1;
	}

	@Override
	public String toString() {
		T t = peek();
		if (t == null)
			return "null";
		return t.toString();
	}
}
