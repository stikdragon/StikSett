package uk.co.stikman.sett.gfx;

import java.util.Collection;

import org.lwjgl.opengl.GL20;

import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public interface Shader {

	class Uniform {
		private int location;

		public Uniform(int id) {
			this.location = id;
		}

		public int getLocation() {
			return location;
		}

		public void bindMat4(Matrix4 m) {
			GL20.glUniformMatrix4fv(location, false, m.asFloatArray());
		}

		public void bindVec2(Vector2 v) {
			GL20.glUniform2f(location, v.x, v.y);
		}

		public void bindInt(int i) {
			GL20.glUniform1i(location, i);
		}

		public void bindFloat(float f) {
			GL20.glUniform1f(location, f);
		}

		public void bindVec3(float x, float y, float z) {
			GL20.glUniform3f(location, x, y, z);
		}

		public void bindVec3(Vector3 v) {
			GL20.glUniform3f(location, v.x, v.y, v.z);
		}

		public void bindTexture(Texture t, int unit) {
			t.bindUniform(unit, location);
		}

		public void bindVec2(float x, float y) {
			GL20.glUniform2f(location, x, y);
		}

		public void bindVec4(float x, float y, float z, float w) {
			GL20.glUniform4f(location, x, y, z, w);
		}

		public void bindBoolean(boolean b) {
			GL20.glUniform1i(location, b ? 1 : 0);
		}

		public void bindVec4(Vector4 v) {
			bindVec4(v.x, v.y, v.z, v.w);
		}
	}

	Shader load(String name, String frag, String vert);

	Collection<String> getActiveAttribs();

	/**
	 * Returns -1 if it's missing (or an exception if allowmissing is false)
	 * 
	 * @param name
	 * @param allowmissing
	 * @return
	 */
	Uniform getUniform(String name, boolean allowmissing);

	Uniform getUniform(String name);

	void use();

	void destroy();

	boolean hasUniform(String name);

}