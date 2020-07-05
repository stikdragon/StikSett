package uk.co.stikman.sett;

public class VoxelFrame {
	private final int	id;
	private float		duration;

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public int getId() {
		return id;
	}

	public VoxelFrame(int id) {
		super();
		this.id = id;
	}

	public VoxelFrame(int id, float duration) {
		super();
		this.id = id;
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "VoxelFrame [id=" + id + ", duration=" + duration + "]";
	}

}
