package uk.co.stikman.sett.client.renderer;

public class ChunkKey {
	public int	cx;
	public int	cy;

	public ChunkKey(ChunkKey copy) {
		this.cx = copy.cx;
		this.cy = copy.cy;
	}

	public ChunkKey() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cx;
		result = prime * result + cy;
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
		ChunkKey other = (ChunkKey) obj;
		if (cx != other.cx)
			return false;
		if (cy != other.cy)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + cx + ", " + cy + "]";
	}

}
