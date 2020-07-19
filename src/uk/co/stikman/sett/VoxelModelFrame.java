package uk.co.stikman.sett;

public class VoxelModelFrame {
	private VoxelModel	model;
	private int			frame;

	public VoxelModel getModel() {
		return model;
	}

	public void setModel(VoxelModel model) {
		this.model = model;
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + frame;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		VoxelModelFrame other = (VoxelModelFrame) obj;
		if (frame != other.frame)
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

}
