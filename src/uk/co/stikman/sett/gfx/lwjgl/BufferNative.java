package uk.co.stikman.sett.gfx.lwjgl;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import uk.co.stikman.sett.gfx.Buffer;


public class BufferNative implements Buffer {

	private int	id;
	private int	lastBindTarget;

	public BufferNative() {
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		GL15.glGenBuffers(buffer);
		this.id = buffer.get(0);
	}

	@Override
	public void delete() {
		GL15.glDeleteBuffers(id);
		id = -1;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		if (id == -1)
			return "GLBuffer [DELETED]";
		return "GLBuffer [id=" + id + "]";
	}

	@Override
	public void bind(int target) {
		GL15.glBindBuffer(target, id);
		lastBindTarget = target;
	}

	@Override
	public void unbind() {
		if (lastBindTarget == -1)
			return;
		GL15.glBindBuffer(lastBindTarget, 0);
		lastBindTarget = -1;
	}

}
