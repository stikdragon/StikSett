package uk.co.stikman.sett.client.renderer;

import static uk.co.stikman.sett.SettApp.CHUNK_SIZE;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.ClientGame;
import uk.co.stikman.sett.FileSourceBatchClose;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.VoxelModel;
import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.gfx.Image;
import uk.co.stikman.sett.gfx.RenderTarget;
import uk.co.stikman.sett.gfx.Shader;
import uk.co.stikman.sett.gfx.Shader.Uniform;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.lwjgl.FrameBufferNative;
import uk.co.stikman.sett.gfx.lwjgl.FrameBufferNative.ColourModel;
import uk.co.stikman.sett.gfx.ui.EasingFloat;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.util.ScanlineConverter;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class GameView {
	private static final StikLog			LOGGER				= StikLog.getLogger(GameView.class);
	private static final float				ROOT3_2				= (float) (Math.sqrt(3.0) / 2.0);
	private static final int				MAX_SHADOWMAP_SIZE	= 4096;
	private static final int				PIXEL_SCALE			= 1;

	private Window3D						window;
	private ClientGame						game;

	private Vector3							viewOffset			= new Vector3();
	private Vector3							cameraPosition		= new Vector3();
	private EasingFloat						viewDist			= EasingFloat.fixedRate(50.0f, 10.0f);

	private Vector2i						viewport			= new Vector2i();
	private Vector2							rotation			= new Vector2();

	private Matrix4							mView				= new Matrix4();
	private Matrix4							mProj				= new Matrix4();
	private Matrix4							mModel				= new Matrix4();

	private Matrix3							tm1					= new Matrix3();
	private Matrix3							tm2					= new Matrix3();
	private Matrix4							tm3					= new Matrix4();
	private Vector3							tv1					= new Vector3();
	private Vector3							tv2					= new Vector3();
	private Vector2							tv3					= new Vector2();
	private Vector2							tv4					= new Vector2();
	private Vector4							tv5					= new Vector4();
	private Vector4							tv6					= new Vector4();

	private Map<ChunkKey, TerrainChunkMesh>	terrainChunks		= Collections.synchronizedMap(new HashMap<>());
	private Map<Object, SceneObject>		sceneObjects		= new HashMap<>();
	private Map<Object, VoxelMesh>			voxelMeshes			= new HashMap<>();
	private boolean							dragon;
	private int								lastY;
	private int								lastX;
	private int								dragbutton;
	private Image							imageTerrain;
	private Set<Vector2i>					visible				= new HashSet<>();

	private int								framecount			= 0;

	private Shader							terrainShader;
	private Shader							objectShader;
	private Shader							waterShader;

	private boolean							needShadowMapUpdate	= true;
	private FrameBufferNative				shadowmapbuf;
	private FrameBufferNative				renderbuf;
	private WaterPlane						waterPlane;
	private Image							imageWater;
	private float							time;
	private Vector2i						mouseLockedAt		= new Vector2i();
	private SelectionMarker					selectionMarker;

	private DebugRay						debugRay;
	private Ray								tray				= new Ray();
	private static final Plane				BASELINE_PLANE		= new Plane(new Vector3(0, 0, 1), new Vector3(0, 0, 0));
	private static final boolean			FORCE_ALL_VISIBLE	= true;
	private final ScanlineConverter			scanconverter		= new ScanlineConverter();
	private boolean							FORCE_CHUNK_TEST	= true;

	public GameView(Window3D window, ClientGame game) {
		this.window = window;
		this.game = game;
		time = 0.0f;
	}

	public void init() {

		try {
			window.setUseLinearTextures(true);
			viewOffset.set(0, 0, 0);
			cameraPosition.set(0, 0, 0);
			rotation.x = 0;
			rotation.y = -3.14159f / 3.0f;
			terrainShader = window.loadShader("terrain", game.getTextFile("terrain.frag"), game.getTextFile("terrain.vert"));
			objectShader = window.loadShader("voxelobject", game.getTextFile("voxelobject.frag"), game.getTextFile("voxelobject.vert"));
			waterShader = window.loadShader("water", game.getTextFile("water1.frag"), game.getTextFile("water1.vert"));
			try (FileSourceBatchClose files = new FileSourceBatchClose(game.getFileSource())) {
				imageTerrain = new Image(window, files.get("terrain.png"), "terrain");
				imageWater = new Image(window, files.get("water1.png"), "terrain");
			}

			int scale = 8;
			int mx = Math.max(game.getWorld().getWidth(), game.getWorld().getHeight());
			while (mx * scale > MAX_SHADOWMAP_SIZE && scale > 1)
				scale /= 2;
			shadowmapbuf = new FrameBufferNative(game.getWorld().getWidth() * scale, game.getWorld().getHeight() * scale, ColourModel.GRAYSCALE, true);
			renderbuf = new FrameBufferNative(512, 512, ColourModel.RGBA_DEPTH, false);

			VoxelMesh vm1 = new VoxelMesh(this, game.getModels().get(game.getWorld().getScenaryDef("caret").getVoxelModelInfo()));
			VoxelMesh vm2 = new VoxelMesh(this, game.getModels().get(game.getWorld().getScenaryDef("caret-outer").getVoxelModelInfo()));
			VoxelMesh vm3 = new VoxelMesh(this, game.getModels().get(game.getWorld().getScenaryDef("house1").getVoxelModelInfo()));
			VoxelMesh vm4 = new VoxelMesh(this, game.getModels().get(game.getWorld().getScenaryDef("castle").getVoxelModelInfo()));
			VoxelMesh vm5 = new VoxelMesh(this, game.getModels().get(game.getWorld().getScenaryDef("flag").getVoxelModelInfo()));
			selectionMarker = new SelectionMarker(this, vm1, vm2, vm3, vm4, vm5);

			//
			// pre-generate all the chunks in parallel.  calling generate 
			// is thread-safe
			//
			LOGGER.info("Generating initial terrain chunks...");
			ExecutorService exec = Executors.newFixedThreadPool(8);
			int cx = game.getWorld().getWidth() / CHUNK_SIZE;
			int cy = game.getWorld().getHeight() / CHUNK_SIZE;
			for (int y = 0; y < cy; ++y) {
				for (int x = 0; x < cx; ++x) {
					ChunkKey key = new ChunkKey();
					key.cy = y;
					key.cx = x;
					exec.submit(() -> generateTerrainChunk(game.getWorld().getTerrain(), key));
				}
			}
			exec.shutdown();
			if (!exec.awaitTermination(1, TimeUnit.MINUTES))
				throw new ResourceLoadError("Failed to created initial terrain chunks in time");
			LOGGER.info("Initial terrain chunks complete");

		} catch (IOException | ResourceLoadError | InterruptedException e) {
			throw new RuntimeException("Failed to initialise: " + e.getMessage(), e);
		}

	}

	public void render() {
		if (game == null || window == null || game.getWorld() == null)
			return;
		window.setRenderTarget(renderbuf.getRenderTarget());
		window.getRenderTarget().getDimensions(viewport);
		window.clear();

		if (needShadowMapUpdate)
			renderTerrainLightMap();

		mProj.makePerspective(40, (float) viewport.x / (float) viewport.y, 1.0f, 10000f);
		mView.makeTranslation(0, 0, -viewDist.get());
		mView.rotate(1, 0, 0, rotation.y);
		mView.rotate(0, 0, 1, rotation.x);
		mView.translate(cameraPosition);
		mModel.makeIdentity();

		//
		// work out what's visible
		//
		determineVisibleSet();

		//
		// Terrain first
		//
		long dt = System.currentTimeMillis();
		renderTerrain();

		//
		// Water flat
		//
		waterShader.use();
		bindStandardUniforms(waterShader);
		//	waterShader.getUniform("txt").bindTexture(imageWater.getTexture(), 0);
		imageWater.getTexture().bind(0);
		getWaterPlane().render();

		//
		// Now objects
		//
		objectShader.use();
		bindStandardUniforms(objectShader);
		Uniform uoff = objectShader.getUniform("offset");
		Matrix3 skew = SettApp.skewMatrix(new Matrix3());

		World world = game.getWorld();
		for (Vector2i v : visible) {
			int x0 = v.x * CHUNK_SIZE;
			int x1 = x0 + CHUNK_SIZE;
			int y0 = v.y * CHUNK_SIZE;
			int y1 = y0 + CHUNK_SIZE;
			for (int y = y0; y < y1; ++y) {
				for (int x = x0; x < x1; ++x) {
					IsNodeObject obj = world.getObjectAt(x, y);
					if (obj != null) {
						SceneObject m = findSceneObjectFor(obj);
						float h = world.getTerrain().get(x, y).getHeight();
						tv3.set(x, y);
						skew.multiply(tv3, tv4);
						uoff.bindVec3(tv4.x, tv4.y, h);
						m.render();
					}

				}
			}
		}

		selectionMarker.render(objectShader, skew);

		if (debugRay != null)
			debugRay.render(objectShader);

		window.setRenderTarget(window.getDefaultRenderTarget());
		window.clear();
		window.drawBuf(renderbuf, 0, 0, renderbuf.getWidth() * PIXEL_SCALE, renderbuf.getHeight() * PIXEL_SCALE);

		if (framecount++ == 0)
			LOGGER.info("Time to generate first frame: " + (System.currentTimeMillis() - dt) + "ms");

	}

	private void determineVisibleSet() {
		visible.clear();
		int w = viewport.x;
		int h = viewport.y;
		Vector2i a = null;
		Vector2i b = null;
		Vector2i c = null;
		Vector2i d = null;

		if (FORCE_ALL_VISIBLE) {
			int cx = game.getWorld().getTerrain().getWidth() / CHUNK_SIZE;
			int cy = game.getWorld().getTerrain().getHeight() / CHUNK_SIZE;
			Matrix4 m = tm3.copy(mProj).multiply(mView);
			for (int y = 0; y < cy; ++y)
				for (int x = 0; x < cx; ++x)
					visible.add(new Vector2i(x, y));
			return;
		}

		if (!FORCE_CHUNK_TEST) {
			Matrix3 skew = SettApp.skewMatrix(tm1).inverse();

			float extra = w * 0.15f;
			castWorld(tv3.set(-extra, -extra * 2), tray);
			if (BASELINE_PLANE.intersect(tray, tv1) != null) {
				skew.multiply(tv1, tv2);
				a = new Vector2i((int) tv2.x, (int) tv2.y);
			}
			castWorld(tv3.set(w + extra, -extra * 2), tray);
			if (BASELINE_PLANE.intersect(tray, tv1) != null) {
				skew.multiply(tv1, tv2);
				b = new Vector2i((int) tv2.x, (int) tv2.y);
			}
			castWorld(tv3.set(w + extra, h + extra), tray);
			if (BASELINE_PLANE.intersect(tray, tv1) != null) {
				skew.multiply(tv1, tv2);
				c = new Vector2i((int) tv2.x, (int) tv2.y);
			}
			castWorld(tv3.set(-extra, h + extra), tray);
			if (BASELINE_PLANE.intersect(tray, tv1) != null) {
				skew.multiply(tv1, tv2);
				d = new Vector2i((int) tv2.x, (int) tv2.y);
			}
		}

		if (a != null && b != null && c != null && d != null) {
			a.x = a.x / CHUNK_SIZE;
			a.y = a.y / CHUNK_SIZE;
			b.x = b.x / CHUNK_SIZE;
			b.y = b.y / CHUNK_SIZE;
			c.x = c.x / CHUNK_SIZE;
			c.y = c.y / CHUNK_SIZE;
			d.x = d.x / CHUNK_SIZE;
			d.y = d.y / CHUNK_SIZE;

			int ww = game.getWorld().getWidth() / CHUNK_SIZE;
			int wh = game.getWorld().getHeight() / CHUNK_SIZE;
			scanconverter.convert((x, y) -> {
				if (x < 0 || y < 0 || x >= ww || y >= wh)
					return;
				visible.add(new Vector2i(x, y));
			}, a, b, c, d);
		} else {
			// 
			// fall back to testing every chunk instead
			//
			Matrix3 skew = SettApp.skewMatrix(tm1);
			Vector3[] corners = new Vector3[8];
			for (int i = 0; i < 8; ++i)
				corners[i] = new Vector3();
			ChunkKey key = new ChunkKey();
			int cx = game.getWorld().getTerrain().getWidth() / CHUNK_SIZE;
			int cy = game.getWorld().getTerrain().getHeight() / CHUNK_SIZE;
			Matrix4 m = tm3.copy(mProj).multiply(mView);
			for (int y = 0; y < cy; ++y) {
				key.cy = y;
				for (int x = 0; x < cx; ++x) {
					//
					// test for visibility
					//
					corners[0].set(x, y, 0);
					corners[1].set(x + 1, y, 0);
					corners[2].set(x + 1, y + 1, 0);
					corners[3].set(x, y + 1, 0);
					corners[4].set(x, y, 10); // TODO: is 10 high enough?
					corners[5].set(x + 1, y, 10);
					corners[6].set(x + 1, y + 1, 10);
					corners[7].set(x, y + 1, 10);
					boolean pass = false;
					for (int i = 0; i < 8; ++i) {
						corners[i].x *= CHUNK_SIZE;
						corners[i].y *= CHUNK_SIZE;
						skew.multiply(corners[i], tv1);
						m.multiply(tv6.set(tv1), tv5);
						tv5.x /= tv5.w;
						tv5.y /= tv5.w;
						tv5.z /= tv5.w;
						if (tv5.x >= -1 && tv5.x <= 1 && tv5.y >= -1 && tv5.y <= 1) {
							pass = true;
							break;
						}
					}

					if (!pass)
						continue; // not visible

					visible.add(new Vector2i(x, y));
				}
			}
		}
	}

	private WaterPlane getWaterPlane() {
		if (waterPlane == null)
			waterPlane = new WaterPlane(this.window, game.getWorld());
		return waterPlane;
	}

	private void renderTerrainLightMap() {
		//
		// render world from above, all in black, from infinity (no perspective)
		//
		RenderTarget old = window.setRenderTarget(shadowmapbuf.getRenderTarget());
		World world = game.getWorld();
		mProj.makeOrtho(0, world.getWidth(), 0, world.getHeight(), 0, 1000);
		mView.makeIdentity();
		mModel.makeIdentity().translate(new Vector2(0.3f, 0.3f));

		window.clear(new Vector4(1, 0, 0, 1));

		objectShader.use();
		bindStandardUniforms(objectShader);
		Uniform uoff = objectShader.getUniform("offset");

		for (int y = 0; y < world.getHeight(); ++y) {
			for (int x = 0; x < world.getWidth(); ++x) {
				IsNodeObject obj = world.getObjectAt(x, y);
				if (obj != null) {
					SceneObject m = findSceneObjectFor(obj);
					float h = world.getTerrain().get(x, y).getHeight();
					tv4.set(x, y);
					uoff.bindVec3(tv4.x, tv4.y, h);
					m.render();
				}
			}
		}
		window.setRenderTarget(old);
		needShadowMapUpdate = false;
	}

	private SceneObject findSceneObjectFor(IsNodeObject obj) {
		SceneObject so = sceneObjects.get(obj);
		if (so == null)
			return generateSceneObject(obj);
		return so;
	}

	private SceneObject generateSceneObject(IsNodeObject obj) {
		//
		// find a voxel model for this
		//
		VoxelModel mdl = game.getModels().get(obj.getVoxelModelInfo());
		if (mdl == null)
			throw new NoSuchElementException("No model found: " + obj.getVoxelModelInfo() + " for NodeObject: " + obj);

		SceneObject so = new SceneObject(this, obj, getSceneMesh(mdl));
		sceneObjects.put(obj, so);
		return so;
	}

	private VoxelMesh getSceneMesh(VoxelModel mdl) {
		VoxelMesh mesh = voxelMeshes.get(mdl);
		if (mesh == null)
			voxelMeshes.put(mdl, mesh = new VoxelMesh(this, mdl));
		return mesh;
	}

	private void renderTerrain() {
		terrainShader.use();
		bindStandardUniforms(terrainShader);
		terrainShader.getUniform("txt").bindTexture(imageTerrain.getTexture(), 0);
		terrainShader.getUniform("shadmap").bindTexture(shadowmapbuf.getTexture(), 1);
		Uniform uoff = terrainShader.getUniform("offset");

		Terrain terrain = game.getWorld().getTerrain();

		imageTerrain.getTexture().bind(0);

		//
		// split this up into chunks 
		//
		Matrix3 skew = SettApp.skewMatrix(new Matrix3());
		ChunkKey key = new ChunkKey();
		int cx = terrain.getWidth() / CHUNK_SIZE;
		int cy = terrain.getHeight() / CHUNK_SIZE;
		int counter = 0;
		for (Vector2i v : visible) {
			key.cx = v.x;
			key.cy = v.y;
			++counter;
			TerrainChunkMesh mesh = terrainChunks.get(key);
			if (mesh == null)
				mesh = generateTerrainChunk(terrain, key);

			tv1.set(v.x, v.y, 0);
			tv1.multiply(CHUNK_SIZE);
			tv1 = skew.multiply(tv1, tv2);
			uoff.bindVec3(tv1);
			mesh.render();
		}
	}

	private void bindStandardUniforms(Shader s) {
		s.getUniform("view").bindMat4(mView);
		s.getUniform("proj").bindMat4(mProj);
		s.getUniform("model").bindMat4(mModel);
		if (s.hasUniform("globalLight"))
			s.getUniform("globalLight").bindVec3(SettApp.SUNLIGHT);
		if (s.hasUniform("time"))
			s.getUniform("time").bindFloat(time);
	}

	private TerrainChunkMesh generateTerrainChunk(Terrain terrain, ChunkKey key) {
		TerrainChunkMesh mesh = new TerrainChunkMesh(new ChunkKey(key), window);
		mesh.generate(terrain, key.cx, key.cy, CHUNK_SIZE, CHUNK_SIZE);
		terrainChunks.put(mesh.getKey(), mesh);
		return mesh;
	}

	public void update(float dt) {
		if (game == null || window == null || game.getWorld() == null)
			return;
		time += dt;
		viewDist.update(dt);
		if (selectionMarker != null)
			selectionMarker.update(dt);
	}

	public void setViewport(int w, int h) {
		viewport.set(w, h);

		if (renderbuf != null)
			renderbuf.destroy();
		int w2 = w / PIXEL_SCALE;
		int h2 = h / PIXEL_SCALE;
		renderbuf = new FrameBufferNative(w2, h2, ColourModel.RGBA_DEPTH, false);
	}

	public void mouseMove(int x, int y) {
		if (dragon) {
			int dx = x - lastX;
			int dy = y - lastY;
			lastX = mouseLockedAt.x;
			lastY = mouseLockedAt.y;
			window.setCursorPosition(lastX, lastY);

			if (dragbutton == 1) {
				Vector3 v = new Vector3(dx, -dy, 0.0f).multiply(0.2f);
				v = tm1.makeRotation(-rotation.x).multiply(v, new Vector3());
				cameraPosition.add(v);
			} else if (dragbutton == 2) {
				rotation.add(dx / 350.0f, dy / 350.0f);
				if (rotation.y > 0)
					rotation.y = 0;
				if (rotation.y < -2.0f)
					rotation.y = -2.0f; // this is coincidently quite a good endstop
			}

		}
	}

	public void mouseDown(int x, int y, int button) {
		if (button == 1 || button == 2) {
			mouseLockedAt.set(x, y);
			lastX = x;
			lastY = y;
			window.hideCursor();
			dragon = true;
			dragbutton = button;
		} else if (button == 0) {
			click(x, y);
		}
	}

	private void click(int x, int y) {
		//
		// cast ray into scene, see which terrain node is the nearest.  Terrain 
		// is skewed at this point, so need to transform the ray back into 
		// un-skewed space
		//
		x /= PIXEL_SCALE;
		y /= PIXEL_SCALE;
		Ray ray = castWorld(new Vector2(x, viewport.y - y), new Ray());

		SettApp.skewMatrix(tm1).inverse(tm2);
		tm2.multiply(tv1.copy(ray.point), ray.point);
		tm2.multiply(tv1.copy(ray.direction), ray.direction).normalize();

		Vector3 pos = game.getWorld().getTerrain().intersectRay(ray);

		if (pos != null) {
			LOGGER.info("Clicked on node: " + pos);
			Vector2i v = new Vector2i(Math.round(pos.x), Math.round(pos.y));
			selectionMarker.setPosition(v);
		}
	}

	//
	// castWorld uses quite a few temp things, and it's easier to just have
	// them here than try to use the general ones.  It wastes maybe 2kb of ram so i 
	// don't care
	//
	private Matrix4		cw_m1	= new Matrix4();
	private Vector4		cw_v1	= new Vector4();
	private Vector4		cw_v2	= new Vector4();
	private Vector4		cw_v3	= new Vector4();
	private Vector4		cw_v4	= new Vector4();
	private Vector2i	cw_vp	= new Vector2i();

	private Ray castWorld(Vector2 pos, Ray out) {
		Vector2i vp = renderbuf.getRenderTarget().getDimensions(cw_vp);

		//
		// start and end of ray in ndc space
		//
		Vector4 a = cw_v1.set(2 * pos.x / vp.x - 1, 2 * pos.y / vp.y - 1, -1, 1);
		Vector4 b = cw_v2.set(2 * pos.x / vp.x - 1, 2 * pos.y / vp.y - 1, 0, 1);

		//
		// transform back through the proj and view matrices
		//
		Matrix4 m = cw_m1.copy(mProj).multiply(mView).inverse();
		a = m.multiply(a, cw_v3);
		b = m.multiply(b, cw_v4);
		a.divide(a.w);
		b.divide(b.w);

		out.point.set(a);
		out.direction.set(Vector4.sub(b, a, new Vector4())).normalize();

		return out;
	}

	public void mouseUp(int x, int y, int button) {
		if (dragon) {
			window.unhideCursor();
			window.setCursorPosition(mouseLockedAt.x, mouseLockedAt.y);
			dragon = false;
		}
	}

	public void mouseWheel(int dy) {
		float f = viewDist.getTarget();
		f -= dy * 5.0f;
		if (f < 10.0f)
			f = 10.0f;
		if (f > 3000.0f)
			f = 3000.0f;
		viewDist.set(f);
	}

	public ClientGame getGame() {
		return game;
	}

	public Window3D getWindow() {
		return window;
	}

	public Vector2 getRotation() {
		return rotation;
	}

}
