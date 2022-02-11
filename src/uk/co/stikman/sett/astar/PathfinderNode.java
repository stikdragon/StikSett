package uk.co.stikman.sett.astar;

/**
 * These are X,Y points. <code>A.equals(B)</code> when X and Y are the same,
 * height is ignored
 * 
 * @author frenchd
 * 
 */
public class PathfinderNode<T> {

	private final T				node;
	public PathfinderNode<T>	previous;
	public float				height;
	public float				g;
	public float				f;

	/**
	 * @param x
	 * @param y
	 */
	public PathfinderNode(T node) {
		this.node = node;
	}

	public T getNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathfinderNode other = (PathfinderNode) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

}