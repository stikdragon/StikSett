package uk.co.stikman.sett.gfx.lwjgl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.sett.gfx.Buffer;
import uk.co.stikman.sett.gfx.VAO;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.text.BitmapFont;
import uk.co.stikman.sett.gfx.text.BitmapFont.BMChar;
import uk.co.stikman.sett.gfx.text.BitmapText;
import uk.co.stikman.sett.gfx.text.Fragment;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.text.TextFragments;
import uk.co.stikman.sett.gfx.text.WordWrap;
import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector4;

public class BitmapTextNative extends BitmapText {
	private Buffer					vertices;
	private Buffer					indicies;
	private VAO						vao;
	private boolean					invalid	= true;
	private int						vertsToDraw;
	private static final Vector2	tmpV	= new Vector2();

	public BitmapTextNative(Window3D window, ShaderNative shader, BitmapFont font, RenderTextOptions options, int maxwidth) {
		super(window, shader, font, options, maxwidth);
	}

	protected void rebuildBuffers() {
		if (vao == null)
			vao = new VAONative();
		if (vertices == null)
			vertices = new BufferNative();
		if (indicies == null)
			indicies = new BufferNative();

		TextFragments fragments = getFragments();
		String text = fragments.toString();
		float[] colours = new float[text.length() * 4];
		for (Fragment f : fragments) {
			for (int i = f.getStartIndex(); i < f.getEndIndex(); ++i) {
				if (f.isReset()) {
					colours[i * 4] = 1.0f;
					colours[i * 4 + 1] = 1.0f;
					colours[i * 4 + 2] = 1.0f;
					colours[i * 4 + 3] = 1.0f;
				} else {
					float[] x = f.getColour();
					colours[i * 4] = x[0];
					colours[i * 4 + 1] = x[1];
					colours[i * 4 + 2] = x[2];
					colours[i * 4 + 3] = 1.0f;
				}
			}
		}

		//
		// Loop over the characters in the text and create rects for each
		//
		boolean includeColour = options.isColourFormatting();
		float[] data = new float[text.length() * 4 * 4 * (includeColour ? 2 : 1)];
		short[] indices = new short[text.length() * 2 * 3];
		float curx = 0.0f;
		float cury = 0.0f;
		int dataptr = 0;
		int idxptr = 0;
		short vertnumber = 0;
		int lineheight = getLineHeight();
		BMChar last = null;
		int lastSpace = -1;
		for (int i = 0; i < text.length(); ++i) {
			if (text.charAt(i) == ' ' && curx > 0)
				lastSpace = i;
			char c = text.charAt(i);
			if (c == 13) {
				curx = 0.0f;
				cury += lineheight;
				continue;
			}

			BMChar ch = font.get(c);
			if (ch == null)
				continue;
			int kern = font.getKerning(last, ch);
			float x = curx + ch.offsetx + kern;
			float y = cury + ch.offsety;

			curx += ch.xadvance;

			if (curx > maxwidth && options.getWrap() != WordWrap.NONE) {
				if (options.getWrap() == WordWrap.BREAK_WORD) {
					curx = 0.0f;
					x = 0.0f;
					cury += lineheight;
					y = cury + ch.offsety;
				} else if (options.getWrap() == WordWrap.WRAP && lastSpace != -1) {
					//
					// Little more fiddly, need to rewind to the previous space
					//
					curx = 0.0f;
					cury += lineheight;
					int d = i - lastSpace;
					dataptr -= 16 * d * (includeColour ? 2 : 1);
					idxptr -= 6 * d;
					vertnumber -= 4 * d;
					i = lastSpace;
					lastSpace = -1;
					continue;
				}
			}

			data[dataptr++] = x;
			data[dataptr++] = y;// + ch.offsety;
			data[dataptr++] = (float) ch.x / font.getMapWidth();
			data[dataptr++] = (float) ch.y / font.getMapHeight();
			if (includeColour) {
				data[dataptr++] = colours[i * 4];
				data[dataptr++] = colours[i * 4 + 1];
				data[dataptr++] = colours[i * 4 + 2];
				data[dataptr++] = colours[i * 4 + 3];
			}

			data[dataptr++] = x + ch.width;
			data[dataptr++] = y;// + ch.offsety;
			data[dataptr++] = (float) (ch.x + ch.width) / font.getMapWidth();
			data[dataptr++] = (float) ch.y / font.getMapHeight();
			if (includeColour) {
				data[dataptr++] = colours[i * 4];
				data[dataptr++] = colours[i * 4 + 1];
				data[dataptr++] = colours[i * 4 + 2];
				data[dataptr++] = colours[i * 4 + 3];
			}

			data[dataptr++] = x + ch.width;
			data[dataptr++] = y + ch.height;// + ch.offsety;
			data[dataptr++] = (float) (ch.x + ch.width) / font.getMapWidth();
			data[dataptr++] = (float) (ch.y + ch.height) / font.getMapHeight();
			if (includeColour) {
				data[dataptr++] = colours[i * 4];
				data[dataptr++] = colours[i * 4 + 1];
				data[dataptr++] = colours[i * 4 + 2];
				data[dataptr++] = colours[i * 4 + 3];
			}

			data[dataptr++] = x;
			data[dataptr++] = y + ch.height;// + ch.offsety;
			data[dataptr++] = (float) ch.x / font.getMapWidth();
			data[dataptr++] = (float) (ch.y + ch.height) / font.getMapHeight();
			if (includeColour) {
				data[dataptr++] = colours[i * 4];
				data[dataptr++] = colours[i * 4 + 1];
				data[dataptr++] = colours[i * 4 + 2];
				data[dataptr++] = colours[i * 4 + 3];
			}

			indices[idxptr++] = vertnumber;
			indices[idxptr++] = (short) (vertnumber + 1);
			indices[idxptr++] = (short) (vertnumber + 2);
			indices[idxptr++] = vertnumber;
			indices[idxptr++] = (short) (vertnumber + 2);
			indices[idxptr++] = (short) (vertnumber + 3);
			vertnumber += 4;
			last = ch;
		}

		vertsToDraw = idxptr;

		FloatBuffer faverts = BufferUtils.createFloatBuffer(data.length);
		faverts.put(data).flip();
		ShortBuffer iaidxes = BufferUtils.createShortBuffer(indices.length);
		for (int i = 0; i < indices.length; ++i)
			iaidxes.put(indices[i]);
		iaidxes.flip();

		int stride = 16;
		if (options.isColourFormatting())
			stride += 16;

		vao.bind();
		vertices.bind(GL15.GL_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, faverts, GL15.GL_STATIC_DRAW);

		indicies.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iaidxes, GL15.GL_STATIC_DRAW);

		int npos = window.getAttribLocation("vertexPosition");
		int ncolour = window.getAttribLocation("vertexColour");
		int nuv = window.getAttribLocation("vertexUV");

		GL20.glEnableVertexAttribArray(npos);
		GL20.glEnableVertexAttribArray(nuv);
		if (options.isColourFormatting())
			GL20.glEnableVertexAttribArray(ncolour);
		GL20.glVertexAttribPointer(npos, 2, GL11.GL_FLOAT, false, stride, 0);
		GL20.glVertexAttribPointer(nuv, 2, GL11.GL_FLOAT, false, stride, 8);
		if (options.isColourFormatting())
			GL20.glVertexAttribPointer(ncolour, 4, GL11.GL_FLOAT, false, stride, 16);

		invalid = false;
	}

	/**
	 * You can force the font to be a certain colour. You can also set the mix
	 * to control how much of this colour is used. At <code>1.0f</code> it'll
	 * use this colour exclusively, at <code>0.0f</code> it will ignore this
	 * colour. <code>0.5f</code> will blend it half with this colour and half
	 * with the text's own specified colours
	 * 
	 */
	@Override
	public void render(Matrix4 projmat, Vector4 overrideColour, float overrideColourMix) {
		if (invalid)
			rebuildBuffers();

		shader.use();
		shader.getUniform("proj", false).bindMat4(projmat);
		shader.getUniform("txt", false).bindTexture(font.getPages()[0].getImage(), 0);

		vao.bind();
		if (options.getOutlineMode() != OutlineMode.NONE) {
			Vector4 c = options.getOutlineColour();
			shader.getUniform("colourOverride").bindFloat(options.getOutlineBlendFactor());
			shader.getUniform("colourOverrideColour").bindVec4(c.x, c.y, c.z, c.w);

			if (options.getOutlineMode() == OutlineMode.OUTLINE) {
				for (int dx = -1; dx < 2; ++dx) {
					for (int dy = -1; dy < 2; ++dy) {
						if (dx == 0 && dy == 0)
							continue;
						tmpV.set(dx, dy);
						shader.getUniform("offset").bindVec2(tmpV);
						GL11.glDrawElements(GL11.GL_TRIANGLES, vertsToDraw, GL11.GL_UNSIGNED_SHORT, 0);
					}
				}
			} else {
				shader.getUniform("offset").bindVec2(tmpV.set(1, 1));
				GL11.glDrawElements(GL11.GL_TRIANGLES, vertsToDraw, GL11.GL_UNSIGNED_SHORT, 0);
			}
		}

		if (overrideColour != null) {
			shader.getUniform("colourOverride").bindFloat(overrideColourMix);
			shader.getUniform("colourOverrideColour").bindVec4(overrideColour.x, overrideColour.y, overrideColour.z, overrideColour.w);
		} else {
			shader.getUniform("colourOverride").bindFloat(0.0f);
		}
		shader.getUniform("offset").bindVec2(0, 0);

		GL11.glDrawElements(GL11.GL_TRIANGLES, vertsToDraw, GL11.GL_UNSIGNED_SHORT, 0);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (!invalid) {
			vao.delete();
			vertices.delete();
			indicies.delete();
			vertices = null;
			indicies = null;
			vao = null;
		}
	}

	@Override
	public void invalidate() {
		invalid = true;
	}

}