package uk.co.stikman.sett.gfx.ui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import uk.co.stikman.sett.gfx.GameResources;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.text.BitmapFont;
import uk.co.stikman.sett.gfx.text.HAlign;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.text.VAlign;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector4;

/**
 * Implements a really basic UI widget library thing. A {@link SimpleWindow}
 * owns everything in it, you configure things via this. At the very least you
 * need to set the font with {@link #setFont(BitmapFont)}, otherwise text won't
 * render
 * 
 * @author Stik
 *
 */
public class SimpleWindow {
	private int					screenHeight;
	private int					screenWidth;
	private String				caption;

	private final Rect			tmpR			= new Rect();
	private final Rect			tmpR2			= new Rect();
	private final Vector2i		tmpVi			= new Vector2i();
	private final Vector4		tmpC			= new Vector4();

	private final Window3D		window;
	private boolean				visible			= false;

	private RenderTextOptions	rtoTitle;
	private Component			currentHover	= null;

	private List<Component>		components		= new ArrayList<>();
	private Rect				bounds;
	private GameResources		resources;
	private boolean				glass			= false;
	private WindowTheming		theming			= new WindowTheming();
	private WindowPosition		windowPosition	= WindowPosition.MANUAL;
	private Rect				requestedBounds;
	private boolean				initialised		= false;
	private UI					ui;

	public SimpleWindow(UI owner, GameResources resources) {
		super();
		this.resources = resources;
		this.window = owner.getWindow3D();
		this.ui = owner;

		rtoTitle = new RenderTextOptions(HAlign.CENTRE, VAlign.TOP);
		rtoTitle.setOutlineMode(OutlineMode.SHADOW);
		rtoTitle.setOutlineColour(new Vector4(0, 0, 0, 0.3f));
	}

	public void screenResize(int w, int h) {
		this.screenWidth = w;
		this.screenHeight = h;
		bounds = null;
	}

	public void show() {
		if (!initialised)
			init();
		visible = true;
		ui.relayoutWindow(this);
	}

	public void hide() {
		visible = false;
	}

	public boolean isVisible() {
		return visible;
	}

	public void render() {
		if (window == null || !isVisible())
			return;

		//
		// Dark overlay
		//
		if (glass)
			window.drawFlatRect(0, 0, screenWidth, screenHeight, tmpC.set(0, 0, 0, 0.4f));

		Rect windowBounds = getBounds();
		
		if (theming.getBackgroundSprite() == null) {
			window.drawFlatRect(windowBounds, theming.getBackgroundColour());
		} else {
			if (theming.getStretchMode() == null)
				window.drawImage(theming.getBackgroundSprite().getImage(0), (int) windowBounds.x, (int) windowBounds.y);
			else
				window.drawSmartQuad(theming.getBackgroundSprite(), windowBounds, theming.getBackgroundColour(), theming.getStretchMode(), 0);
		}

		if (theming.getTitleFont() != null) {
			tmpR2.set(windowBounds.getX(), windowBounds.getY() + 2, windowBounds.getW(), 30);
			window.drawText(tmpR2, getCaption(), theming.getTitleFont(), rtoTitle, theming.getFontColour());
		}

		for (Component c : components) {
			c.render();
		}

	}

	public boolean keyPress(char ch) {
		if (!isVisible())
			return false;
		return false;
	}

	public boolean keyCode(int keycode, boolean down, int mods) {
		if (!isVisible())
			return false;
		if (down && keycode == GLFW.GLFW_KEY_ESCAPE) {
			hide();
			return true;
		}
		return false;
	}

	public void mouseMove(int x, int y) {
		if (!isVisible())
			return;

		//		x = (int) (x - bounds.x);
		//		y = (int) (y - bounds.y);

		Component hov = null;
		for (Component c : components) {
			Rect r = c.getBounds();
			if (r.contains(x, y)) {
				hov = c;
				c.mouseMove((int) (x - r.x), (int) (y - r.y));
			}
		}
		if (hov != currentHover) {
			if (currentHover != null) {
				Rect r = currentHover.getBounds();
				currentHover.mouseExit((int) (x - r.x), (int) (y - r.y));
			}
			currentHover = hov;
			if (currentHover != null) {
				Rect r = currentHover.getBounds();
				currentHover.mouseEnter((int) (x - r.x), (int) (y - r.y));
			}
		}
	}

	public void mouseUp(int x, int y, int button) {
		for (Component c : components) {
			Rect r = c.getBounds();
			if (r.contains(x, y))
				c.mouseUp((int) (x - r.x), (int) (y - r.y), button);
		}
	}

	public void mouseDown(int x, int y, int button) {
		for (Component c : components) {
			Rect r = c.getBounds();
			if (r.contains(x, y))
				c.mouseDown((int) (x - r.x), (int) (y - r.y), button);
		}
	}

	public void init() {
		initialised = true;
	}

	public void update(float dt) {
		for (Component c : components)
			c.update(dt);
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public List<Component> getComponents() {
		return components;
	}

	protected Rect getBounds() {
		if (bounds == null) {
			if (requestedBounds == null)
				requestedBounds = new Rect(20, 20, 100, 100); // a default size
			bounds = new Rect(requestedBounds);
			if (windowPosition == WindowPosition.CENTRE)
				bounds.centreIn(tmpR2.set(0, 0, screenWidth, screenHeight));
			bounds.quantize();
		}
		return bounds;
	}

	public Window3D getWindow() {
		return window;
	}

	/**
	 * When <code>true</code> it'll draw a black ghosting over the background
	 * 
	 * @return
	 */
	public boolean isGlass() {
		return glass;
	}

	public void setGlass(boolean glass) {
		this.glass = glass;
	}

	public WindowTheming getTheming() {
		return theming;
	}

	public void setTheming(WindowTheming theming) {
		this.theming = theming;
	}

	public WindowPosition getWindowPosition() {
		return windowPosition;
	}

	public void setWindowPosition(WindowPosition windowPosition) {
		this.windowPosition = windowPosition;
	}

	public void setBounds(Rect bounds) {
		this.requestedBounds = new Rect(bounds);
		this.bounds = null;
	}

	public GameResources getResources() {
		return resources;
	}

	public Component find(String name) {
		for (Component c : components)
			if (name.equals(c.getName()))
				return c;
		return null;
	}

}
