package uk.co.stikman.sett.game;

public class VoxelModelParams {
	private char	rotation	= ' ';
	private String	name;

	public VoxelModelParams() {

	}

	public VoxelModelParams(String name) {
		super();
		this.name = name;
	}

	public VoxelModelParams(char rotation, String name) {
		super();
		this.rotation = rotation;
		this.name = name;
	}

	public char getRotation() {
		return rotation;
	}

	public void setRotation(char rotation) {
		this.rotation = rotation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + rotation;
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
		VoxelModelParams other = (VoxelModelParams) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rotation != other.rotation)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VoxelModelParams [rotation=" + rotation + ", name=" + name + "]";
	}

}
