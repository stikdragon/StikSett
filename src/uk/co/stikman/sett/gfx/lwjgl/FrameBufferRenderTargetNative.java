package uk.co.stikman.sett.gfx.lwjgl;

import org.lwjgl.opengl.GL11;

import uk.co.stikman.sett.gfx.RenderTarget;
import uk.co.stikman.utils.math.Vector2i;

public class FrameBufferRenderTargetNative implements RenderTarget {

	private FrameBufferNative buf;

	public FrameBufferRenderTargetNative(FrameBufferNative buf) {
		this.buf = buf;
	}

	@Override
	public Vector2i getDimensions(Vector2i out) {
		out.x = buf.getWidth();
		out.y = buf.getHeight();
		return out;
	}

	@Override
	public void targetUnbound() {
		buf.unbind();
	}

	@Override
	public void targetBound() {
		buf.bind();

	}

	@Override
	public void apply(Vector2i v) {
		GL11.glViewport(0, 0, v.x, v.y);
	}

}
