package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.utils.math.Vector3;

public class Ray {
	public Vector3	point		= new Vector3();
	public Vector3	direction	= new Vector3();
	private Vector3	tv			= new Vector3();

	public Ray(Vector3 point, Vector3 direction) {
		super();
		this.point = point;
		this.direction = direction;
	}

	public Ray() {
	}

	public Vector3 interpolate(float t, Vector3 out) {
		return direction.multiply(t, out).add(point);
	}

	@Override
	public String toString() {
		return "Ray [point=" + point + ", direction=" + direction + "]";
	}

	public Vector3 intersect(Vector3 O, Vector3 N, Vector3 res) {
		float f = N.dot(direction);
		if (f == 0.0f)
			return null;
		Vector3.sub(O, point, tv);
		f = tv.dot(N) / f;
		if (f < 0)
			return null;
		return res.copy(point).addMult(direction, f);
	}

}
