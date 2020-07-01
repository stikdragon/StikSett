package uk.co.stikman.sett.game;

import java.util.Arrays;

import uk.co.stikman.sett.client.renderer.Ray;
import uk.co.stikman.utils.math.Vector3;

public class Rectaloid {
	public Vector3			a			= new Vector3();
	public Vector3			b			= new Vector3();

	private Vector3			tv1			= new Vector3();
	private static Vector3	normals[]	= new Vector3[6];
	private Vector3[]		origins		= new Vector3[6];
	private boolean			invalid		= true;

	static {
		normals[0] = new Vector3(0, 1, 0);
		normals[1] = new Vector3(0, -1, 0);
		normals[2] = new Vector3(-1, 0, 0);
		normals[3] = new Vector3(1, 0, 0);
		normals[4] = new Vector3(0, 0, 1);
		normals[5] = new Vector3(0, 0, -1);
	}

	public Rectaloid() {
		for (int i = 0; i < 6; i++)
			origins[i] = new Vector3();
		invalid = true;
	}

	private void genVecs() {
		//
		// points on opposite corners for the 6 faces
		//
		origins[0] = a;
		origins[3] = a;
		origins[5] = a;

		origins[1] = b;
		origins[2] = b;
		origins[4] = b;
		invalid = false;
	}

	public void set(float x0, float y0, float z0, float sx, float sy, float sz) {
		a.set(x0, y0, z0);
		b.set(sx + x0, sy + y0, sz + z0);
		invalid = true;
	}

	/**
	 * returns <code>out</code> if there is an intersection with one of the
	 * faces. Out will be the closest intersection to the origin of
	 * <code>ray</code> (ie. <code>ray.point</code>). <code>ray.direction</code>
	 * must be normalised
	 * 
	 * @param ray
	 * @param out
	 * @return
	 */
	public Vector3 intersect(Ray ray, Vector3 out) {
		if (invalid)
			genVecs();
		//
		// intersect with all faces, return closest
		//
		float bestT = Float.MAX_VALUE;
		for (int i = 0; i < 6; ++i) {

			Vector3 n = normals[i];
			float mu = ray.direction.dot(n);
			if (mu != 0) {
				float d = n.dot(origins[i]); // dist from origin
				float t = (d - ray.point.dot(n)) / mu;
				if (t >= 0.0f) {
					Vector3 p = tv1.copy(ray.point).addMult(ray.direction, t);

					//
					// gives us a point on the ray, is this inside the face?
					// can shortcut a lot of the maths since we're always aligned with
					// axes
					//
					switch (i) {
					case 4: // XY faces
					case 5:
						if (contained(p.x, p.y, a.x, a.y, b.x, b.y)) {
							if (t < bestT) {
								out.copy(p);
								bestT = t;
							}
						}
						break;
					case 0: // XZ
					case 1:
						if (contained(p.x, p.z, a.x, a.z, b.x, b.z)) {
							if (t < bestT) {
								out.copy(p);
								bestT = t;
							}
						}
						break;
					case 2: // YZ
					case 3:
						if (contained(p.y, p.z, a.y, a.z, b.y, b.z)) {
							if (t < bestT) {
								out.copy(p);
								bestT = t;
							}
						}
						break;
					}
				}
			}
		}
		if (bestT == Float.MAX_VALUE)
			return null;
		return out;
	}

	public Vector3 intersect2(Ray ray, Vector3 out) {
		if (invalid)
			genVecs();
		//
		// intersect with all faces, return closest
		//
		float bestT = Float.MAX_VALUE;
		for (int i = 0; i < 6; ++i) {
			Vector3 n = normals[i];
			float d = ray.direction.dot(n);
			if (d != 0.0f) {
				float t = origins[i].sub(ray.point, tv1).dot(n) / d;
				if (t >= 0.0f) {
					Vector3 p = tv1.copy(ray.point).addMult(ray.direction, t);

					//
					// gives us a point on the ray, is this inside the face?
					// can shortcut a lot of the maths since we're always aligned with
					// axes
					//
					switch (i) {
					case 4: // XY faces
					case 5:
						if (contained(p.x, p.y, a.x, a.y, b.x, b.y)) {
							if (t < bestT) {
								out.copy(p);
								bestT = t;
							}
						}
						break;
					case 0: // XZ
					case 1:
						if (contained(p.x, p.z, a.x, a.z, b.x, b.z)) {
							if (t < bestT) {
								out.copy(p);
								bestT = t;
							}
						}
						break;
					case 2: // YZ
					case 3:
						if (contained(p.y, p.z, a.y, a.z, b.y, b.z)) {
							if (t < bestT) {
								out.copy(p);
								bestT = t;
							}
						}
						break;
					}
				}
			}
		}
		if (bestT == Float.MAX_VALUE)
			return null;
		return out;
	}

	/**
	 * see [X,Y] if contained in rect formed from [ax, ay] to [ax+sx, ay+sy]
	 * 
	 * @param x
	 * @param y
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return
	 */
	private boolean contained(float x, float y, float ax, float ay, float bx, float by) {
		boolean b = true;
		if (bx < ax)
			b &= x <= ax && x >= bx;
		else
			b &= x <= bx && x >= ax;
		if (by < ay)
			b &= y <= ay && y >= by;
		else
			b &= y <= by && y >= ay;
		return b;
	}

	@Override
	public String toString() {
		return a + "  to  " + b;
	}

}
