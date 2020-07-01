package uk.co.stikman.sett.gfx.lwjgl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.NativeResource;

public class GLFWWindow {

	public interface KeyCodeHandler {
		void invoke(GLFWWindow window, int key, int scancode, int action, int mods);
	}

	public interface KeyPressHandler {
		void invoke(GLFWWindow window, int codepoint);
	}

	public interface ResizeHandler {
		void invoke(GLFWWindow window, int width, int height);
	}

	public interface MouseMoveHandler {
		void invoke(GLFWWindow window, int x, int y);
	}

	public interface MouseDownHandler {
		void invoke(GLFWWindow window, int x, int y, int button);
	}

	public interface MouseUpHandler {
		void invoke(GLFWWindow window, int x, int y, int button);
	}

	public interface MouseWheelHandler {
		void invoke(GLFWWindow window, int dy);
	}

	public interface MouseLeaveHandler {
		void invoke(GLFWWindow window);
	}

	private long						handle;
	private List<KeyPressHandler>		keyPressHandlers	= new ArrayList<>();
	private List<KeyCodeHandler>		keyCodeHandlers		= new ArrayList<>();
	private List<ResizeHandler>			resizeHandlers		= new ArrayList<>();
	private List<MouseMoveHandler>		mouseMoveHandlers	= new ArrayList<>();
	private List<MouseDownHandler>		mouseDownHandlers	= new ArrayList<>();
	private List<MouseUpHandler>		mouseUpHandlers		= new ArrayList<>();
	private List<MouseWheelHandler>		mouseWheelHandlers	= new ArrayList<>();
	private List<MouseLeaveHandler>		mouseLeaveHandlers	= new ArrayList<>();
	private GLFWKeyCallback				keyCodeCallback;
	private GLFWCharCallback			keyPressCallback;
	private GLFWWindowSizeCallback		resizeCallback;
	private GLFWCursorPosCallback		mouseMoveCallback;
	private GLFWMouseButtonCallback		mouseButtonCallback;

	private int							width;
	private int							height;
	protected int						lastMouseX;
	protected int						lastMouseY;
	private GLFWScrollCallback			mouseWheelCallback;
	private GLFWWindowIconifyCallback	iconifyHandler;
	protected boolean					minimized;
	private GLFWCursorEnterCallback		mouseLeaveCallback;

	public GLFWWindow(GLFWWindowOptions opts) {

		String[] bits = opts.getContextVersion().split("\\.");
		int maj = Integer.parseInt(bits[0]);
		int min = Integer.parseInt(bits[1]);

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, opts.isVisible() ? GL11.GL_TRUE : GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, opts.isResizable() ? GL11.GL_TRUE : GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, maj);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, min);
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, opts.getSamples());
		switch (opts.getProfile()) {
		case ANY_PROFILE:
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_ANY_PROFILE);
			break;
		case COMPAT_PROFILE:
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
			break;
		case CORE_PROFILE:
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
			break;
		}

		handle = GLFW.glfwCreateWindow(opts.getWidth(), opts.getHeight(), opts.getCaption(), 0, 0);
		if (handle == 0)
			throw new RuntimeException("Failed to create the GLFW window");
		width = opts.getWidth();
		height = opts.getHeight();

		GLFW.glfwSetWindowIconifyCallback(handle, getIconifyHandler());

		resizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				GLFWWindow.this.width = width;
				GLFWWindow.this.height = height;
				for (ResizeHandler x : resizeHandlers)
					x.invoke(GLFWWindow.this, width, height);
			}
		};
		GLFW.glfwSetWindowSizeCallback(handle, resizeCallback);

	}

	private GLFWWindowIconifyCallback getIconifyHandler() {
		if (iconifyHandler == null) {
			iconifyHandler = new GLFWWindowIconifyCallback() {
				@Override
				public void invoke(long handle, boolean iconified) {
					minimized = iconified;
				}
			};
		}
		return iconifyHandler;
	}

	public void destroy() {
		//
		// Remove handlers
		//
		releaseRetained(iconifyHandler);
		releaseRetained(keyCodeCallback);
		releaseRetained(keyPressCallback);
		releaseRetained(resizeCallback);
		releaseRetained(mouseLeaveCallback);

		GLFW.glfwDestroyWindow(handle);
		handle = 0;
	}

	private void releaseRetained(NativeResource r) {
		if (r != null)
			r.free();
	}

	public long getHandle() {
		return handle;
	}

	public void addKeyCodeHandler(final KeyCodeHandler handler) {
		keyCodeHandlers.add(handler);
		if (keyCodeCallback == null) {
			keyCodeCallback = new GLFWKeyCallback() {
				@Override
				public void invoke(long window, int key, int scancode, int action, int mods) {
					for (KeyCodeHandler x : keyCodeHandlers)
						x.invoke(GLFWWindow.this, key, scancode, action, mods);
				}
			};
			GLFW.glfwSetKeyCallback(handle, keyCodeCallback);
		}
	}

	public void addKeyPressHandler(final KeyPressHandler handler) {
		keyPressHandlers.add(handler);
		if (keyPressCallback == null) {
			keyPressCallback = new GLFWCharCallback() {
				@Override
				public void invoke(long window, int codepoint) {
					for (KeyPressHandler x : keyPressHandlers)
						x.invoke(GLFWWindow.this, codepoint);
				}
			};
			GLFW.glfwSetCharCallback(handle, keyPressCallback);
		}

	}

	public void addMouseMoveHandler(final MouseMoveHandler handler) {
		mouseMoveHandlers.add(handler);
		if (mouseMoveCallback == null) {
			mouseMoveCallback = new GLFWCursorPosCallback() {
				@Override
				public void invoke(long window, double xpos, double ypos) {
					lastMouseX = (int) xpos;
					lastMouseY = (int) ypos;
					for (MouseMoveHandler x : mouseMoveHandlers)
						x.invoke(GLFWWindow.this, (int) xpos, (int) ypos);
				}
			};
			GLFW.glfwSetCursorPosCallback(handle, mouseMoveCallback);
		}
	}

	public void addMouseDownHandler(final MouseDownHandler handler) {
		mouseDownHandlers.add(handler);
		if (mouseButtonCallback == null)
			addMouseButtonCallback();
	}

	public void addMouseUpHandler(final MouseUpHandler handler) {
		mouseUpHandlers.add(handler);
		if (mouseButtonCallback == null)
			addMouseButtonCallback();
	}

	public void addMouseWheelHandler(final MouseWheelHandler handler) {
		mouseWheelHandlers.add(handler);
		if (mouseWheelCallback == null) {
			mouseWheelCallback = new GLFWScrollCallback() {
				@Override
				public void invoke(long window, double xoffset, double yoffset) {
					for (MouseWheelHandler h : mouseWheelHandlers)
						h.invoke(GLFWWindow.this, (int) yoffset);
				}
			};
			GLFW.glfwSetScrollCallback(handle, mouseWheelCallback);
		}
	}

	public void addMouseLeaveHandler(final MouseLeaveHandler handler) {
		mouseLeaveHandlers.add(handler);
		if (mouseLeaveCallback == null) {
			mouseLeaveCallback = new GLFWCursorEnterCallback() {
				@Override
				public void invoke(long window, boolean entered) {
					if (!entered)
						for (MouseLeaveHandler h : mouseLeaveHandlers)
							h.invoke(GLFWWindow.this);
				}
			};
			GLFW.glfwSetCursorEnterCallback(handle, mouseLeaveCallback);
		}
	}

	private void addMouseButtonCallback() {
		mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				if (action == GLFW.GLFW_PRESS) {
					for (MouseDownHandler x : mouseDownHandlers)
						x.invoke(GLFWWindow.this, lastMouseX, lastMouseY, button);
				} else if (action == GLFW.GLFW_RELEASE) {
					for (MouseUpHandler x : mouseUpHandlers)
						x.invoke(GLFWWindow.this, lastMouseX, lastMouseY, button);
				}
			}
		};
		GLFW.glfwSetMouseButtonCallback(handle, mouseButtonCallback);
	}

	public void addResizeHandler(final ResizeHandler handler) {
		resizeHandlers.add(handler);
	}

	public void show() {
		GLFW.glfwShowWindow(handle);
		int width = getWidth();
		int height = getHeight();
		for (ResizeHandler x : resizeHandlers)
			x.invoke(GLFWWindow.this, width, height);
	}

	public void swapBuffers() {
		GLFW.glfwSwapBuffers(handle);
	}

	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(handle);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setPos(int x, int y) {
		GLFW.glfwSetWindowPos(handle, x, y);
	}

	public void setShouldClose(boolean b) {
		GLFW.glfwSetWindowShouldClose(handle, b);
	}

	public void setSize(int w, int h) {
		GLFW.glfwSetWindowSize(handle, w, h);
	}

	public boolean isMinimized() {
		return minimized;
	}

	public GLFWCursor createCursor(BufferedImage img, int hotx, int hoty) {
		GLFWCursor cur = new GLFWCursor();
		cur.setHotSpot(hotx, hoty);
		if (img.getWidth() != 16 || img.getHeight() != 16)
			throw new IllegalArgumentException("Cursor must be 16x16 pixels");

		ByteBuffer buf = BufferUtils.createByteBuffer(16 * 16 * 4);
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int px = img.getRGB(x, y);
				buf.put((byte) ((px >> 16) & 0xff));
				buf.put((byte) ((px >> 8) & 0xff));
				buf.put((byte) ((px >> 0) & 0xff));
				buf.put((byte) ((px >> 24) & 0xff));
			}
		}
		buf.flip();
		GLFWImage gi = GLFWImage.malloc();
		gi.set(16, 16, buf);
		cur.setHandle(GLFW.glfwCreateCursor(gi, 0, 0));
		return cur;
	}

	public void setCursor(GLFWCursor cur) {
		if (cur == null)
			GLFW.glfwSetCursor(getHandle(), 0);
		else
			GLFW.glfwSetCursor(getHandle(), cur.getHandle());
	}

	public void hideCursor() {
		GLFW.glfwSetInputMode(getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
	}

	public void unhideCursor() {
		GLFW.glfwSetInputMode(getHandle(),  GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	}

	public void setCursorPosition(int x, int y) {
		GLFW.glfwSetCursorPos(getHandle(), x, y);
	}

}
