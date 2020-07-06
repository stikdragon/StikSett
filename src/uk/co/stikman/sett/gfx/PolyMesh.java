package uk.co.stikman.sett.gfx;

import uk.co.stikman.utils.FloatList;
import uk.co.stikman.utils.IntList;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public abstract class PolyMesh {
	protected FloatList	verts			= new FloatList();
	protected IntList	tris			= new IntList();
	protected boolean	invalid			= true;
	protected int[]		frameOffsets	= null;				// if null then this isn't animated
	protected int[]		frameSizes		= null;				// if null then this isn't animated

	private int			indexcount;
	private boolean		destroyed;
	private Vector3		bounds;
	protected Window3D	window;

	public PolyMesh(Window3D owner) {
		this.window = owner;
	}

	public int addVert(float x, float y, float z, float u, float v, Vector3 normal, Vector4 c) {
		if (destroyed)
			throw new RuntimeException("Object is destroyed");
		int idx = verts.size() / 12;
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
		invalid = true;
		return idx;

	}

	public int addVert(float x, float y, float z, float u, float v, Vector4 c) {
		if (destroyed)
			throw new RuntimeException("Object is destroyed");
		int idx = verts.size() / 12;
		verts.add(x);
		verts.add(y);
		verts.add(z);

		verts.add(u);
		verts.add(v);

		verts.add(c.x);
		verts.add(c.y);
		verts.add(c.z);
		verts.add(c.w);

		verts.add(1); // normals
		verts.add(0);
		verts.add(0);
		invalid = true;
		return idx;
	}

	public void scaleVerts(float f) {
		for (int i = 0; i < verts.size(); ++i) {
			verts.set(i, verts.get(i) * f);
			++i;
			verts.set(i, verts.get(i) * f);
			++i;
			verts.set(i, verts.get(i) * f);
			i += 9;
		}
	}

	public void offsetVerts(float dx, float dy, float dz) {
		for (int i = 0; i < verts.size(); ++i) {
			verts.set(i, verts.get(i) + dx);
			++i;
			verts.set(i, verts.get(i) + dy);
			++i;
			verts.set(i, verts.get(i) + dz);
			i += 9;
		}
	}

	public int getCurrentVertIndex() {
		return verts.size() / 12;
	}
	
	public int getCurrentTriIndex() {
		return tris.size();
	}

	public void addVertArray(float[] data, int off, int len) {
		if (destroyed)
			throw new RuntimeException("Object is destroyed");
		verts.add(data, off, len);
	}

	public void addTri(int a, int b, int c) {
		if (destroyed)
			throw new RuntimeException("Object is destroyed");
		tris.add(a);
		tris.add(b);
		tris.add(c);
		invalid = true;
	}

	public void addTriArray(int[] data, int off, int len) {
		if (destroyed)
			throw new RuntimeException("Object is destroyed");
		tris.add(data, off, len);
	}

	public void prealloc(int verts, int tris) {
		this.verts = new FloatList(verts);
		this.tris = new IntList(tris);
	}

	protected abstract void rebuild();

	public abstract void render(int frame);

	public void destroy() {
		destroyed = true;
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

	public int getVertCount() {
		return verts.size() / 12;
	}

	public IntList getTris() {
		return tris;
	}

	public Window3D getWindow() {
		return window;
	}

	public int[] getFrameOffsets() {
		return frameOffsets;
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

}