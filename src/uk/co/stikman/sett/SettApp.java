package uk.co.stikman.sett;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.co.stikman.sett.client.renderer.GameView;
import uk.co.stikman.sett.game.Flag;
import uk.co.stikman.sett.game.Player;
import uk.co.stikman.sett.game.Road;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.gfx.util.WindowInitError;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class SettApp {

	private static final int	WINDOW_W	= 1024;
	private static final int	WINDOW_H	= 768;

	public static final int		TRI_SIZE	= 32;
	public static final int		CHUNK_SIZE	= 16;
	public static final float	PATH_WIDTH	= 0.4f;
	public static final float	ROAD_DEPTH	= 0.03f;
	public static final float	ROOT3_2		= (float) (Math.sqrt(3.0) / 2.0);
	public static final Vector3	SUNLIGHT	= new Vector3(9, 9, -1).normalize();

	public static void main(String[] args) {
		new SettApp().go();
	}

	private Window3DNative	window;
	private SettUI			ui;
	private GameView		view;
	private double			lastT;
	private Player			p1;
	private Player p2;
	private Player p3;

	private void go() {
		try {
			window = new Window3DNative(WINDOW_W, WINDOW_H, true);
			window.setTitle("Thing");
			window.setOnFrame(this::onFrame);
			window.setOnInit(this::onInit);
			window.setOnResize(this::onResize);
			//			window.setOnKeyPress(this::onKeyPress);
			//			window.setOnKeyCode(this::onKeyCode);
			window.setOnMouseMove(this::onMouseMove);
			window.setOnMouseDown(this::onMouseDown);
			window.setOnMouseUp(this::onMouseUp);
			window.setOnMouseWheel(this::onMouseWheel);

			RenderTextOptions rto = new RenderTextOptions(RenderTextOptions.DEFAULT);
			rto.setOutlineMode(OutlineMode.OUTLINE);
			rto.setOutlineBlendFactor(0.75f);
			rto.setOutlineColour(new Vector4(0, 0, 0, 1));
			window.setDefaultTextRenderOptions(rto);
			window.start();

		} catch (WindowInitError e) {
			e.printStackTrace();
		}

	}

	private void onInit() throws WindowInitError {
		try {
			ui = new SettUI(this);

			ClientGame game = new ClientGame(this);
			game.setWorld(new World());
			game.loadResources();
			WorldParameters params = new WorldParameters(14);
			game.getWorld().generate(params);
			
			p1 = new Player(game, "Player1", VectorColours.HSLtoRGB(new Vector3(0, 1.0f,  0.6f)));
			p2 = new Player(game, "Player2", VectorColours.HSLtoRGB(new Vector3(0.75f, 1.0f,  0.7f)));
			p3 = new Player(game, "Player3", VectorColours.HSLtoRGB(new Vector3(0.6f, 1.0f,  0.6f)));
			randomRoads(game);
			randomFlags(game);
			view = new GameView(window, game);
			view.init();
		} catch (ResourceLoadError e) {
			throw new WindowInitError(e);
		}
	}

	private void randomFlags(ClientGame game) {
		Random rng = new Random();
		for (int y = 0; y < game.world.getHeight(); ++y) {
			for (int x = 0; x < game.world.getWidth(); ++x) {
				TerrainNode n = game.world.getTerrain().get(x, y);
				List<Road> lst = game.world.getRoadsAt(n, new ArrayList<>());
				if (lst.size() > 2 || lst.size() == 1) {
					Player p = p1;
					switch (rng.nextInt(3)) {
					case 0: p = p1; break;
					case 1: p = p2; break;
					case 2: p = p3; break;
					}
					n.setObject(new Flag(game, p));
				}
			}
		}
	}

	private void randomRoads(ClientGame game) {
		Random rng = new Random();
		int n = 40 * game.world.getWidth() * game.world.getHeight() / 50000;
		for (int k = 0; k < n; ++k) {
			List<Vector2i> nodes = new ArrayList<>();
			float dir = 0.0f;
			int chg = 0;
			int x = rng.nextInt(game.world.getWidth());
			int y = rng.nextInt(game.world.getHeight());
			for (int i = 0; i < 100; ++i) {
				if (chg-- < 0) {
					chg = rng.nextInt(2);
					dir += rng.nextFloat() - 0.5f;
				}
				int dx = (int) Math.round(Math.sin(dir));
				int dy = (int) Math.round(Math.cos(dir));
				if (dx == 0 && dy == 0)
					continue;
				if ((dx == -1 && dy == 1) || (dx == 1 && dy == -1))
					continue; // illegal shape

				x += dx;
				y += dy;
				Vector2i nuw = new Vector2i(x, y);
				nodes.add(nuw);
			}
			game.addRoad(nodes);
		}
	}

	private void onResize(int w, int h) {
		ui.handleResize(w, h);
		view.setViewport(w, h);
	}

	private void onFrame(double time) {

		double dt = 0;
		if (lastT > 0)
			dt = time - lastT;
		lastT = time;
		dt /= 1e9;

		//
		// Do update() then render()
		//
		window.clear();
		update((float) dt);
		render();
	}

	private void onMouseMove(int x, int y) {
		ui.handleMouseMove(x, y);
		view.mouseMove(x, y);
	}

	private void onMouseDown(int x, int y, int button) {
		ui.handleMouseDown(x, y, button);
		view.mouseDown(x, y, button);

	}

	private void onMouseUp(int x, int y, int button) {
		ui.handleMouseUp(x, y, button);
		view.mouseUp(x, y, button);
	}

	private void onMouseWheel(int x, int y, int dy) {
		view.mouseWheel(dy);
	}

	private void render() {
		if (view == null)
			return;
		window.clear();
		view.render();
	}

	private void update(float dt) {
		if (view == null)
			return;
		view.update(dt);
	}

	public Window3DNative getWindow() {
		return window;
	}

	public static Matrix3 skewMatrix(Matrix3 m) {
		m.makeIdentity();

		//
		// scaling the Y axis is technically correct, but original settlers 
		// only did the skew, as far as i can tell.  This means the hexagons
		// aren't regular
		//
		//		m.scale(1.0f, ROOT3_2); 
		m.skew(0, -0.5f);
		return m;
	}

}
