package uk.co.stikman.sett.gfx.lwjgl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.sett.gfx.Texture;



/**
 * Textures will always be a power of two, so if the actual image isn't it'll
 * end up smaller inside the texture, subimage size is actual size
 * 
 * @return
 */
public class TextureNative implements Texture {

	private int		height;
	private int		width;
	private float	imageW;
	private float	imageH;

	private int		id	= -1;

	@Override
	public void setSize(int w, int h) {
		this.width = w;
		this.height = h;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public void setImageSize(float w, float h) {
		this.imageW = w;
		this.imageH = h;
	}

	public float getImageW() {
		return imageW;
	}

	public float getImageH() {
		return imageH;
	}

	public TextureNative() {
		id = GL11.glGenTextures();
	}

	/**
	 * Texture unit should be 0..31 (or max)
	 * 
	 * @param textureunit
	 * @param uniform
	 */
	@Override
	public void bindUniform(int textureunit, int uniform) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureunit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL20.glUniform1i(uniform, textureunit);
	}

	/**
	 * Texture unit should be 0..31 (or max)
	 * 
	 * @param textureunit
	 */
	@Override
	public void bind(int textureunit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureunit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	}

	@Override
	public void destroy() {
		GL11.glDeleteTextures(id);
		id = -1;
	}

	public int getId() {
		return id;
	}

}
