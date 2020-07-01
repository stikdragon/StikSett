package uk.co.stikman.sett.gfx.lwjgl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.sett.gfx.PolyMesh;
import uk.co.stikman.sett.gfx.VAO;
import uk.co.stikman.sett.gfx.Window3D;

public class PolyMeshNative extends PolyMesh {

	private BufferNative	vertices;
	private BufferNative	indicies;
	private VAO				vao;
	private int				indexcount;
	private boolean			destroyed;

	public PolyMeshNative(Window3D owner) {
		super(owner);
	}

	@Override
	public void render(int frame) {
		if (destroyed)
			return;

		if (invalid)
			rebuild();

		vao.bind();
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexcount, GL11.GL_UNSIGNED_INT, frame * indexcount * 2);
	}

	@Override
	protected void rebuild() {
		if (destroyed)
			throw new RuntimeException("Object is destroyed");
		if (vao == null)
			vao = new VAONative();
		if (vertices == null)
			vertices = new BufferNative();
		if (indicies == null)
			indicies = new BufferNative();

		FloatBuffer faverts = BufferUtils.createFloatBuffer(verts.size());
		faverts.put(verts.list, 0, verts.size()).flip();
		IntBuffer iaidxes = BufferUtils.createIntBuffer(tris.size());
		iaidxes.put(tris.list, 0, tris.size()).flip();

		vao.bind();

		vertices.bind(GL15.GL_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, faverts, GL15.GL_STATIC_DRAW);
		int npos = window.getAttribLocation("vertexPosition");
		int ncolour = window.getAttribLocation("vertexColour");
		int nuv = window.getAttribLocation("vertexUV");
		int nnorm = window.getAttribLocation("vertexNormal");
		GL20.glEnableVertexAttribArray(npos);
		GL20.glEnableVertexAttribArray(ncolour);
		GL20.glEnableVertexAttribArray(nuv);
		GL20.glEnableVertexAttribArray(nnorm);
		GL20.glVertexAttribPointer(npos, 3, GL11.GL_FLOAT, false, 48, 0);
		GL20.glVertexAttribPointer(nuv, 2, GL11.GL_FLOAT, false, 48, 12);
		GL20.glVertexAttribPointer(ncolour, 4, GL11.GL_FLOAT, false, 48, 20);
		GL20.glVertexAttribPointer(nnorm, 3, GL11.GL_FLOAT, false, 48, 36);

		indicies.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iaidxes, GL15.GL_STATIC_DRAW);

		indexcount = tris.size();
		if (framecount > 1)
			indexcount /= framecount;
		invalid = false;
	}

	@Override
	public void destroy() {
		super.destroy();
		if (!invalid) {
			vertices.delete();
			indicies.delete();
			vertices = null;
			indicies = null;
		}
	}

}
