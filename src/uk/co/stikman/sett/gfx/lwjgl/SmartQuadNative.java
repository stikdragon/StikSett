package uk.co.stikman.sett.gfx.lwjgl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.sett.gfx.Buffer;
import uk.co.stikman.sett.gfx.Image;
import uk.co.stikman.sett.gfx.RectSprite;
import uk.co.stikman.sett.gfx.SmartQuad;
import uk.co.stikman.sett.gfx.StretchMode;
import uk.co.stikman.sett.gfx.VAO;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector4;


public class SmartQuadNative implements SmartQuad {

	private boolean		invalid	= true;
	private VAO			vao;
	private Buffer		vertices;
	private Buffer		indicies;
	private int			indexcount;
	private Vector4		colour	= VectorColours.WHITE;
	private Vector2		size;
	private RectSprite	sprite;
	private Image		image;
	private StretchMode	stretchMode;
	private Window3D window;

	public SmartQuadNative(Window3D window, RectSprite image, Vector2 size, StretchMode mode) {
		this.sprite = image;
		this.size = size;
		this.stretchMode = mode;
		this.window = window;

		//
		// find first image, should really be on layer 0
		//
		for (Image x : image.getLayers()) {
			if (x != null) {
				this.image = image.getImage(0);
				break;
			}
		}
	}

	@Override
	public void render() {
		if (invalid)
			rebuild();

		vao.bind();
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexcount, GL11.GL_UNSIGNED_SHORT, 0);
	}

	private int ptr;

	private void output(float[] verts, float x, float y, float u, float v) {
		verts[ptr++] = x;
		verts[ptr++] = y;

		verts[ptr++] = u;
		verts[ptr++] = v;

		verts[ptr++] = colour.x;
		verts[ptr++] = colour.y;
		verts[ptr++] = colour.z;
		verts[ptr++] = colour.w;
	}

	private void rebuild() {
		if (vao == null)
			vao = new VAONative();
		if (vertices == null)
			vertices = new BufferNative();
		if (indicies == null)
			indicies = new BufferNative();
		StretchMode sm = stretchMode;

		float Bx = size.x;
		float By = size.y;
		float Tx = sprite.getUvSize().x;
		float Ty = sprite.getUvSize().y;
		float R = sm.getMarginRight();
		float L = sm.getMarginLeft();
		float T = sm.getMarginTop();
		float B = sm.getMarginBottom();
		float Cx = Tx - R - L; // centre sizes
		float Cy = Ty - T - B;
		float BxMr = size.x - R;
		float TxMr = sprite.getWidth() - R;
		float ByMb = size.y - B;
		float TyMb = sprite.getHeight() - B;

		//
		// Number of rects across, down, area
		//
		int leftright = ((int) ((Bx - R - L) / Cx) + 1);
		int topbot = ((int) ((By - T - B) / Cy) + 1);
		int centre = topbot * leftright;
		int tris = 8 + leftright * 4 + topbot * 4 + centre * 2;

		float[] verts = new float[(leftright * 2 + topbot * 2 + centre + 4) * 4 * 8]; // 4 verts each, vert has X,Y,U,V,R,G,B,A
		short[] idxes = new short[tris * 3];
		for (int i = 0; i < idxes.length; i += 6) {
			int base = (i / 6) * 4;
			idxes[i] = (short) base;
			idxes[i + 1] = (short) (base + 1);
			idxes[i + 2] = (short) (base + 2);
			idxes[i + 3] = (short) base;
			idxes[i + 4] = (short) (base + 2);
			idxes[i + 5] = (short) (base + 3);
		}

		ptr = 0;

		//@formatter:off
		//
		// Corners first
		//
		output(verts, 0, 0, 0, 0);
		output(verts, L, 0, L, 0);
		output(verts, L, T, L, T);
		output(verts, 0, T, 0, T);

		output(verts, BxMr, 0, TxMr, 0);
		output(verts, Bx,   0, Tx,   0);
		output(verts, Bx,   T, Tx,   T);
		output(verts, BxMr, T, TxMr, T);
		
		output(verts, BxMr, ByMb, TxMr, TyMb);
		output(verts, Bx,   ByMb, Tx,   TyMb);
		output(verts, Bx,   By,   Tx,   Ty);
		output(verts, BxMr, By,   TxMr, Ty);
		
		output(verts, 0,   ByMb, 0,   TyMb);
		output(verts, L,   ByMb, L,   TyMb);
		output(verts, L,   By,   L,   Ty);
		output(verts, 0,   By,   0,   Ty);
		
		//
		// Top and bottom bars
		//
		for (int i = 0; i < leftright; ++i) {
			float xoff = i * Cx;
			if (i == leftright - 1) { // partial bar
				output(verts, xoff + L, 0, L,           0);
				output(verts, BxMr,     0, BxMr - xoff, 0);
				output(verts, BxMr,     T, BxMr - xoff, T);
				output(verts, xoff + L, T, L,           T);

				output(verts, xoff + L, ByMb, L,           TyMb);
				output(verts, BxMr,     ByMb, BxMr - xoff, TyMb);
				output(verts, BxMr,     By,   BxMr - xoff, Ty);
				output(verts, xoff + L, By,   L,           Ty);
			} else {
				output(verts, xoff + L,      0, L,      0);
				output(verts, xoff + L + Cx, 0, L + Cx, 0);
				output(verts, xoff + L + Cx, T, L + Cx, T);
				output(verts, xoff + L,      T, L,      T);
				
				output(verts, xoff + L,      ByMb, L,      TyMb);
				output(verts, xoff + L + Cx, ByMb, L + Cx, TyMb);
				output(verts, xoff + L + Cx, By,   L + Cx, Ty);
				output(verts, xoff + L,      By,   L,      Ty);				
			}
		}
		//
		// Left and right bars
		//
		for (int i = 0; i < topbot; ++i) {
			float yoff = i * Cy;
			if (i == topbot - 1) { // partial bar
				output(verts, 0, yoff + T,   0, T          );
				output(verts, 0, ByMb,       0, ByMb - yoff);
				output(verts, L, ByMb,       L, ByMb - yoff);
				output(verts, L, yoff + T,   L, T          );

				output(verts, BxMr, yoff + T,   TxMr, T          );
				output(verts, BxMr, ByMb,       TxMr, ByMb - yoff);
				output(verts, Bx, ByMb,       Tx,     ByMb - yoff);
				output(verts, Bx, yoff + T,   Tx,     T          );
				
			} else {
				output(verts, 0, yoff + T,      0, T     );
				output(verts, 0, yoff + T + Cy, 0, T + Cy);
				output(verts, L, yoff + T + Cy, L, T + Cy);
				output(verts, L, yoff + T,      L, T     );
				
				output(verts, BxMr, yoff + T,      TxMr, T     );
				output(verts, BxMr, yoff + T + Cy, TxMr, T + Cy);
				output(verts, Bx,   yoff + T + Cy, Tx,   T + Cy);
				output(verts, Bx,   yoff + T,      Tx,   T     );				
			}
		}
		
		
		//
		// Whole centres
		//
		for (int i = 0; i < leftright - 1; ++i) {
			float xoff = i * Cx;
			for (int j = 0; j < topbot - 1; ++j) {
				float yoff = j * Cy;
				output(verts, xoff + L,      yoff + T,      L,      T     );
				output(verts, xoff + L,      yoff + T + Cy, L,      T + Cy);
				output(verts, xoff + L + Cx, yoff + T + Cy, L + Cx, T + Cy);
				output(verts, xoff + L + Cx, yoff + T,      L + Cx, T     );
			}
		}
		
		//
		// Right partial centres
		//
		float xoff = (leftright - 1) * Cx;
		for (int i = 0; i < topbot - 1; ++i) {
			float yoff = i * Cy;
			output(verts, L + xoff, yoff + T,        L,           T   );
			output(verts, L + xoff, yoff + T + Cy,   L,           TyMb);
			output(verts, BxMr,     yoff + T + Cy,   BxMr - xoff, TyMb);
			output(verts, BxMr,     yoff + T,        BxMr - xoff, T   );
		}
		
		//
		// Bottom partial centres
		//
		float yoff = (topbot - 1) * Cy;
		for (int i = 0; i < leftright - 1; ++i) {
			xoff = i * Cx;
			output(verts, L + xoff,       yoff + T, L,       T          );
			output(verts, L + xoff,       ByMb,     L,       ByMb - yoff);
			output(verts, L + xoff + Cx,  ByMb,     L + Cx,  ByMb - yoff);
			output(verts, L + xoff + Cx,  yoff + T, L + Cx,  T          );
		}
		
		//
		// Bottom right centre
		//
		yoff = (topbot - 1) * Cy;
		xoff = (leftright - 1) * Cx;
		output(verts, L + xoff, yoff + T, L,       T               );
		output(verts, L + xoff, ByMb,     L,       ByMb - yoff     );
		output(verts, BxMr,     ByMb,     BxMr - xoff,  ByMb - yoff);
		output(verts, BxMr,     yoff + T, BxMr - xoff,  T          );
		
		
		//@formatter:on

		//
		// fix texture coords to 0..1 
		//
		Vector2 off = sprite.getUvOffset();
		float imageW = image.getWidth();
		float imageH = image.getHeight();
		for (int i = verts.length - 1; i >= 0; --i) {
			switch (i % 8) {
				case 2:
					verts[i] = (verts[i] + off.x) / imageW;
					break;
				case 3:
					verts[i] = (verts[i] + off.y) / imageH;
					break;
			}
		}

		indexcount = idxes.length;

		FloatBuffer faverts = BufferUtils.createFloatBuffer(verts.length);
		faverts.put(verts).flip();
		ShortBuffer iaidxes = BufferUtils.createShortBuffer(idxes.length);
		iaidxes.put(idxes).flip();

		vao.bind();
		vertices.bind(GL15.GL_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, faverts, GL15.GL_STATIC_DRAW);

		indicies.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iaidxes, GL15.GL_STATIC_DRAW);

		int npos = window.getAttribLocation("vertexPosition");
		int ncolour = window.getAttribLocation("vertexColour");
		int nuv = window.getAttribLocation("vertexUV");
		
		int offset = 0;
		int stride = 32;
		GL20.glVertexAttribPointer(npos, 2, GL11.GL_FLOAT, false, stride, offset);
		GL20.glEnableVertexAttribArray(npos);
		offset += 8;
		GL20.glVertexAttribPointer(nuv, 2, GL11.GL_FLOAT, false, stride, offset);
		GL20.glEnableVertexAttribArray(nuv);
		offset += 8;
		GL20.glVertexAttribPointer(ncolour, 4, GL11.GL_FLOAT, false, stride, offset);
		GL20.glEnableVertexAttribArray(ncolour);
		offset += 16;
		invalid = false;
	}

	@Override
	public void destroy() {
		if (vertices != null)
			vertices.delete();
		if (indicies != null)
			indicies.delete();
		if (vao != null)
			vao.delete();
	}

	public void setSize(Vector2 v) {
		this.size = v;
		invalid = true;
	}

	public RectSprite getSprite() {
		return sprite;
	}

	public Vector4 getColour() {
		return colour;
	}

	public void setColour(Vector4 colour) {
		this.colour = colour;
	}

}
