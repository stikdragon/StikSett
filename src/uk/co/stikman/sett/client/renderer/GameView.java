package uk.co.stikman.sett.client.renderer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.ClientGame;
import uk.co.stikman.sett.FileSourceBatchClose;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.VoxelModel;
import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.SceneryType;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.Tri;
import uk.co.stikman.sett.game.VoxelModelParams;
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
	private Vector3							tv1					= new Vector3();
	private Vector3							tv2					= new Vector3();
	private Vector2							tv3					= new Vector2();
	private Vector2							tv4					= new Vector2();
	private Vector4							tv5					= new Vector4();

	private Map<ChunkKey, TerrainChunkMesh>	terrainChunks		= Collections.synchronizedMap(new HashMap<>());
	private Map<Object, SceneObject>		sceneObjects		= new HashMap<>();
	private Map<Object, VoxelMesh>			voxelMeshes			= new HashMap<>();
	private boolean							dragon;
	private int								lastY;
	private int								lastX;
	private int								dragbutton;
	private Image							imageTerrain;

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
			selectionMarker = new SelectionMarker(this, new VoxelMesh(this, game.getModels().get(game.getWorld().getScenaryDef("caret").getVoxelModelInfo())));

			//
			// pre-generate all the chunks in parallel.  calling generate 
			// is thread-safe
			//
			LOGGER.info("Generating initial terrain chunks...");
			ExecutorService exec = Executors.newFixedThreadPool(8);
			int cx = game.getWorld().getWidth() / SettApp.CHUNK_SIZE;
			int cy = game.getWorld().getHeight() / SettApp.CHUNK_SIZE;
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
			renderTerrainShadowMap();

		mProj.makePerspective(40, (float) viewport.x / (float) viewport.y, 1.0f, 10000f);
		mView.makeTranslation(0, 0, -viewDist.get());
		mView.rotate(1, 0, 0, rotation.y);
		mView.rotate(0, 0, 1, rotation.x);
		mView.translate(cameraPosition);
		mModel.makeIdentity();

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
		for (int y = 0; y < world.getHeight(); ++y) {
			for (int x = 0; x < world.getWidth(); ++x) {
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
		
		selectionMarker.render(objectShader, skew);

		if (debugRay != null)
			debugRay.render(objectShader);

		window.setRenderTarget(window.getDefaultRenderTarget());
		window.clear();
		window.drawBuf(renderbuf, 0, 0, renderbuf.getWidth() * PIXEL_SCALE, renderbuf.getHeight() * PIXEL_SCALE);

		if (framecount++ == 0)
			LOGGER.info("Time to generate first frame: " + (System.currentTimeMillis() - dt) + "ms");

	}

	private WaterPlane getWaterPlane() {
		if (waterPlane == null)
			waterPlane = new WaterPlane(this.window, game.getWorld());
		return waterPlane;
	}

	private void renderTerrainShadowMap() {
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
		//		needShadowMapUpdate = false;
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
		int cx = terrain.getWidth() / SettApp.CHUNK_SIZE;
		int cy = terrain.getHeight() / SettApp.CHUNK_SIZE;
		for (int y = 0; y < cy; ++y) {
			key.cy = y;
			for (int x = 0; x < cx; ++x) {
				key.cx = x;
				TerrainChunkMesh mesh = terrainChunks.get(key);
				if (mesh == null)
					mesh = generateTerrainChunk(terrain, key);

				tv1.set(x, y, 0);
				tv1.multiply(SettApp.CHUNK_SIZE);
				tv1 = skew.multiply(tv1, tv2);
				uoff.bindVec3(tv1);
				mesh.render();
			}
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
		mesh.generate(terrain, key.cx, key.cy, SettApp.CHUNK_SIZE, SettApp.CHUNK_SIZE);
		terrainChunks.put(mesh.getKey(), mesh);
		return mesh;
	}

	public void update(float dt) {
		if (game == null || window == null || game.getWorld() == null)
			return;
		time += dt;
		viewDist.update(dt);
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
//
//				if (debugRay != null)
//					debugRay.destroy();
//				debugRay = new DebugRay(window);
//				debugRay.setOrigin(ray.point);
//				debugRay.setVector(ray.direction);
				
//				System.out.println(ray);

		SettApp.skewMatrix(tm1).inverse(tm2);
		tm2.multiply(tv1.copy(ray.point), ray.point);
		tm2.multiply(tv1.copy(ray.direction), ray.direction).normalize();

		Vector3 pos = game.getWorld().getTerrain().intersectRay(ray);
		LOGGER.info("Click: " + pos);

		if (pos != null) {
			Vector2i v = new Vector2i(Math.round(pos.x), Math.round(pos.y));
			selectionMarker.setPosition(tv1.set(v.x, v.y, game.getWorld().getTerrain().get(v).getHeight()));
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

}
