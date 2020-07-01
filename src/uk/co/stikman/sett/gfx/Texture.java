package uk.co.stikman.sett.gfx;

public interface Texture {

	void setSize(int w, int h);

	void setImageSize(float w, float h);

	/**
	 * Texture unit should be 0..31 (or max)
	 * 
	 * @param textureunit
	 * @param uniform
	 */
	void bindUniform(int textureunit, int uniform);

	/**
	 * Texture unit should be 0..31 (or max)
	 * 
	 * @param textureunit
	 */
	void bind(int textureunit);

	void destroy();

}