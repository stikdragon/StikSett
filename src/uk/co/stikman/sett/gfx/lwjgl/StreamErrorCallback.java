package uk.co.stikman.sett.gfx.lwjgl;

import java.io.PrintStream;

import org.lwjgl.glfw.GLFWErrorCallbackI;

public class StreamErrorCallback implements GLFWErrorCallbackI {

	private PrintStream stream;

	public StreamErrorCallback(PrintStream str) {
		this.stream = str;
	}

	@Override
	public void invoke(int error, long description) {
		stream.println("GLError " + error + ":" + description);
	}

}
