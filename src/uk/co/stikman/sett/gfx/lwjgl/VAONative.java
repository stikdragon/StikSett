package uk.co.stikman.sett.gfx.lwjgl;

import org.lwjgl.opengl.GL30;

import uk.co.stikman.sett.gfx.VAO;

public class VAONative implements VAO {

	private int	id;

	public VAONative() {
		id = GL30.glGenVertexArrays();
	}
	
	@Override
	public void bind() {
		GL30.glBindVertexArray(id);
	}
		
	@Override
	public void delete() {
		GL30.glDeleteVertexArrays(id);
	}
	
	@Override
	public String toString() {
		if (id == -1)
			return "VAO [DELETED]";
		return "VAO [id=" + id + "]";
	}


}
