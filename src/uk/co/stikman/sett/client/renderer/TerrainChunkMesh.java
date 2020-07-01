package uk.co.stikman.sett.client.renderer;

import static uk.co.stikman.sett.SettApp.CHUNK_SIZE;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.gfx.PolyMesh;
import uk.co.stikman.sett.gfx.VAO;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.lwjgl.BufferNative;
import uk.co.stikman.sett.gfx.lwjgl.VAONative;
import uk.co.stikman.utils.FloatList;
import uk.co.stikman.utils.IntList;
import uk.co.stikman.utils.Utils;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class TerrainChunkMesh {
	private static final StikLog	LOGGER			= StikLog.getLogger(TerrainChunkMesh.class);
	private static final int		VERT_ITEM_COUNT	= 22;
	private static final int		VARIATIONS_SIZE	= 1000;

	protected FloatList				verts			= new FloatList();
	protected IntList				tris			= new IntList();
	protected boolean				invalid			= true;
	private int						indexcount;
	private boolean					destroyed;
	private Vector3					bounds;
	protected int					framecount		= -1;										// -1 means not animated, draw everything
	protected Window3D				window;
	private BufferNative			vertices;
	private BufferNative			indicies;
	private VAO						vao;

	private int						chunkX;
	private int						chunkY;
	private ChunkKey				key;
	private Vector4					tv4				= new Vector4();

	private Vector4					c1				= new Vector4();
	private Vector4					c2				= new Vector4();
	private Vector4					c3				= new Vector4();
	private Vector4					c4				= new Vector4();
	private Vector4					c5				= new Vector4();
	private Random					rng				= new Random();

	private static final float[]	UV_VARIATIONS	= new float[VARIATIONS_SIZE * 2];
	private static final Vector4	DEEP_BLUE		= new Vector4(0, 0, 0.5f, 1.0f);

	static {
		Random rng = new Random(1);
		for (int i = 0; i < UV_VARIATIONS.length; ++i) {
			int n = rng.nextInt(4);
			UV_VARIATIONS[i] = n / 5.0f + 0.1f;
		}
	}

	public TerrainChunkMesh(ChunkKey key, Window3D window) {
		this.key = key;
		this.chunkX = key.cx;
		this.chunkY = key.cy;
		this.window = window;
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkY() {
		return chunkY;
	}

	public ChunkKey getKey() {
		return key;
	}

	public void generate(Terrain terr, int cx, int cy, int sz, int sy) {
		if (vertices != null)
			destroy();

		int n = CHUNK_SIZE * CHUNK_SIZE * 20 * VERT_ITEM_COUNT;
		int m = CHUNK_SIZE * CHUNK_SIZE * 2 * 22 * 3;
		verts = new FloatList(n);
		tris = new IntList(m);

		LOGGER.info("Generating chunk " + key);

		Vector2 v1 = new Vector2();
		Vector2 v2 = new Vector2();
		Vector2 v3 = new Vector2();
		Vector2 v4 = new Vector2();
		Vector2 tv = new Vector2();
		Vector3 tv5 = new Vector3();
		Vector3 tv6 = new Vector3();

		Matrix3 xfm = SettApp.skewMatrix(new Matrix3());
		int ox = key.cx * CHUNK_SIZE;
		int oy = key.cy * CHUNK_SIZE;
		for (int y = 0; y < CHUNK_SIZE; ++y) {
			for (int x = 0; x < CHUNK_SIZE; ++x) {
				//
				// work out what it is frmo the terrain
				//
				v1.set(x, y);
				xfm.multiply(v1, v2);
				float tx = v2.x;
				float ty = v2.y;

				TerrainNode node0 = terr.get(ox + x, oy + y);
				TerrainNode node1 = terr.get(ox + x + 1, oy + y);
				TerrainNode node2 = terr.get(ox + x + 1, oy + y + 1);
				TerrainNode node3 = terr.get(ox + x, oy + y + 1);

				//
				// work out which quad represents this combo
				//
				//@formatter:off
				int key = MapQuad.key(
						node0.getFlag() != null, 
						node1.getFlag() != null, 
						node2.getFlag() != null, 
						node3.getFlag() != null, 
						node0.getRoad(1) != null,
						node0.getRoad(0) != null,
						node1.getRoad(2) != null,
						node3.getRoad(0) != null,
						node0.getRoad(2) != null
				);
				//@formatter:on
				MapQuad mq = MapQuad.quads[key];

				//
				// copy the mesh from MapQuad
				//
				Vector4 col = new Vector4(1, 1, 1, 1);
				float h0 = node0.getHeight();
				float h1 = node1.getHeight();
				float h2 = node2.getHeight();
				float h3 = node3.getHeight();
				int offset = verts.size() / VERT_ITEM_COUNT;

				for (int i = 0; i < MapQuad.verts.length; ++i) {

					float nx = MapQuad.verts[i++];
					float ny = MapQuad.verts[i++];

					float vx = MapQuad.verts[i++];
					float vy = MapQuad.verts[i++];
					float bf0 = MapQuad.verts[i++];
					float bf1 = MapQuad.verts[i++];
					float bf2 = MapQuad.verts[i++];
					float bf3 = MapQuad.verts[i];

					//
					// interpolate height between verts to work out the Z component
					//
					float h = h0 * bf0 + h1 * bf1 + h2 * bf2 + h3 * bf3;

					tv5.set(0, 0, 0);
					tv5.addMult(node0.getNormal(), bf0);
					tv5.addMult(node1.getNormal(), bf1);
					tv5.addMult(node2.getNormal(), bf2);
					tv5.addMult(node3.getNormal(), bf3);

					c1.copy(col);
					if (h < 0) {
						c2.copy(c1);
						Vector4.lerp(DEEP_BLUE, c1, Utils.clamp(-h / 3.f, 0, 1), c1);
					}
					//c1.multiply(0.3f + Utils.clamp(1.5f * Math.abs(tv5.dot(sunlight)), 0, 1));
					verts.add(vx + tx); // xyz
					verts.add(vy + ty);
					verts.add(h);

					//
					// pick a different section of the texture map each time
					//
					float u = vx;
					float v = vy;
					n = (y * CHUNK_SIZE + x) % (UV_VARIATIONS.length / 2);
					u *= 0.2f;
					v *= 0.2f;
					u += UV_VARIATIONS[n * 2];
					v += UV_VARIATIONS[n * 2 + 1];
					u += 0.33333f;
					v += 0.33333f;
					u *= 0.66667f;
					v *= 0.66667f;
					verts.add(u);
					verts.add(v);

					verts.add(c1.x); // colour
					verts.add(c1.y);
					verts.add(c1.z);
					verts.add(1); // opacity

					verts.add(node0.getType()); // texture IDs for four corners
					verts.add(node1.getType());
					verts.add(node2.getType());
					verts.add(node3.getType());

					verts.add(bf0); // interpolated texture weights
					verts.add(bf1);
					verts.add(bf2);
					verts.add(bf3);

					verts.add(tv5.x); // normals
					verts.add(tv5.y);
					verts.add(tv5.z);

					float smbx = (ox + x + nx) / terr.getWidth();
					float smby = (oy + y + ny) / terr.getHeight();

					verts.add(smbx);
					verts.add(smby);
				}

				//
				// tris
				//
				for (int tri : mq.tris)
					tris.add(tri + offset);
			}
		}

		invalid = true;

	}

	public void render() {
		if (destroyed)
			return;

		if (invalid)
			rebuild();

		vao.bind();
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexcount, GL11.GL_UNSIGNED_INT, 0);
	}

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
		int ntfact = window.getAttribLocation("tfact");
		int ntxt = window.getAttribLocation("txt");
		int nnorm = window.getAttribLocation("vertexNormal");
		int nsmb = window.getAttribLocation("shadowUV");
		GL20.glEnableVertexAttribArray(npos);
		GL20.glEnableVertexAttribArray(ncolour);
		GL20.glEnableVertexAttribArray(nuv);
		GL20.glEnableVertexAttribArray(ntfact);
		GL20.glEnableVertexAttribArray(ntxt);
		GL20.glEnableVertexAttribArray(nnorm);
		GL20.glEnableVertexAttribArray(nsmb);

		// we can't support anything other than floats here, since it's fiddly
		// in java to interleave arrays
		GL20.glVertexAttribPointer(npos, 3, GL11.GL_FLOAT, false, 88, 0);
		GL20.glVertexAttribPointer(nuv, 2, GL11.GL_FLOAT, false, 88, 12);
		GL20.glVertexAttribPointer(ncolour, 4, GL11.GL_FLOAT, false, 88, 20);
		GL20.glVertexAttribPointer(ntxt, 4, GL11.GL_FLOAT, false, 88, 36);
		GL20.glVertexAttribPointer(ntfact, 4, GL11.GL_FLOAT, false, 88, 52);
		GL20.glVertexAttribPointer(nnorm, 3, GL11.GL_FLOAT, false, 88, 68);
		GL20.glVertexAttribPointer(nsmb, 2, GL11.GL_FLOAT, false, 88, 80);

		indicies.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iaidxes, GL15.GL_STATIC_DRAW);

		indexcount = tris.size();
		if (framecount > 1)
			indexcount /= framecount;
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

	@Override
	public String toString() {
		return "PolyMesh";
	}

	public float getWidth() {
		if (bounds == null)
			calcBounds();
		return bounds.x;
	}

	public float getHeight() {
		if (bounds == null)
			calcBounds();
		return bounds.y;
	}

	private void calcBounds() {
		bounds = new Vector3();
		for (int i = 0; i < verts.size();) {
			float x = verts.get(i++);
			float y = verts.get(i++);
			float z = verts.get(i++);
			++i;
			++i;
			if (x > bounds.x)
				bounds.x = x;
			if (y > bounds.y)
				bounds.y = y;
			if (z > bounds.z)
				bounds.z = z;
		}
	}

	/**
	 * Calling this divides <code>indexcount</code> by framecount. When you call
	 * {@link #render(int)} you need to specify a frame number, which becomes an
	 * offset
	 * 
	 * @param framecount
	 */
	public void setFrameCount(int framecount) {
		if (indexcount > 0)
			throw new IllegalStateException("Mesh has already been generated, cannot set framecount");
		this.framecount = framecount;
	}

	public int getVertCount() {
		return verts.size() / VERT_ITEM_COUNT;
	}

	public IntList getTris() {
		return tris;
	}

	public Window3D getWindow() {
		return window;
	}

}
