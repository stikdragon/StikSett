package uk.co.stikman.sett.gfx.lwjgl;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import uk.co.stikman.sett.gfx.GLException;


public class GLUtil {

	public static void checkError() {
		int n = GL11.glGetError();
		switch (n) {
			case GL11.GL_NO_ERROR: return;
			case GL11.GL_INVALID_ENUM: throw new GLException(n, "Invalid Enum");
			case GL11.GL_INVALID_OPERATION: throw new GLException(n, "Invalid Operation");
			case GL11.GL_INVALID_VALUE: throw new GLException(n, "Invalid Value");
			case GL30.GL_INVALID_FRAMEBUFFER_OPERATION : throw new GLException(n, "Invalid Framebuffer Operation");
			case GL11.GL_OUT_OF_MEMORY : throw new GLException(n, "Out of Memory");
			case GL11.GL_STACK_OVERFLOW: throw new GLException(n, "Stack Overflow");
			case GL11.GL_STACK_UNDERFLOW: throw new GLException(n, "Stack Underflow");
			default: throw new GLException(n, "Unknown OpenGL error");
		}
	}


}
