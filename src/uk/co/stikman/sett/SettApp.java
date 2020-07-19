package uk.co.stikman.sett;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.client.renderer.BaseView;
import uk.co.stikman.sett.client.renderer.GameView;
import uk.co.stikman.sett.conn.GameConnection;
import uk.co.stikman.sett.conn.PendingNetworkOp;
import uk.co.stikman.sett.conn.Response;
import uk.co.stikman.sett.conn.ResponseHandler;
import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gameevents.GameEvent;
import uk.co.stikman.sett.gfx.BlendMode;
import uk.co.stikman.sett.gfx.GameResources;
import uk.co.stikman.sett.gfx.RenderTarget;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.lwjgl.FrameBufferNative;
import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.lwjgl.FrameBufferNative.ColourModel;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.text.WordWrap;
import uk.co.stikman.sett.gfx.ui.Button;
import uk.co.stikman.sett.gfx.ui.Label;
import uk.co.stikman.sett.gfx.ui.SimpleWindow;
import uk.co.stikman.sett.gfx.ui.UITimer;
import uk.co.stikman.sett.gfx.ui.WindowPosition;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.gfx.util.StreamSource;
import uk.co.stikman.sett.gfx.util.WindowInitError;
import uk.co.stikman.sett.svr.GameServer;
import uk.co.stikman.sett.svr.GameServerConfig;
import uk.co.stikman.sett.svr.SendMessage;
import uk.co.stikman.sett.svr.ServerException;
import uk.co.stikman.utils.StikDataInputStream;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class SettApp {
	public static final int		VERSION			= 1;

	public static final StikLog	LOGGER			= StikLog.getLogger(SettApp.class);

	private static final int	WINDOW_W		= 1024;
	private static final int	WINDOW_H		= 768;

	public static final int		TRI_SIZE		= 32;
	public static final int		CHUNK_SIZE		= 16;
	public static final float	PATH_WIDTH		= 0.4f;
	public static final float	ROAD_DEPTH		= 0.03f;
	public static final float	ROOT3_2			= (float) (Math.sqrt(3.0) / 2.0);
	public static final Vector3	SUNLIGHT		= new Vector3(9, 9, -1).normalize();
	public static final int		DEFAULT_PORT	= 20202;

	private static final int	UI_PIXEL_SCALE	= 3;

	public static void main(String[] args) {
		new SettApp().go();
	}

	private Window3DNative					window;
	private SettUI							ui;
	private GameResources					uiResources;
	private BaseView						view;
	private double							lastT;

	private GameServer						svr;

	private GameConnection					conn;

	private Map<Integer, PendingNetworkOp>	pendingNetworkOperations	= new HashMap<>();

	private ClientGame						game;

	private FrameBufferNative				uifbo;

	private LoadingGameWindow				loading;

	private float							netRate						= 0.5f;				//5.0f;
	private float							nextNetPoll					= 0.0f;

	private void go() {
		try {
			GameServerConfig config = new GameServerConfig();
			svr = new GameServer(config, this);
			svr.start();

		} catch (Exception e) {
			LOGGER.error("Failed to start server:");
			LOGGER.error(e);
			return;
		}

		try {
			setView(new NullView(this));

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

		svr.terminate();
	}

	private void initNetwork() {
		try {
			conn = new GameConnection();
			conn.connect("localhost", DEFAULT_PORT);

			SendMessage msg = new SendMessage();
			msg.write4("INFO");
			send(msg, resp -> {
				try {
					StikDataInputStream str = resp.asStream();
					int vers = str.readInt();
					if (vers != SettApp.VERSION)
						throw new ServerException(ServerException.E_INVALID_VERSION, "Server version [" + vers + "] is not compatible with this client (version [" + SettApp.VERSION + "])");
				} catch (ServerException | IOException e) {
					LOGGER.error("Response: " + e.getMessage());
				}

				login();
			});
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void login() {
		try {
			SendMessage msg = new SendMessage();
			msg.write4("AUTH");
			msg.writeString("Stik");
			msg.writeString("password");
			send(msg, resp -> {
				try {
					resp.getData();
				} catch (ServerException e) {
					LOGGER.error("Response: " + e.getMessage());
				}
				if (!(view instanceof MainMenuView))
					throw new RuntimeException("No menu");
				createGame(((MainMenuView) view).getWorldParams());
			});

		} catch (Exception e) {
			LOGGER.error("Response: " + e.getMessage());
		}
	}

	private void createGame(WorldParameters params) {
		loading.setProgress(0);
		try {
			SendMessage msg = new SendMessage();
			msg.write4("NEWG");
			msg.writeString("New Game 1");
			msg.writeBuf(params.toBytes());
			send(msg, resp -> {
				try {
					resp.getData();
				} catch (ServerException e) {
					LOGGER.error("Response: " + e.getMessage());
				}
				downloadInitGame();
			});
			loading.setProgress(20);

		} catch (IOException e) {
			LOGGER.error("Response: " + e.getMessage());
		}
	}

	private void downloadInitGame() {
		loading.setProgress(40);
		try {
			SendMessage msg = new SendMessage();
			msg.write4("GINI");
			send(msg, resp -> {
				ClientGame game = new ClientGame(this);
				try {
					game.fromStream(new SettInputStream(resp.asStream()));
				} catch (ServerException | IOException | ResourceLoadError e) {
					LOGGER.error("Response: " + e.getMessage(), e);
					loading.hide();
					setView(new MainMenuView(this));
					showMessage("ERROR", e.getMessage());
					return;
				}

				// load initial game data from stream
				this.game = game;
				GameView view = new GameView(this, game);
				view.init();
				setView(view);
				loading.setProgress(100);
				loading.closeIn1000ms();

			});
		} catch (IOException e) {
			LOGGER.error("Response: " + e.getMessage());
		}
	}

	private void showMessage(String title, String message) {
		SettStandardWindow wnd = new SettStandardWindow(this, 0);
		wnd.setBounds(new Rect(0, 0, 200, 100));
		Label l = new Label(wnd, title);
		l.setColour(new Vector4(1, 1, 0, 1)).setBounds(new Rect(12, 12, 180, 16));
		l.getRenderOptions().setOutlineMode(OutlineMode.SHADOW);
		l = new Label(wnd, message);
		l.setColour(new Vector4(0.7f, 0.7f, 0.7f, 1)).setBounds(new Rect(12, 30, 180, 50));
		l.getRenderOptions().setWrap(WordWrap.WRAP);
		l.getRenderOptions().setLineSpacing(1);

		Button b = new Button(wnd, "ok", "OK");
		b.setOnClick(x -> wnd.hide());
		Rect r = wnd.getClientBounds();
		b.setBounds(new Rect(r.x + r.w - 38, r.y + r.h - 18, 36, 16));
		wnd.setWindowPosition(WindowPosition.CENTRE);
		wnd.showModal();
	}

	private void setView(BaseView v) {
		if (v == null)
			throw new NullPointerException();
		if (v == this.view)
			return;
		if (this.view != null)
			this.view.hidden();
		this.view = v;
		if (getWindow() != null)
			view.setViewport(getWindow().getWidth(), getWindow().getHeight());
		this.view.shown();
	}

	public void send(SendMessage msg, ResponseHandler onresponse) throws IOException {
		int id = conn.send(msg.getBytes());
		pendingNetworkOperations.put(Integer.valueOf(id), new PendingNetworkOp(onresponse));
	}

	private void onInit() throws WindowInitError {
		uifbo = new FrameBufferNative(512, 512, ColourModel.RGBA_DEPTH, false);
		ui = new SettUI(this);
		uiResources = new GameResources(window);
		try {
			uiResources.load(Resources.getFileAsString("ui/uires.txt"), new StreamSource() {
				@Override
				public InputStream getStream(String name) throws IOException {
					return Resources.getFile("ui/" + name);
				}
			});
		} catch (IOException | ResourceLoadError e) {
			throw new WindowInitError("Could not load Resources", e);
		}
		setView(new MainMenuView(this));
		//		initNetwork();
	}

	private void onResize(int w, int h) {
		ui.handleResize(w / UI_PIXEL_SCALE, h / UI_PIXEL_SCALE);

		if (uifbo != null)
			uifbo.destroy();
		int w2 = w / UI_PIXEL_SCALE;
		int h2 = h / UI_PIXEL_SCALE;
		uifbo = new FrameBufferNative(w2, h2, ColourModel.RGBA_DEPTH, false);

		if (view != null)
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
		update((float) dt);
		render();
	}

	private void onMouseMove(int x, int y) {
		if (ui.handleMouseMove(x / UI_PIXEL_SCALE, y / UI_PIXEL_SCALE))
			return;
		view.mouseMove(x, y);
	}

	private void onMouseDown(int x, int y, int button) {
		if (ui.handleMouseDown(x / UI_PIXEL_SCALE, y / UI_PIXEL_SCALE, button))
			return;
		view.mouseDown(x, y, button);
	}

	private void onMouseUp(int x, int y, int button) {
		if (ui.handleMouseUp(x / UI_PIXEL_SCALE, y / UI_PIXEL_SCALE, button))
			return;
		view.mouseUp(x, y, button);
	}

	private void onMouseWheel(int x, int y, int dy) {
		view.mouseWheel(dy);
	}

	private void render() {
		window.setDepthTestEnabled(true);
		window.clear();
		view.render();

		RenderTarget old = window.setRenderTarget(uifbo.getRenderTarget());
		window.setBlend(BlendMode.FBO_SEPARATE);
		window.clear();
		window.setDepthTestEnabled(false);
		ui.render();
		window.setRenderTarget(old);
		window.drawBuf(uifbo, 0, 0, uifbo.getWidth() * UI_PIXEL_SCALE, uifbo.getHeight() * UI_PIXEL_SCALE);
	}

	private void update(float dt) {
		if (game != null) {
			nextNetPoll -= dt;
			if (nextNetPoll <= 0.0f) {
				while (nextNetPoll <= 0.0f)
					nextNetPoll += (1.0f / netRate);
				sendNetPoll();
			}
		}

		//
		// read network responses and call response handlers, all in 
		// the current thread
		//
		if (conn != null) {
			for (;;) {
				Response r = conn.extract();
				if (r == null)
					break;

				PendingNetworkOp networkOp = pendingNetworkOperations.remove(Integer.valueOf(r.getId()));
				networkOp.getHandler().response(r);
			}
		}

		ui.update(dt);
		if (view == null)
			return;

		if (game != null)
			game.update(dt);
		view.update(dt);
	}

	private void sendNetPoll() {
		SendMessage msg = new SendMessage();
		try {
			msg.write4("POLL");
			send(msg, res -> {
				try {
					List<GameEvent> events = GameEvents.read(res.getData());
					for (GameEvent ev : events) {
						ev.applyTo(game);
						if (ev.isChangesStructure()) {
							((GameView) view).triggerShadowRebuild();
						}
					}
				} catch (ServerException | IOException e) {
					LOGGER.error(e);
				}
			});
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public Window3D getWindow() {
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

	public SettUI getUI() {
		return ui;
	}

	public GameResources getUIResources() {
		return uiResources;
	}

	public void quit() {
		window.close();
	}

	public void startNewGame(WorldParameters params) {
		if (view instanceof MainMenuView)
			((MainMenuView) view).hideMenus();
		loading = new LoadingGameWindow(this);
		loading.showModal();
		initNetwork();
	}

}
