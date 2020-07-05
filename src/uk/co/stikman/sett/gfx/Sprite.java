package uk.co.stikman.sett.gfx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.util.Coord;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.utils.IntList;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector4;

public abstract class Sprite {
	private final String		name;
	private final GameResources	owner;
	private Image[]				images;
	private Vector2i[]			layerOffsets;
	private List<Coord>			vertices	= new ArrayList<>();
	private PolyMesh			mesh		= null;
	private Vector2				bounds;
	private boolean				animated	= false;
	private List<Frame>			frames;
	private Vector2i			tmpVi		= new Vector2i();
	private float				totalDuration;
	private boolean				noclip		= false;
	private Set<String>			tags		= new HashSet<>();

	public List<Coord> getVertices() {
		return vertices;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param gameResources
	 * @param name
	 */
	public Sprite(GameResources owner, String name) {
		super();
		this.owner = owner;
		this.name = name;
		images = new Image[owner.getLayerCount()];
		layerOffsets = new Vector2i[owner.getLayerCount()];
	}

	public void setImage(int layer, Image image, Vector2i offset) {
		this.images[layer] = image;
		this.layerOffsets[layer] = offset;
	}

	public Image getImage(int layer) {
		return images[layer];
	}

	public Vector2i getLayerOffset(int layer) {
		return layerOffsets[layer];
	}

	protected void addVertexInt(Coord c) {
		if (mesh != null)
			throw new IllegalStateException("Cannot modify a Sprite after it has been rendered");
		vertices.add(c);
	}

	public PolyMesh getMesh() {
		if (mesh == null)
			rebuildMesh();
		return mesh;
	}

	private void rebuildMesh() {
		if (mesh != null)
			throw new IllegalStateException("Mesh already created");
		mesh = owner.getWindow().createPolyMesh();

		List<Frame> frames;
		if (animated) {
			frames = this.frames;
		} else {
			frames = new ArrayList<>();
			frames.add(new Frame(0, 0, tmpVi.set(0, 0)));
		}

		int framecount = frames.size();

		//
		// triangulate the poly
		//
		EarClippingTriangulator ect = new EarClippingTriangulator();
		float[] verts = new float[vertices.size() * 2 * framecount];
		int i = 0;

		//
		// find an image on any layer (they should all be the same size)
		//
		Image img = images[i];
		while (img == null && i < images.length)
			img = images[i++];
		if (img == null)
			return;

		i = 0;
		for (Frame f : frames) {
			for (Coord c : vertices) {
				verts[i++] = c.x;
				verts[i++] = c.y;
				mesh.addVert(c.x, c.y, 0, (img.getImgU() * c.u + f.getImageoffset().x) / img.getWidth(), (img.getImgV() * c.v + f.getImageoffset().y) / img.getHeight(), VectorColours.WHITE);
			}
		}

		IntList tris = ect.computeTriangles(verts, 0, vertices.size() * 2);
		for (int nf = 0; nf < frames.size(); ++nf) {
			int a = nf * vertices.size();
			for (int j = 0; j < tris.size();) {
				int n1 = tris.get(j++);
				int n2 = tris.get(j++);
				int n3 = tris.get(j++);
				mesh.addTri(n1 + a, n2 + a, n3 + a);
			}
		}
		throw new RuntimeException("Reimplement this for new frame method");
//		mesh.setFrameCount(framecount);

	}

	public Vector2 getBounds() {
		if (bounds == null) {
			float x = 0f;
			float y = 0f;

			for (Coord v : vertices) {
				x = Math.max(x, v.x);
				y = Math.max(y, v.y);
			}
			bounds = new Vector2();
			bounds.x = x;
			bounds.y = y;
		}
		return bounds;
	}

	public void invalidateMesh() {
		if (mesh != null)
			mesh.destroy();
		mesh = null;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean hasLayer(int layer) {
		return images[layer] != null;
	}

	public void setAnimated(boolean b) {
		this.animated = b;
	}

	public boolean isAnimated() {
		return animated;
	}

	public void addFrame(int duration, Vector2i imageoffset) {
		if (frames == null)
			frames = new ArrayList<Frame>();
		frames.add(new Frame(frames.size(), duration, imageoffset));
		this.totalDuration = 0.0f;
		for (Frame f : frames)
			totalDuration += f.getDuration();
		totalDuration /= 1000.0f;
	}

	/**
	 * You can pass -1 for w and h, which will just use the sprite's default
	 * size
	 * 
	 * @param window
	 * @param layer
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param angle
	 * @param time
	 * @param colour
	 */
	public void renderLayer(Window3DNative window, int layer, int x, int y, int w, int h, float angle, float time, Vector4 colour) {
		if (!hasLayer(layer))
			return;
		Vector2 b = getBounds();
		if (w == -1)
			w = (int) b.x;
		if (h == -1)
			h = (int) b.y;

		float sx = (float) w / b.x;
		float sy = (float) h / b.y;

		PolyMesh msh = getMesh();
		if (animated && totalDuration > 0) {
			//
			// Work out which frame to render
			//
			int n = (int) (time / totalDuration);
			time -= n * totalDuration;
			float a = 0;
			Frame best = frames.get(0);
			for (Frame f : frames) {
				if (a > time)
					break;
				best = f;
				a += (float) (f.getDuration() / 1000.0f);
			}
			window.drawMesh(msh, getImage(layer), x, y, sx, sy, angle, colour, best.getIndex());
		} else {
			window.drawMesh(msh, getImage(layer), x, y, sx, sy, angle, colour, 0);
		}
	}

	public void renderLayer(Window3DNative window, int layer, Rect rect, int angle, int time, Vector4 colour) {
		renderLayer(window, layer, (int) rect.x, (int) rect.y, (int) rect.w, (int) rect.h, angle, time, colour);
	}

	public GameResources getOwner() {
		return owner;
	}

	public boolean isNoclip() {
		return noclip;
	}

	public void setNoclip(boolean noclip) {
		this.noclip = noclip;
	}

	public Image[] getLayers() {
		return images;
	}

	public void addTags(String[] tags) {
		for (String s : tags)
			this.tags.add(s);
	}

	public Set<String> getTags() {
		return tags;
	}

	public boolean hasTag(String s) {
		return tags.contains(s);
	}

}
