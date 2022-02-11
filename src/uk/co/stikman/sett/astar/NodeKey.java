package uk.co.stikman.sett.astar;

public class NodeKey {
	public NodeKey(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public final int	x;
	public final int	y;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + x;
		return prime * result + y;
	}

	@Override
	public boolean equals(Object obj) {
		NodeKey other = (NodeKey) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PointKey [x=" + x + ", y=" + y + "]";
	}

}