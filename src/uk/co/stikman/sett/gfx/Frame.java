package uk.co.stikman.sett.gfx;

import uk.co.stikman.utils.math.Vector2i;

public class Frame {

	private int			duration;
	private Vector2i	imageoffset;
	private int			index;

	public Frame(int index, int duration, Vector2i imageoffset) {
		this.index = index;
		this.duration = duration;
		this.imageoffset = imageoffset;
	}

	public int getDuration() {
		return duration;
	}

	public Vector2i getImageoffset() {
		return imageoffset;
	}

	@Override
	public String toString() {
		return "Frame [duration=" + duration + ", imageoffset=" + imageoffset + ", index=" + index + "]";
	}

	public int getIndex() {
		return index;
	}

}
