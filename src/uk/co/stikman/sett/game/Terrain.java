package uk.co.stikman.sett.game;

import java.security.acl.Owner;
import java.util.Random;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.client.renderer.Ray;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class Terrain {

	private static final StikLog	LOGGER		= StikLog.getLogger(Terrain.class);

	private static final float		MAX_HEIGHT	= 30.0f;

	private int						width;
	private int						height;
	private TerrainNode[]			nodes;
	private World					world;
	private static Vector3			tv			= new Vector3();
	private static Vector3			tv2			= new Vector3();
	private static Vector3			tv3			= new Vector3();
	private static Vector3			tv4			= new Vector3();
	private static Vector3			tv5			= new Vector3();

	public Terrain(World owner, int width, int height) {
		super();
		this.world = owner;
		if (width % SettApp.CHUNK_SIZE != 0 || height % SettApp.CHUNK_SIZE != 0)
			throw new IllegalArgumentException("Size must be multiple of CHUNK_SIZE (" + SettApp.CHUNK_SIZE + ")");

		this.width = width;
		this.height = height;
		nodes = new TerrainNode[width * height];
		for (int i = 0; i < nodes.length; ++i)
			nodes[i] = new TerrainNode(i % width, i / width);
	}

	public TerrainNode get(Vector2i v) {
		return get(v.x, v.y);
	}

	public TerrainNode get(int x, int y) {
		if (x < 0)
			x = 0;
		else if (x >= width)
			x = width - 1;
		if (y < 0)
			y = 0;
		else if (y >= height)
			y = height - 1;
		return nodes[y * width + x];
	}

	public void set(int x, int y, float altitude, int type, int owner) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return;
		TerrainNode n = nodes[y * width + x];

		if (altitude > MAX_HEIGHT)
			altitude = MAX_HEIGHT;
		n.setHeight(altitude);
		n.setType(type);
		n.setOwner(owner);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void generate(GenerateOptions opts) {
		LOGGER.info("Generating world...");
		Random rng = new Random(opts.getSeed());
		OpenSimplexNoise noise1 = new OpenSimplexNoise(opts.getSeed());
		OpenSimplexNoise noise2 = new OpenSimplexNoise(opts.getSeed() + 1);
		OpenSimplexNoise noise3 = new OpenSimplexNoise(opts.getSeed() + 2);
		OpenSimplexNoise noise4 = new OpenSimplexNoise(opts.getSeed() + 3);

		float scale = opts.getScale();

		Matrix3 xfm = SettApp.skewMatrix(new Matrix3());
		Vector2 tv = new Vector2();
		Vector2 tv2 = new Vector2();

		//
		// some initial gentle variation
		//
		for (int y = 0; y < height; ++y)
			for (int x = 0; x < width; ++x)
				set(x, y, (float) (noise1.eval(xfm.multiply(tv.set(x, y), tv2).multiply(scale)) + 0.707f) * opts.getBaseRippleSize(), 0, 0);

		//
		// some desert
		//
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				float f = (float) noise2.eval(xfm.multiply(tv.set(x, y), tv2).multiply(scale / 2.5f));
				if (f > opts.getDesertAmount())
					get(x, y).setType(1);
			}
		}

		//
		// put in some big lumps for mountains
		//
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				float f = (float) noise3.eval(xfm.multiply(tv.set(x, y), tv2).multiply(scale / 2.5f));
				if (f > opts.getMountainAmount()) {
					//
					// put in a big splodge
					//
					int r = rng.nextInt(6) + 3;
					int r2 = r * r;
					float sz = opts.getMountainSize();
					if (rng.nextFloat() > 0.8f) { // make some of them extra big
						sz *= rng.nextFloat() * 2.0f;
						r2 *= 2.0f;
					}
					for (int ax = x - r; ax < x + r; ++ax) {
						for (int ay = y - r; ay < y + r; ++ay) {
							float h = (float) ((noise3.eval(xfm.multiply(tv.set(ax, ay), tv2).multiply(scale)) + 1));
							h *= sz;
							xfm.multiply(tv.set(ax - x, ay - y), tv2);
							if (tv2.lengthSquared() <= r2)
								set(ax, ay, h, 2, 0);
						}
					}

				}
			}
		}

		//
		// same as mountains, but water instead
		//
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				float f = (float) noise4.eval(xfm.multiply(tv.set(x, y), tv2).multiply(scale * 1.5f));
				if (f > opts.getWaterAmount()) {
					int r = rng.nextInt(6) + 3;
					int r2 = r * r;
					for (int ax = x - r; ax < x + r; ++ax) {
						for (int ay = y - r; ay < y + r; ++ay) {
							xfm.multiply(tv.set(ax - x, ay - y), tv2);
							if (tv2.lengthSquared() <= r2)
								get(ax, ay).setHeight(-4); // we don't apply the water type here, that comes later
						}
					}
				}
			}
		}

		//
		// do some smoothing
		//
		for (int i = 0; i < 3; ++i) {
			float[] tmp = new float[width * height];
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					float h = 0.0f;
					h += get(x - 1, y - 1).getHeight();
					h += get(x, y - 1).getHeight();
					h += get(x + 1, y - 1).getHeight();
					h += get(x - 1, y).getHeight();
					h += get(x, y).getHeight();
					h += get(x + 1, y).getHeight();
					h += get(x - 1, y + 1).getHeight();
					h += get(x, y + 1).getHeight();
					h += get(x + 1, y + 1).getHeight();
					tmp[width * y + x] = h / 9.0f;
				}
			}
			int ptr = -1;
			for (int y = 0; y < height; ++y)
				for (int x = 0; x < width; ++x)
					get(x, y).setHeight(tmp[++ptr]);
		}
		float max = 0.0f;
		for (int y = 0; y < height; ++y)
			for (int x = 0; x < width; ++x)
				max = Math.max(max, get(x, y).getHeight());

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				TerrainNode n = get(x, y);
				if (n.getHeight() > max * opts.getSnowLevel())
					n.setType(3); // snow
				if (n.getHeight() < 0)
					n.setType(4); // water
			}
		}

		String[] rots = new String[] { "N", "S", "W", "E" };
		int m = width * height / 10;
		for (int i = 0; i < m; ++i) {
			TerrainNode n = get(rng.nextInt(getWidth()), rng.nextInt(getHeight()));
			if (n.getType() == 0) {
				switch (rng.nextInt(5)) {
				case 0:
				case 1:
					n.setObject(new Tree(nextId(), world.getScenaryDef("oaktree1-" + rots[rng.nextInt(4)])));
					break;
				case 2:
				case 3:
					n.setObject(new Tree(nextId(), world.getScenaryDef("pinetree1-" + rots[rng.nextInt(4)])));
					break;
				case 4:
					n.setObject(new Rock(nextId(), world.getScenaryDef("rock1-" + rots[rng.nextInt(4)])));
					break;
				}
			}
		}

		LOGGER.info("  Normals...");
		recalculateNormals();
		LOGGER.info("  done");
	}

	private int nextId() {
		return world.getGame().nextId();
	}

	public void recalculateNormals() {
		recalculateNormals(0, 0, width, height);

	}

	/**
	 * do just a range
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void recalculateNormals(int x0, int y0, int x1, int y1) {
		final int h = y1 - y0;
		int y2 = 0;
		final int y2_ = y2;

		Vector3 n = new Vector3();
		Vector3 v = new Vector3();
		for (int y = y2_; y < y2_ + h; ++y) {
			if (y >= y1) // happens if not % 8
				break;
			for (int x = x0; x < x1; ++x) {
				//
				// calculate normals
				//
				TerrainNode n0 = get(x, y);
				TerrainNode n1 = get(x + 1, y);
				TerrainNode n2 = get(x + 1, y + 1);
				TerrainNode n3 = get(x, y + 1);
				TerrainNode n4 = get(x - 1, y);
				TerrainNode n5 = get(x - 1, y - 1);
				TerrainNode n6 = get(x, y - 1);

				n.set(0, 0, 0);
				n.add(norm(n0, n1, n2, v));
				n.add(norm(n0, n2, n3, v));
				n.add(norm(n0, n3, n4, v));
				n.add(norm(n0, n4, n5, v));
				n.add(norm(n0, n5, n6, v));
				n.add(norm(n0, n6, n1, v));
				n0.setNormal(n.normalize());
			}
		}

	}

	private Vector3 norm(TerrainNode a, TerrainNode b, TerrainNode c, Vector3 res) {
		tv.set(a.getX(), a.getY(), a.getHeight());
		tv2.set(b.getX(), b.getY(), b.getHeight());
		tv3.set(c.getX(), c.getY(), c.getHeight());
		tv4.copy(tv2).sub(tv);
		tv5.copy(tv3).sub(tv);
		return Vector3.cross(tv4, tv5, res).normalize();
	}

	public Vector3 intersectRay(Ray ray) {
		//
		// first intersect with cubes formed by chunks across the surface, to work out which 
		// bits of the mesh we need to actually intersect with 
		// 
		final int csz = 16;
		if (width % csz != 0 || height % csz != 0) // TODO: i dno't like this
			throw new RuntimeException("Map size must be multiple of " + csz + " to allow intersection tests");

		int cx = width / csz;
		int cy = height / csz;

		Vector3 bestV = null;
		float bestD = Float.MAX_VALUE;

		Rectaloid r = new Rectaloid();
		for (int y = 0; y < cy; ++y) {
			for (int x = 0; x < cx; ++x) {
				r.set(x * csz, y * csz, 0, csz, csz, MAX_HEIGHT + 1.0f);// extend a little bit for safety
				if (r.intersect(ray, tv) != null) {
					//
					// full intersection on this mesh
					//
					Vector3 res = intersectChunk(x, y, csz, ray);
					if (res != null) {
						float f = res.distanceSq(ray.point);
						if (f < bestD) {
							bestD = f;
							if (bestV == null)
								bestV = new Vector3();
							bestV.copy(res);
						}
					}
				}
			}
		}

		//
		// return nearest node
		//
		if (bestV == null)
			return null;
		return new Vector3(bestV);
		//return new Vector2i(Math.round(bestV.x), Math.round(bestV.y));
	}

	private Vector3 intersectChunk(int cx, int cy, int sz, Ray ray) {
		int y0 = cy * sz;
		int y1 = y0 + sz - 1;
		int x0 = cx * sz;
		int x1 = x0 + sz - 1;

		Tri tri = new Tri();
		Vector3 v = new Vector3();

		//
		// we only look for a single intersection, so it's possible this 
		// will give the wrong intersection on very lumpy terrain that's been
		// viewed from an oblique angle
		//
		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				// R tri first
				tri.a.set(x, y, get(x, y).getHeight());
				tri.b.set(x + 1, y, get(x + 1, y).getHeight());
				tri.c.set(x + 1, y + 1, get(x + 1, y + 1).getHeight());

				Vector3 res = tri.intersect(ray, v);
				if (res != null)
					return res;

				// then L tri
				tri.a.set(x, y, get(x, y).getHeight());
				tri.b.set(x + 1, y + 1, get(x + 1, y + 1).getHeight());
				tri.c.set(x, y + 1, get(x, y + 1).getHeight());
				res = tri.intersect(ray, v);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	/**
	 * returns 7 elements, [0] is the requested node, then [1-6] are the
	 * neighbours. Will not return <code>null</code>, but will clamp to the
	 * edges instead, so some nodes will be repeated if you ask for an edge
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public TerrainNode[] getNeighbours(int x, int y) {
		TerrainNode[] arr = new TerrainNode[7];
		arr[0] = get(x, y);
		arr[1] = get(x, y + 1);
		arr[2] = get(x + 1, y + 1);
		arr[3] = get(x + 1, y);
		arr[4] = get(x, y - 1);
		arr[5] = get(x - 1, y - 1);
		arr[6] = get(x - 1, y);
		return arr;
	}

}
