package uk.co.stikman.sett.gfx.lwjgl;

import java.util.HashMap;
import java.util.Map;

public class GLFWWindowOptions {

	public enum OpenGLProfile {
		ANY_PROFILE, COMPAT_PROFILE, CORE_PROFILE
	}

	private boolean					visible			= true;
	private boolean					resizable		= true;
	private String					contextVersion	= "1.0";
	private OpenGLProfile			profile			= OpenGLProfile.ANY_PROFILE;
	private Map<Integer, Integer>	extraHints		= new HashMap<>();
	private int						width			= 300;
	private int						height			= 300;
	private int						samples;
	private String					caption			= "";

	public GLFWWindowOptions setVisible(boolean b) {
		visible = b;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isResizable() {
		return resizable;
	}

	public GLFWWindowOptions setResizable(boolean resizable) {
		this.resizable = resizable;
		return this;
	}

	public String getContextVersion() {
		return contextVersion;
	}

	public GLFWWindowOptions setContextVersion(String s) {
		if (!s.matches("^[0-9]+\\.[0-9]+$"))
			throw new IllegalArgumentException("Context Version must be in the form Major.Minor, eg. 1.1, 3.2");
		this.contextVersion = s;
		return this;
	}

	public OpenGLProfile getProfile() {
		return profile;
	}

	public GLFWWindowOptions setProfile(OpenGLProfile profile) {
		this.profile = profile;
		return this;
	}

	/**
	 * Add a specific GLFW hint that's not supported by this config object
	 * 
	 * @param target
	 * @param hint
	 */
	public void addGLFWHint(int target, int hint) {
		extraHints.put(target, hint);
	}

	public Map<Integer, Integer> getExtraHints() {
		return extraHints;
	}

	public int getWidth() {
		return width;
	}

	public GLFWWindowOptions setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public GLFWWindowOptions setHeight(int height) {
		this.height = height;
		return this;
	}

	public GLFWWindowOptions setSize(int w, int h) {
		width = w;
		height = h;
		return this;
	}

	public GLFWWindowOptions setSamples(int samples) {
		this.samples = samples;
		return this;
	}

	public int getSamples() {
		return samples;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

}
