package uk.co.stikman.sett.gfx.lwjgl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import uk.co.stikman.sett.gfx.FrameBuffer;
import uk.co.stikman.sett.gfx.RenderTarget;
import uk.co.stikman.sett.gfx.Texture;

public class FrameBufferNative implements FrameBuffer {
	private int					id;
	private TextureNative		texture;
	private final int			height;
	private final int			width;
	private final ColourModel	colourModel;

	public enum ColourModel {
		RGBA,
		GRAYSCALE,
		RGBA_DEPTH
	}

	public FrameBufferNative(int width, int height, ColourModel cm, boolean linearTexture) {
		this.colourModel = cm;
		id = GL30.glGenFramebuffers();
		texture = new TextureNative();
		texture.bind(0);
		this.width = width;
		this.height = height;
		switch (cm) {
		case GRAYSCALE:
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RED, width, height, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, 0);
			break;
		case RGBA:
		case RGBA_DEPTH:
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0);
			break;
		}
		if (linearTexture) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		}

		bind();
		GL32.glFramebufferTexture(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture.getId(), 0);
		GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
		
		if (cm == ColourModel.RGBA_DEPTH) {
			int buf = GL30.glGenRenderbuffers();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, buf);
			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, buf);
		}
		
		if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("FBO create failed");
		unbind();
	}

	@Override
	public String toString() {
		return "FrameBuffer " + id + ": [" + width + ", " + height + "]";
	}

	public int getId() {
		return id;
	}

	@Override
	public void bind() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, id);
	}

	@Override
	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
	}

	@Override
	public void destroy() {
		texture.destroy();
		texture = null;
		GL30.glDeleteFramebuffers(id);
		id = -1;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public Texture getTexture() {
		return texture;
	}

	public RenderTarget getRenderTarget() {
		return new FrameBufferRenderTargetNative(this);
	}

	public ColourModel getColourModel() {
		return colourModel;
	}

}
