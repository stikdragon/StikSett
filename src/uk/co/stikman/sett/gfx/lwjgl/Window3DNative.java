package uk.co.stikman.sett.gfx.lwjgl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import uk.co.stikman.sett.gfx.BlendMode;
import uk.co.stikman.sett.gfx.Cursor;
import uk.co.stikman.sett.gfx.Image;
import uk.co.stikman.sett.gfx.PolyMesh;
import uk.co.stikman.sett.gfx.RenderTarget;
import uk.co.stikman.sett.gfx.Shader;
import uk.co.stikman.sett.gfx.Texture;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.sett.gfx.WindowRenderTargetNative;
import uk.co.stikman.sett.gfx.lwjgl.GLFWWindowOptions.OpenGLProfile;
import uk.co.stikman.sett.gfx.text.BitmapFont;
import uk.co.stikman.sett.gfx.text.BitmapText;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.text.TextureLoader;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.gfx.util.WindowInitError;
import uk.co.stikman.sett.gfx.util.ZipUtil;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector4;

public class Window3DNative extends Window3D {

	private GLFWWindow			window;
	private List<TextureNative>	textures			= new ArrayList<>();
	private List<BitmapFont>	fonts				= new ArrayList<>();
	private List<Cursor>		cursors				= new ArrayList<>();

	private RenderTarget		defaultRenderTarget	= new WindowRenderTargetNative(this);

	private int					framecount;
	private int					mouseX;
	private int					mouseY;

	public Window3DNative(int width, int height, boolean linear) {
		super(width, height, linear);

	}

	public void start() throws WindowInitError {
		try {
			initGLFW();
		} catch (ResourceLoadError e) {
			throw new WindowInitError(e);
		}

		if (getOnInit() != null)
			getOnInit().init();
		if (getOnResize() != null)
			getOnResize().resized(getWidth(), getHeight());
		loop();
	}

	@Override
	public void setDepthTestEnabled(boolean b) {
		if (b)
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		else
			GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	private void loop() {
		GLFW.glfwSwapInterval(1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GLUtil.checkError();

		setRenderTarget(defaultRenderTarget);

		long startTime = System.nanoTime();
		while (!window.shouldClose()) {
			++framecount;
			long t1 = System.nanoTime() - startTime;
			try {

				if (!window.isMinimized() && window.getHeight() > 0 && window.getWidth() > 0) {
					if (getOnFrame() != null)
						getOnFrame().frame(t1);
					window.swapBuffers();
					GLUtil.checkError();
				}

			} catch (Throwable th) {
				handleException(th);
			}

			GLFW.glfwPollEvents();
		}

	}

	private void handleException(Throwable th) {
		throw new RuntimeException(th);
	}

	private void initGLFW() throws ResourceLoadError {

		GLFW.glfwSetErrorCallback(new StreamErrorCallback(System.err));
		if (!GLFW.glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		GLFWWindowOptions opts = new GLFWWindowOptions();
		opts.setVisible(false).setResizable(true).setContextVersion("3.3").setProfile(OpenGLProfile.CORE_PROFILE);
		opts.setSize(getWidth(), getHeight());
		opts.setSamples(4).setCaption(getTitle());

		window = new GLFWWindow(opts);
		GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		window.setPos((vidmode.width() - getWidth()) / 2, (vidmode.height() - getHeight()) / 2);
		GLFW.glfwMakeContextCurrent(window.getHandle());
		window.show();
		window.addResizeHandler(this::resized);
		window.addKeyPressHandler(this::keyPress);
		window.addKeyCodeHandler(this::keyCode);
		window.addMouseMoveHandler(this::mouseMoved);
		window.addMouseDownHandler(this::mouseDown);
		window.addMouseUpHandler(this::mouseUp);
		window.addMouseWheelHandler(this::mouseWheel);

		GL.createCapabilities();
		setBasicShader(loadShader("basic", getTextResource("basic.frag"), getTextResource("basic.vert")));
		setTextShader(loadShader("text", getTextResource("text.frag"), getTextResource("text.vert")));
		setFlatShader(loadShader("flat", getTextResource("flat.frag"), getTextResource("flat.vert")));
	}

	@Override
	protected Shader constructShader() {
		return new ShaderNative(this);
	}

	private void mouseWheel(GLFWWindow window, int dy) {
		if (getOnMouseWheel() != null)
			getOnMouseWheel().mouseWheel(mouseX, mouseY, dy);
	}

	private void mouseUp(GLFWWindow window, int x, int y, int button) {
		if (getOnMouseUp() != null)
			getOnMouseUp().mouseUp(x, y, button);
	}

	private void mouseDown(GLFWWindow window, int x, int y, int button) {
		if (getOnMouseDown() != null)
			getOnMouseDown().mouseDown(x, y, button);
	}

	private void mouseMoved(GLFWWindow window, int x, int y) {
		mouseX = x;
		mouseY = y;
		if (getOnMouseMove() != null)
			getOnMouseMove().mouseMove(x, y);
	}

	private void keyPress(GLFWWindow window, int codepoint) {
		if (getOnKeyPress() != null)
			getOnKeyPress().keyPress(codepoint);
	}

	private void keyCode(GLFWWindow window, int key, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_REPEAT) // don't care about repeat
			return;
		if (getOnKeyCode() != null)
			getOnKeyCode().keyCode(key, action == GLFW.GLFW_PRESS, mods);
	}

	private void resized(GLFWWindow window, int w, int h) {
		setWidth(w);
		setHeight(h);
		getRenderTarget().apply(new Vector2i(w, h));
		if (getOnResize() != null)
			getOnResize().resized(w, h);
	}

	@Override
	public int getFramecount() {
		return framecount;
	}

	@Override
	public BitmapFont loadFontZIP(InputStream is) throws ResourceLoadError {
		String fntname = "<stream>";
		try {
			ZipUtil zip = new ZipUtil(is);
			List<String> lst = zip.find(".*\\.fnt$");
			if (lst.size() != 1)
				throw new ResourceLoadError("ZIP file must contain exactly one *.fnt file.  There are " + lst.size());
			fntname = lst.get(0);

			TextureLoader tl = new TextureLoader() {
				@Override
				public Texture loadTexture(String s) throws ResourceLoadError {
					return loadTextureInt(s, zip.getStream(s), false);
				}
			};

			BitmapFont f = new BitmapFont(fntname.replaceAll("\\.fnt$", ""), tl);
			f.loadFrom(Arrays.asList(IOUtils.toString(zip.getStream(fntname), StandardCharsets.UTF_8).split("\n")), "");
			fonts.add(f);
			return f;
		} catch (IOException e) {
			throw new ResourceLoadError("Could not load font from [" + fntname + "]", e);
		}
	}

	private TextureNative loadTextureInt(String name, BufferedImage img, boolean linear) throws ResourceLoadError {
		TextureNative txt = new TextureNative();
		try {
			ByteBuffer imageBuffer = null;
			WritableRaster raster;
			BufferedImage texImage;

			int texWidth = 2;
			int texHeight = 2;

			while (texWidth < img.getWidth())
				texWidth *= 2;
			while (texHeight < img.getHeight())
				texHeight *= 2;
			txt.setSize(texWidth, texHeight);
			txt.setImageSize((float) img.getWidth() / texWidth, (float) img.getHeight() / texHeight);

			ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);

			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, null);
			texImage = new BufferedImage(cm, raster, false, null);

			Graphics g = texImage.getGraphics();
			g.setColor(new Color(0f, 0f, 0f, 0f));
			g.fillRect(0, 0, texWidth, texHeight);
			g.drawImage(img, 0, 0, null);

			byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

			imageBuffer = ByteBuffer.allocateDirect(data.length);
			imageBuffer.order(ByteOrder.nativeOrder());
			imageBuffer.put(data, 0, data.length);
			imageBuffer.flip();

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			txt.bind(0);
			GLUtil.checkError();

			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texWidth, texHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);
			GLUtil.checkError();

			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			if (linear) {
				GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			} else {
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			}
			GLUtil.checkError();
		} catch (Exception th) {
			txt.destroy();
			throw new ResourceLoadError("Could not load texture [" + name + "]", th);
		}
		textures.add(txt);
		return txt;

	}

	private TextureNative loadTextureInt(String name, InputStream is, boolean linear) throws ResourceLoadError {
		try (BufferedInputStream bif = new BufferedInputStream(is)) {
			BufferedImage img = ImageIO.read(bif);
			return loadTextureInt(name, img, linear);

		} catch (IOException e) {
			throw new ResourceLoadError("Could not load texture [" + name + "]", e);
		}
	}

	@Override
	public void loadImage(String name, Image image, InputStream is, boolean linear) throws ResourceLoadError {
		TextureNative txt = loadTextureInt(name, is, linear);
		image.setDimensions((int) (txt.getImageW() * txt.getWidth()), (int) (txt.getImageH() * txt.getHeight()), txt.getImageW(), txt.getImageH());
		image.setTexture(txt);
	}

	@Override
	public void loadImage(String name, Image image, BufferedImage img, boolean linear) throws ResourceLoadError {
		TextureNative txt = loadTextureInt(name, img, linear);
		image.setDimensions((int) (txt.getImageW() * txt.getWidth()), (int) (txt.getImageH() * txt.getHeight()), txt.getImageW(), txt.getImageH());
		image.setTexture(txt);
	}

	@Override
	public PolyMesh createPolyMesh() {
		return new PolyMeshNative(this);
	}

	/**
	 * If <code>null</code> then it's back to default
	 * 
	 * @param name
	 */
	public void setCursor(String name) {
		if (name == null) {
			window.setCursor(null);
		} else {
			Cursor c = findCursor(name);
			if (c == null)
				throw new NoSuchElementException(name);
			c.apply();
		}
	}

	@Override
	public RenderTarget getDefaultRenderTarget() {
		return defaultRenderTarget;
	}

	/**
	 * Clear to BLACK
	 */
	@Override
	public void clear() {
		clear(VectorColours.TRANSPARENT_BLACK);
		//		clear(new Vector4(1, 0, 1, 1));
	}

	/**
	 * Clear to given colour. Alpha should probably be 1, unless you're looking
	 * for a weird effect of some sort
	 * 
	 * @param c
	 */
	@Override
	public void clear(Vector4 c) {
		GL11.glClearColor(c.x, c.y, c.z, c.w);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		//		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GLUtil.checkError();
	}

	public List<Cursor> getCursors() {
		return cursors;
	}

	@Override
	public Cursor loadCursor(String name, InputStream png, Vector2i hotspot) throws IOException {
		if (findCursor(name) != null)
			throw new IllegalArgumentException("Cursor " + name + " already loaded");
		Cursor c = new CursorNative(this);
		c.setName(name);
		c.setHotspotX(hotspot.x);
		c.setHotspotY(hotspot.y);
		c.setImage(ImageIO.read(png));
		cursors.add(c);
		return c;
	}

	private Cursor findCursor(String name) {
		for (Cursor c : cursors)
			if (name.equals(c.getName()))
				return c;
		return null;
	}

	public GLFWWindow getGLFWWindow() {
		return window;
	}

	@Override
	protected BitmapText constructBitmapText(Shader shader, BitmapFont font, RenderTextOptions rto, int w) {
		return new BitmapTextNative(this, (ShaderNative) shader, font, rto, w);
	}

	@Override
	public void hideCursor() {
		window.hideCursor();
	}

	@Override
	public void unhideCursor() {
		window.unhideCursor();
	}

	@Override
	public void setCursorPosition(int x, int y) {
		window.setCursorPosition(x, y);
	}

	@Override
	public void setBlend(BlendMode bm) {
		switch (bm) {
		case FBO_SEPARATE:
			GL11.glEnable(GL11.GL_BLEND);
			GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case OFF:
			GL11.glDisable(GL11.GL_BLEND);
			break;
		case ON:
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			break;
		default:
			break;
		}
	}

	/**
	 * Shuts down the app
	 */
	public void close() {
		window.setShouldClose(true);
	}

}
