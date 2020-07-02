package uk.co.stikman.sett;

import uk.co.stikman.sett.client.renderer.GameView;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.gfx.util.WindowInitError;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class SettApp {

	private static final int	WINDOW_W	= 1024;
	private static final int	WINDOW_H	= 768;

	public static final int		TRI_SIZE	= 32;
	public static final int		CHUNK_SIZE	= 16;
	public static final float	PATH_WIDTH	= 0.2f;
	public static final float	ROOT3_2		= (float) (Math.sqrt(3.0) / 2.0);
	public static final Vector3	SUNLIGHT	= new Vector3(9, 9, -1).normalize();

	public static void main(String[] args) {
		new SettApp().go();
	}

	private Window3DNative	window;
	private SettUI			ui;
	private GameView		view;
	private double			lastT;

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
			WorldParameters params = new WorldParameters(32);
			game.getWorld().generate(params);
			view = new GameView(window, game);
			view.init();
		} catch (ResourceLoadError e) {
			throw new WindowInitError(e);
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
