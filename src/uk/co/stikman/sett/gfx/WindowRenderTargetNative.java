package uk.co.stikman.sett.gfx;

import org.lwjgl.opengl.GL11;

import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.utils.math.Vector2i;

public class WindowRenderTargetNative implements RenderTarget {

	private Window3DNative window;

	public WindowRenderTargetNative(Window3DNative window) {
		this.window = window;
	}

	@Override
	public Vector2i getDimensions(Vector2i out) {
		out.x = window.getWidth();
		out.y = window.getHeight();
		return out;
	}

	@Override
	public void targetUnbound() {
	}

	@Override
	public void targetBound() {
	}

	@Override
	public void apply(Vector2i v) {
		GL11.glViewport(0, 0, v.x, v.y);
	}

}
