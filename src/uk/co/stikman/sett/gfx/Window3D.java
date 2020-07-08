package uk.co.stikman.sett.gfx;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;

import uk.co.stikman.sett.gfx.MeshCache.Entry;
import uk.co.stikman.sett.gfx.lwjgl.FrameBufferNative;
import uk.co.stikman.sett.gfx.lwjgl.SmartQuadNative;
import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.text.BitmapFont;
import uk.co.stikman.sett.gfx.text.BitmapText;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.util.OnFrameEvent;
import uk.co.stikman.sett.gfx.util.OnInitHandler;
import uk.co.stikman.sett.gfx.util.OnKeyCodeEvent;
import uk.co.stikman.sett.gfx.util.OnKeyPressEvent;
import uk.co.stikman.sett.gfx.util.OnMouseDownEvent;
import uk.co.stikman.sett.gfx.util.OnMouseMoveEvent;
import uk.co.stikman.sett.gfx.util.OnMouseUpEvent;
import uk.co.stikman.sett.gfx.util.OnMouseWheelEvent;
import uk.co.stikman.sett.gfx.util.OnResizeEvent;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector4;

public abstract class Window3D {
	private static final int					STRETCH_CACHE_SIZE			= 64;

	private String								caption;
	private int									height;
	private int									width;
	private boolean								useLinearTextures;

	private Map<String, Shader>					shaders						= new HashMap<>();
	private MeshCache							meshCache					= new MeshCache();

	private Shader								currentShader;
	private Shader								basicShader;
	private Shader								flatShader;
	private Shader								textShader;

	private RenderTarget						renderTarget				= null;

	private final SmartQuadKey					tmpSQK						= new SmartQuadKey();
	private Map<SmartQuadKey, SmartQuadNative>	smartQuadCache				= new HashMap<>();
	private StretchMode[]						stretchCache				= new StretchMode[STRETCH_CACHE_SIZE];
	private Map<String, Integer>				attribLocations				= new HashMap<>();
	private RenderTextOptions					defaultTextRenderOptions	= RenderTextOptions.DEFAULT;

	private OnFrameEvent						onFrame;
	private OnInitHandler						onInit;
	private OnResizeEvent						onResize;
	private OnKeyPressEvent						onKeyPress;
	private OnKeyCodeEvent						onKeyCode;
	private OnMouseMoveEvent					onMouseMove;
	private OnMouseDownEvent					onMouseDown;
	private OnMouseUpEvent						onMouseUp;
	private OnMouseWheelEvent					onMouseWheel;

	private Matrix4								tmpM						= new Matrix4();
	private Vector2[]							tmpV						= new Vector2[] { new Vector2(), new Vector2() };
	private Vector2i							tmpVi						= new Vector2i();

	public Window3D(int width, int height, boolean linear) {
		this.width = width;
		this.height = height;
		this.useLinearTextures = linear;
		for (int i = 0; i < STRETCH_CACHE_SIZE; ++i)
			stretchCache[i] = StretchMode.createSmart(i);
	}

	public String getTextResource(String name) throws ResourceLoadError {
		try (InputStream is = Window3DNative.class.getResourceAsStream(name)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ResourceLoadError("Could not load shader [" + name + "]", e);
		}
	}

	public Shader loadShader(String name, String frag, String vert) {
		Shader s = constructShader();
		s.load(name, frag, vert);
		shaders.put(name, s);
		return s;
	}

	public RenderTarget setRenderTarget(RenderTarget t) {
		if (t == renderTarget)
			return renderTarget;
		if (t == null)
			throw new NullPointerException("RenderTarget cannot be null");

		RenderTarget old = renderTarget;
		if (renderTarget != null)
			renderTarget.targetUnbound();
		renderTarget = t;
		t.targetBound();
		Vector2i v = tmpVi;
		renderTarget.getDimensions(v);
		renderTarget.apply(v);
		return old;
	}

	public RenderTarget getRenderTarget() {
		return renderTarget;
	}

	public Shader getCurrentShader() {
		return currentShader;
	}

	public void setCurrentShader(Shader s) {
		this.currentShader = s;
	}

	public Shader getShader(String name) {
		Shader s = shaders.get(name);
		if (s == null)
			throw new NoSuchElementException(name);
		return s;
	}

	public void drawBuf(FrameBufferNative buf, int x, int y, int w, int h) {
		drawBuf(buf, x, y, w, h, basicShader);
	}

	public void drawBuf(FrameBufferNative buf, int x, int y, int w, int h, Shader shader) {
		Matrix4 view = tmpM;
		Vector2i v = tmpVi;
		renderTarget.getDimensions(v);
		view.makeOrtho(0.0f, v.x, v.y, 0.0f, -1.0f, 1.0f);
		view.translate(x, y, 0.0f);

		shader.use();
		Shader.Uniform uniView = basicShader.getUniform("view", false);
		Shader.Uniform uniColour = basicShader.getUniform("colour", false);
		Shader.Uniform uniTxt = basicShader.getUniform("txt", false);

		uniView.bindMat4(view);
		uniColour.bindVec4(1, 1, 1, 1);
		uniTxt.bindTexture(buf.getTexture(), 0);

		Entry e = meshCache.get(buf);
		if (e.getMesh() == null) {
			PolyMesh m = createPolyMesh();
			m.addVert(0, 0, 0, 0, 1, VectorColours.WHITE);
			m.addVert(0, h, 0, 0, 0, VectorColours.WHITE);
			m.addVert(w, h, 0, 1, 0, VectorColours.WHITE);
			m.addVert(w, 0, 0, 1, 1, VectorColours.WHITE);
			m.addTri(0, 1, 2);
			m.addTri(0, 2, 3);
			e.setMesh(m);
		}
		e.getMesh().render(0);
	}

	public void drawFlatRect(Rect r, Vector4 c) {
		drawFlatRect((int) r.x, (int) r.y, (int) r.w, (int) r.h, c);
	}

	public void drawFlatRect(int x, int y, int w, int h, Vector4 colour) {
		Matrix4 view = tmpM;
		Vector2i vi = tmpVi;
		renderTarget.getDimensions(vi);
		view.makeOrtho(0.0f, vi.x, vi.y, 0.0f, -1.0f, 1.0f);
		view.translate(x, y, 0.0f);
		view.scale(w, h, 0.0f);

		flatShader.use();
		Shader.Uniform uniView = flatShader.getUniform("view", false);
		Shader.Uniform uniColour = flatShader.getUniform("colour", false);

		uniView.bindMat4(view);
		uniColour.bindVec4(colour);
		Entry e = meshCache.get("_flatrect");
		if (e.getMesh() == null) {
			PolyMesh m = createPolyMesh();
			m.addVert(0, 0, 0, 0, 0, VectorColours.WHITE);
			m.addVert(0, 1, 0, 0, 0, VectorColours.WHITE);
			m.addVert(1, 1, 0, 0, 0, VectorColours.WHITE);
			m.addVert(1, 0, 0, 0, 0, VectorColours.WHITE);
			m.addTri(0, 1, 2);
			m.addTri(0, 2, 3);
			e.setMesh(m);
		}
		e.getMesh().render(0);
	}

	public void drawImage(Image image) {
		drawImage(image, 0, 0, 0f);
	}

	public void drawImage(Image image, int offx, int offy) {
		drawImage(image, offx, offy, 0f);
	}

	public void drawImage(Image image, int offx, int offy, float angle) {
		//
		// Bind texture, shader, create a VBO, render
		//
		Matrix4 view = tmpM;
		Vector2i vi = tmpVi;
		renderTarget.getDimensions(vi);
		view.makeOrtho(0.0f, vi.x, vi.y, 0.0f, -1.0f, 1.0f);
		if (angle != 0) {
			view.translate(offx + image.getWidth() / 2, offy + image.getHeight() / 2, 0.0f);
			view.rotate(0, 0, 1, angle);
			view.translate(-image.getWidth() / 2, -image.getHeight() / 2, 0.0f);
		} else {
			view.translate(offx, offy, 0.0f);
		}

		basicShader.use();
		Shader.Uniform uniView = basicShader.getUniform("view", false);
		Shader.Uniform uniColour = basicShader.getUniform("colour", false);
		Shader.Uniform uniTxt = basicShader.getUniform("txt", false);

		uniView.bindMat4(view);
		uniColour.bindVec4(1, 1, 1, 1);
		uniTxt.bindTexture(image.getTexture(), 0);

		Entry e = meshCache.get(image);
		if (e.getMesh() == null) {
			int iw = image.getWidth();
			int ih = image.getHeight();
			float u = image.getImgU();
			float v = image.getImgV();
			PolyMesh m = createPolyMesh();
			m.addVert(0, 0, 0, 0, 0, VectorColours.WHITE);
			m.addVert(0, ih, 0, 0, v, VectorColours.WHITE);
			m.addVert(iw, ih, 0, u, v, VectorColours.WHITE);
			m.addVert(iw, 0, 0, u, 0, VectorColours.WHITE);
			m.addTri(0, 1, 2);
			m.addTri(0, 2, 3);
			e.setMesh(m);
		}
		e.getMesh().render(0);
	}

	/**
	 * A "SmartQuad" is an intelligently resizable square image that tiles the
	 * inner portion. It's good for doing things like windows and UI boxes
	 * 
	 * @param image
	 * @param r
	 * @param colour
	 * @param border
	 */
	public void drawSmartQuad(RectSprite sprite, Rect r, Vector4 colour, int border, int layer) {
		StretchMode sm = null;
		if (border < STRETCH_CACHE_SIZE)
			sm = stretchCache[border];
		else
			sm = StretchMode.createSmart(border);
		drawSmartQuad(sprite, r, colour, sm, layer);
	}

	public void drawSmartQuad(RectSprite sprite, Rect r, Vector4 colour, StretchMode stretch, int layer) {
		SmartQuadKey key = tmpSQK;
		key.set(sprite, (int) r.getW(), (int) r.getH(), colour, stretch);
		SmartQuadNative sq = smartQuadCache.get(key);
		if (sq == null) {
			sq = new SmartQuadNative(this, sprite, new Vector2(r.getW(), r.getH()), stretch);
			sq.setColour(colour);
			key = new SmartQuadKey();
			key.set(sprite, (int) r.getW(), (int) r.getH(), colour, stretch);
			smartQuadCache.put(key, sq);
		}

		Matrix4 view = tmpM;
		Vector2i vi = tmpVi;
		renderTarget.getDimensions(vi);
		view.makeOrtho(0.0f, vi.x, vi.y, 0.0f, -1.0f, 1.0f);
		view.translate(r.getX(), r.getY(), 0.0f);

		basicShader.use();
		Shader.Uniform uniView = basicShader.getUniform("view", false);
		Shader.Uniform uniColour = basicShader.getUniform("colour", false);
		Shader.Uniform uniTxt = basicShader.getUniform("txt", false);

		uniView.bindMat4(view);
		if (colour != null)
			uniColour.bindVec4(colour);
		else
			uniColour.bindVec4(1, 1, 1, 1);
		uniTxt.bindTexture(sprite.getImage(layer).getTexture(), 0);
		sq.render();
	}

	public void drawMesh(PolyMesh mesh, Image image, int offx, int offy, float scalex, float scaley, float angle, Vector4 colour, int frame) {
		Matrix4 view = tmpM;
		Vector2i vi = tmpVi;
		renderTarget.getDimensions(vi);
		view.makeOrtho(0.0f, vi.x, vi.y, 0.0f, -1.0f, 1.0f);
		if (angle != 0) {
			view.translate(offx + mesh.getWidth() / 2, offy + mesh.getHeight() / 2, 0.0f);
			view.rotate(0, 0, 1, angle);
			view.translate(-mesh.getWidth() / 2, -mesh.getHeight() / 2, 0.0f);
		} else {
			view.translate(offx, offy, 0.0f);
		}
		view.scale(scalex, scaley, 1.0f);

		basicShader.use();
		Shader.Uniform uniView = basicShader.getUniform("view", false);
		Shader.Uniform uniColour = basicShader.getUniform("colour", false);
		Shader.Uniform uniTxt = basicShader.getUniform("txt", false);

		uniView.bindMat4(view);
		if (colour != null)
			uniColour.bindVec4(colour);
		else
			uniColour.bindVec4(1, 1, 1, 1);
		uniTxt.bindTexture(image.getTexture(), 0);

		mesh.render(frame);
	}

	/**
	 * Colour can be null if you don't want to force a colour, otherwise it'll
	 * use whatever the text has encoded into it
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param text
	 * @param font
	 * @param rto
	 * @param colour
	 */
	@SuppressWarnings("incomplete-switch")
	public void drawText(int x, int y, int w, int h, String text, BitmapFont font, RenderTextOptions rto, Vector4 colour) {
		// TODO: cache these to avoid constant reallocations
		BitmapText bmt = constructBitmapText(textShader, font, rto, w);
		try {
			bmt.setText(text);
			textShader.use();

			Matrix4 view = tmpM;
			Vector2i v = tmpVi;
			renderTarget.getDimensions(v);
			view.makeOrtho(0.0f, v.x, v.y, 0.0f, -1.0f, 1.0f);

			int offx = 0;
			switch (rto.getAlignH()) {
			case CENTRE:
				offx = (w - bmt.getLineWidth()) / 2;
				break;
			case RIGHT:
				offx = w - bmt.getLineWidth();
				break;
			}
			int offy = 0;
			switch (rto.getAlignV()) {
			case BOTTOM:
				offy = h - bmt.getLineHeight();
				break;
			case CENTRE:
				offy = (h - bmt.getLineHeight()) / 2;
				break;
			}

			view.translate(x + offx, y + offy, 0.0f);

			bmt.render(view, colour, colour == null ? 0.0f : 1.0f);
		} finally {
			bmt.destroy();
		}
	}

	public void drawText(Rect r, String text, BitmapFont font, RenderTextOptions rto, Vector4 colour) {
		drawText((int) r.getX(), (int) r.getY(), (int) r.getW(), (int) r.getH(), text, font, rto, colour);
	}

	public void drawText(Rect r, String text, BitmapFont font, Vector4 colour) {
		drawText((int) r.getX(), (int) r.getY(), (int) r.getW(), (int) r.getH(), text, font, null, colour);
	}

	public void drawText(int x, int y, int w, int h, String text, BitmapFont font) {
		drawText(x, y, w, h, text, font, null);
	}

	public void drawText(int x, int y, int w, int h, String text, BitmapFont font, Vector4 colour) {
		drawText(x, y, w, h, text, font, defaultTextRenderOptions, colour);
	}

	protected abstract BitmapText constructBitmapText(Shader shader, BitmapFont font, RenderTextOptions rto, int w);

	public Vector2 measureText(String text, BitmapFont font, RenderTextOptions rto, Vector2 result) {
		// TODO: cache these to avoid constant reallocations
		BitmapText bmt = constructBitmapText(textShader, font, rto, Integer.MAX_VALUE);
		try {
			bmt.setText(text);
			bmt.measure(result);
			return result;
		} finally {
			bmt.destroy();
		}
	}

	protected abstract Shader constructShader();

	public abstract PolyMesh createPolyMesh();

	public abstract void clear(Vector4 c);

	public abstract void clear();

	public void setTitle(String caption) {
		this.caption = caption;
	}

	public String getTitle() {
		return caption;
	}

	public void setOnFrame(OnFrameEvent h) {
		this.onFrame = h;
	}

	public OnResizeEvent getOnResize() {
		return onResize;
	}

	public void setOnResize(OnResizeEvent onResize) {
		this.onResize = onResize;
	}

	public void setOnMouseMove(OnMouseMoveEvent e) {
		this.onMouseMove = e;
	}

	public void setOnKeyPress(OnKeyPressEvent h) {
		this.onKeyPress = h;
	}

	public OnKeyPressEvent getOnKeyPress() {
		return onKeyPress;
	}

	public OnKeyCodeEvent getOnKeyCode() {
		return onKeyCode;
	}

	public void setOnKeyCode(OnKeyCodeEvent onKeyCode) {
		this.onKeyCode = onKeyCode;
	}

	public OnMouseMoveEvent getOnMouseMove() {
		return onMouseMove;
	}

	public OnMouseDownEvent getOnMouseDown() {
		return onMouseDown;
	}

	public void setOnMouseDown(OnMouseDownEvent onMouseDown) {
		this.onMouseDown = onMouseDown;
	}

	public OnMouseUpEvent getOnMouseUp() {
		return onMouseUp;
	}

	public void setOnMouseUp(OnMouseUpEvent onMouseUp) {
		this.onMouseUp = onMouseUp;
	}

	public OnMouseWheelEvent getOnMouseWheel() {
		return onMouseWheel;
	}

	public void setOnMouseWheel(OnMouseWheelEvent onMouseWheel) {
		this.onMouseWheel = onMouseWheel;
	}

	public OnInitHandler getOnInit() {
		return onInit;
	}

	public void setOnInit(OnInitHandler onInit) {
		this.onInit = onInit;
	}

	public OnFrameEvent getOnFrame() {
		return onFrame;
	}

	public abstract int getFramecount();

	public void setBasicShader(Shader basicShader) {
		this.basicShader = basicShader;
	}

	public void setFlatShader(Shader flatShader) {
		this.flatShader = flatShader;
	}

	public void setTextShader(Shader textShader) {
		this.textShader = textShader;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isUseLinearTextures() {
		return useLinearTextures;
	}

	public void setUseLinearTextures(boolean useLinearTextures) {
		this.useLinearTextures = useLinearTextures;
	}

	public int registerAttribLocation(String name) {
		Integer i = attribLocations.get(name);
		if (i != null)
			return i.intValue();
		attribLocations.put(name, i = Integer.valueOf(attribLocations.size()));
		return i.intValue();
	}

	public int getAttribLocation(String name) {
		Integer i = attribLocations.get(name);
		if (i == null)
			throw new NoSuchElementException("Shader Attrib [" + name + "] does not exist");
		return i.intValue();
	}

	public void setDefaultTextRenderOptions(RenderTextOptions rto) {
		this.defaultTextRenderOptions = rto;
	}

	public RenderTextOptions getDefaultTextRenderOptions() {
		return defaultTextRenderOptions;
	}

	public abstract void loadImage(String name, Image image, BufferedImage img, boolean linear) throws ResourceLoadError;

	public abstract void loadImage(String name, Image image, InputStream is, boolean linear) throws ResourceLoadError;

	public abstract RenderTarget getDefaultRenderTarget();

	public abstract void unhideCursor();

	public abstract void hideCursor();

	public abstract void setCursorPosition(int x, int y);

	public abstract Cursor loadCursor(String name, InputStream png, Vector2i hotspot) throws IOException;

	public abstract BitmapFont loadFontZIP(InputStream is) throws ResourceLoadError;

	public abstract void setDepthTestEnabled(boolean b);

}
