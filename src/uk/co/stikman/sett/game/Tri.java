package uk.co.stikman.sett.game;

import uk.co.stikman.sett.client.renderer.Ray;
import uk.co.stikman.utils.math.Vector3;

public class Tri {
	public Vector3	a	= new Vector3();
	public Vector3	b	= new Vector3();
	public Vector3	c	= new Vector3();

	private Vector3	tv1	= new Vector3();
	private Vector3	tv2	= new Vector3();
	private Vector3	tv3	= new Vector3();
	private Vector3	tv4	= new Vector3();

	public Vector3 intersect(Ray ray, Vector3 out) {
		Vector3 e1 = Vector3.sub(b, a, tv1);
		Vector3 e2 = Vector3.sub(c, a, tv2);
		Vector3 n = Vector3.cross(e2, e1, tv4);
		float mu = ray.direction.dot(n);
		if (mu == 0.0f)
			return null;
		float d = n.dot(a);
		float t = (d - n.dot(ray.point)) / mu;
		if (t < 0.0f)
			return null;
		out.copy(ray.point).addMult(ray.direction, t);

		//
		// see if internal to tri
		//
		Vector3 e = Vector3.sub(b, a, tv1);
		Vector3 p = Vector3.sub(out, a, tv2);
		Vector3 cp = Vector3.cross(p, e, tv3);
		if (n.dot(cp) < 0.0f)
			return null;

		e = Vector3.sub(c, b, tv1);
		p = Vector3.sub(out, b, tv2);
		cp = Vector3.cross(p, e, tv3);
		if (n.dot(cp) < 0.0f)
			return null;

		e = Vector3.sub(a, c, tv1);
		p = Vector3.sub(out, c, tv2);
		cp = Vector3.cross(p, e, tv3);
		if (n.dot(cp) < 0.0f)
			return null;

		return out;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tri:\n");
		sb.append(a).append("\n");
		sb.append(b).append("\n");
		sb.append(c);
		return sb.toString();
	}

}
