package uk.co.stikman.sett.client.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.sett.VoxelModel;
import uk.co.stikman.sett.VoxelPalette;
import uk.co.stikman.sett.gfx.VAO;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.lwjgl.BufferNative;
import uk.co.stikman.sett.gfx.lwjgl.VAONative;
import uk.co.stikman.utils.FloatList;
import uk.co.stikman.utils.IntList;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class VoxelMesh {
	private VoxelModel				voxmodel;
	private VoxelPalette			palette;

	protected FloatList				verts		= new FloatList();
	protected IntList				tris		= new IntList();
	protected Window3D				window;
	private BufferNative			vertices;
	private BufferNative			indicies;
	private VAO						vao;
	private int[]					frameOffsets;
	private boolean					invalid		= true;
	private boolean					destroyed	= false;
	private int[]					frameSizes;

	private static final Vector3[]	NORMALS		= new Vector3[] { new Vector3(0, -1, 0), new Vector3(0, 1, 0), new Vector3(1, 0, 0), new Vector3(-1, 0, 0), new Vector3(0, 0, 1), new Vector3(0, 0, -1) };
	private static final int		NUM_ATTRS	= 13;

	public VoxelMesh(GameView gameView, VoxelModel voxmodel) {
		this.window = gameView.getWindow();
		this.voxmodel = voxmodel;
		this.palette = gameView.getGame().getVoxelPalette();

		generate();
	}

	private void generate() {
		if (vertices != null)
			destroy();

		//
		// generate mesh for this, we need to work out which faces are visible
		//
		verts = new FloatList();
		tris = new IntList();

		int[] offsets = new int[voxmodel.getFrames().size()];
		for (int f = 0; f < voxmodel.getFrames().size(); ++f) {
			offsets[f] = tris.size();
			for (int z = 0; z < voxmodel.getSizeZ(); ++z) {
				for (int y = 0; y < voxmodel.getSizeY(); ++y) {
					for (int x = 0; x < voxmodel.getSizeX(); ++x) {
						int vox = voxmodel.get(f, x, y, z);
						if (vox == -1)
							continue;
						float ovr = 0;
						if (vox == 233 || vox == 234 || vox == 235)
							ovr = 1;
						Vector4 colour = palette.get(vox);
						outputFace(voxmodel, f, 0, x, y, z, 0, -1, 0, colour, ovr);
						outputFace(voxmodel, f, 1, x, y, z, 0, 1, 0, colour, ovr);
						outputFace(voxmodel, f, 2, x, y, z, 1, 0, 0, colour, ovr);
						outputFace(voxmodel, f, 3, x, y, z, -1, 0, 0, colour, ovr);
						outputFace(voxmodel, f, 4, x, y, z, 0, 0, 1, colour, ovr);
						outputFace(voxmodel, f, 5, x, y, z, 0, 0, -1, colour, ovr);
					}
				}
			}
		}

		if (voxmodel.getFrames().size() > 1)
			setFrameOffsets(offsets);

		float ox = voxmodel.getSizeX() / 2.0f;
		float oy = voxmodel.getSizeY() / 2.0f;
		offsetVerts(-ox, -oy, 0);
		scaleVerts(0.05f);
		invalid = true;
	}

	private void scaleVerts(float f) {
		for (int i = 0; i < verts.size(); ++i) {
			verts.set(i, verts.get(i) * f);
			++i;
			verts.set(i, verts.get(i) * f);
			++i;
			verts.set(i, verts.get(i) * f);
			i += NUM_ATTRS - 3;
		}
	}

	private void offsetVerts(float dx, float dy, float dz) {
		for (int i = 0; i < verts.size(); ++i) {
			verts.set(i, verts.get(i) + dx);
			++i;
			verts.set(i, verts.get(i) + dy);
			++i;
			verts.set(i, verts.get(i) + dz);
			i += NUM_ATTRS - 3;
		}
	}

	private void outputFace(VoxelModel model, int frame, int face, int x, int y, int z, int dx, int dy, int dz, Vector4 colour, float ovr) {
		int vox = model.get(frame, x + dx, y + dy, z + dz);
		if (vox != -1) // face is occluded by another voxel 
			return;

		int n0, n1, n2, n3;

		switch (face) {
		case 0:
			n0 = addVert(x + 0, y + 0, z + 0, 0, 0, NORMALS[face], colour, ovr);
			n1 = addVert(x + 0, y + 0, z + 1, 1, 0, NORMALS[face], colour, ovr);
			n2 = addVert(x + 1, y + 0, z + 1, 1, 1, NORMALS[face], colour, ovr);
			n3 = addVert(x + 1, y + 0, z + 0, 0, 1, NORMALS[face], colour, ovr);
			break;
		case 1:
			n0 = addVert(x + 1, y + 1, z + 0, 0, 0, NORMALS[face], colour, ovr);
			n1 = addVert(x + 1, y + 1, z + 1, 1, 0, NORMALS[face], colour, ovr);
			n2 = addVert(x + 0, y + 1, z + 1, 1, 1, NORMALS[face], colour, ovr);
			n3 = addVert(x + 0, y + 1, z + 0, 0, 1, NORMALS[face], colour, ovr);
			break;
		case 2:
			n0 = addVert(x + 1, y + 0, z + 0, 0, 0, NORMALS[face], colour, ovr);
			n1 = addVert(x + 1, y + 0, z + 1, 1, 0, NORMALS[face], colour, ovr);
			n2 = addVert(x + 1, y + 1, z + 1, 1, 1, NORMALS[face], colour, ovr);
			n3 = addVert(x + 1, y + 1, z + 0, 0, 1, NORMALS[face], colour, ovr);
			break;
		case 3:
			n0 = addVert(x + 0, y + 1, z + 0, 0, 0, NORMALS[face], colour, ovr);
			n1 = addVert(x + 0, y + 1, z + 1, 1, 0, NORMALS[face], colour, ovr);
			n2 = addVert(x + 0, y + 0, z + 1, 1, 1, NORMALS[face], colour, ovr);
			n3 = addVert(x + 0, y + 0, z + 0, 0, 1, NORMALS[face], colour, ovr);
			break;
		case 4:
			n0 = addVert(x + 0, y + 0, z + 1, 0, 0, NORMALS[face], colour, ovr);
			n1 = addVert(x + 0, y + 1, z + 1, 1, 0, NORMALS[face], colour, ovr);
			n2 = addVert(x + 1, y + 1, z + 1, 1, 1, NORMALS[face], colour, ovr);
			n3 = addVert(x + 1, y + 0, z + 1, 0, 1, NORMALS[face], colour, ovr);
			break;
		case 5:
			n0 = addVert(x + 1, y + 0, z + 0, 0, 0, NORMALS[face], colour, ovr);
			n1 = addVert(x + 1, y + 1, z + 0, 1, 0, NORMALS[face], colour, ovr);
			n2 = addVert(x + 0, y + 1, z + 0, 1, 1, NORMALS[face], colour, ovr);
			n3 = addVert(x + 0, y + 0, z + 0, 0, 1, NORMALS[face], colour, ovr);
			break;
		default:
			return; // what
		}

		tris.add(n0);
		tris.add(n1);
		tris.add(n2);

		tris.add(n0);
		tris.add(n2);
		tris.add(n3);
	}

	private int addVert(float x, float y, float z, float u, float v, Vector3 normal, Vector4 c, float ovr) {
		int n = verts.size() / NUM_ATTRS;
		verts.add(x);
		verts.add(y);
		verts.add(z);
		verts.add(u);
		verts.add(v);
		verts.add(c.x);
		verts.add(c.y);
		verts.add(c.z);
		verts.add(c.w);
		verts.add(normal.x);
		verts.add(normal.y);
		verts.add(normal.z);
		verts.add(ovr); // colour override
		return n;
	}

	public void render(int frame) {
		if (destroyed)
			return;

		if (invalid)
			rebuild();

		vao.bind();

		int count;
		int off;
		if (frameOffsets != null) {
			count = frameSizes[frame];
			off = frameOffsets[frame];
		} else {
			count = tris.size();
			off = 0;
		}
		GL11.glDrawElements(GL11.GL_TRIANGLES, count, GL11.GL_UNSIGNED_INT, off * 4);
	}

	public void setFrameOffsets(int[] frameOffsets) {
		if (this.frameOffsets != null)
			throw new IllegalStateException("Frame data has already been set");
		this.frameOffsets = frameOffsets;
		frameSizes = new int[frameOffsets.length];
		for (int i = 0; i < frameSizes.length - 1; ++i)
			frameSizes[i] = frameOffsets[i + 1] - frameOffsets[i];
		frameSizes[frameSizes.length - 1] = tris.size() - frameOffsets[frameOffsets.length - 1];
	}

	private void rebuild() {
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
		int nover = window.getAttribLocation("vertexOverrideColour");
		GL20.glEnableVertexAttribArray(npos);
		GL20.glEnableVertexAttribArray(ncolour);
		GL20.glEnableVertexAttribArray(nuv);
		GL20.glEnableVertexAttribArray(nnorm);
		GL20.glEnableVertexAttribArray(nover);
		GL20.glVertexAttribPointer(npos, 3, GL11.GL_FLOAT, false, NUM_ATTRS * 4, 0);
		GL20.glVertexAttribPointer(nuv, 2, GL11.GL_FLOAT, false, NUM_ATTRS * 4, 12);
		GL20.glVertexAttribPointer(ncolour, 4, GL11.GL_FLOAT, false, NUM_ATTRS * 4, 20);
		GL20.glVertexAttribPointer(nnorm, 3, GL11.GL_FLOAT, false, NUM_ATTRS * 4, 36);
		GL20.glVertexAttribPointer(nover, 1, GL11.GL_FLOAT, false, NUM_ATTRS * 4, 48);

		indicies.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iaidxes, GL15.GL_STATIC_DRAW);

		invalid = false;
	}

	public void destroy() {
		destroyed = true;
		if (!invalid) {
			vertices.delete();
			indicies.delete();
			vertices = null;
			indicies = null;
			vao.delete();
			vao = null;
		}
	}

}
