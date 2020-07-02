package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.utils.math.Vector3;

public class Plane {

	private Vector3	point;
	private Vector3	normal;

	/**
	 * define a plane as a normal and a point on it
	 * 
	 * @param normal
	 * @param point
	 */
	public Plane(Vector3 normal, Vector3 point) {
		this.normal = normal.normalize();
		this.point = point;
	}

	/**
	 * returns <code>null</code> if no interseciton
	 * 
	 * @param ray
	 * @param out
	 * @return
	 */
	public Vector3 intersect(Ray ray, Vector3 out) {
		Vector3 n = normal;
		float mu = ray.direction.dot(n);
		if (mu == 0.0f)
			return null;
		float d = n.dot(point);
		float t = (d - n.dot(ray.point)) / mu;
		if (t < 0.0f)
			return null;
		return out.copy(ray.point).addMult(ray.direction, t);
	}

}
