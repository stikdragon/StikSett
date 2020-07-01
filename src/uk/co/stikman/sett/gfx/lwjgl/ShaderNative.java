package uk.co.stikman.sett.gfx.lwjgl;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import uk.co.stikman.sett.gfx.Shader;

public class ShaderNative implements Shader {

	private static Shader				currentShader;

	private int							program;
	private Map<String, Integer>		attribs		= new HashMap<>();
	private Map<String, Shader.Uniform>	uniforms	= new HashMap<>();

	private Set<String>					absent		= new HashSet<>();

	private Window3DNative				window;

	public ShaderNative(Window3DNative window) {
		this.window = window;
	}

	@Override
	public Shader load(String name, String frag, String vert) {
		int fragmentShader = getShader(GL20.GL_FRAGMENT_SHADER, frag);
		int vertexShader = getShader(GL20.GL_VERTEX_SHADER, vert);
		program = GL20.glCreateProgram();

		String line = vert.split("\\r?\\n")[0];
		if (!line.startsWith("//ATTRIB:"))
			throw new IllegalArgumentException("Shader [" + name + "] must start with //ATTRIB:");
		line = line.substring(9);
		for (String attr : line.split(","))
			bindAttrib(attr, window.registerAttribLocation(attr));

		GL20.glAttachShader(program, vertexShader);
		GL20.glAttachShader(program, fragmentShader);
		GL20.glLinkProgram(program);
		GLUtil.checkError();

		if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) != GL11.GL_TRUE)
			throw new RuntimeException("Could not link shader " + name + ": " + GL20.glGetProgramInfoLog(program));

		GL20.glUseProgram(program);
		GLUtil.checkError();

		return this;
	}

	@Override
	public Collection<String> getActiveAttribs() {
		int cnt = GL20.glGetProgrami(program, GL20.GL_ACTIVE_ATTRIBUTES);
		List<String> set = new ArrayList<>();
		for (int i = 0; i < cnt; ++i) {
			IntBuffer size = BufferUtils.createIntBuffer(1);
			IntBuffer type = BufferUtils.createIntBuffer(1);
			set.add(GL20.glGetActiveAttrib(program, i, size, type));
		}
		return set;
	}

	private void bindAttrib(String name, int idx) {
		GL20.glBindAttribLocation(program, idx, name);
	}

	private int getShader(int type, String source) {
		int shader = GL20.glCreateShader(type);

		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);

		String s = GL20.glGetShaderInfoLog(shader);
		if (s != null && s.length() > 0)
			System.out.println(s);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
			throw new RuntimeException(s);

		return shader;
	}

	@Override
	public Shader.Uniform getUniform(String name, boolean allowmissing) {
		Shader.Uniform u = uniforms.get(name);
		if (u != null)
			return u;
		int x = GL20.glGetUniformLocation(program, name);
		if (x == -1) {
			if (allowmissing)
				return null;
			throw new RuntimeException("Uniform " + name + " not found");
		}
		u = new Shader.Uniform(x);
		uniforms.put(name, u);
		return u;
	}

	@Override
	public Shader.Uniform getUniform(String name) {
		return getUniform(name, false);
	}

	@Override
	public void use() {
		GL20.glUseProgram(program);
		window.setCurrentShader(this);
	}

	@Override
	public void destroy() {
		// TODO: shader destroy
		window.setCurrentShader(null);
	}

	@Override
	public boolean hasUniform(String name) {
		if (absent.contains(name))
			return false;
		Uniform u = getUniform(name, true);
		if (u == null) {
			absent.add(name);
			return false;
		}
		return true;
	}

}
