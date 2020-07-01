package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.gfx.PolyMesh;
import uk.co.stikman.sett.gfx.Shader;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.utils.math.Vector3;

public class DebugRay {

	private Vector3					dir		= new Vector3(0, 0, 0);
	private PolyMesh				mesh;
	private boolean					invalid;
	private Vector3					origin	= new Vector3();
	private Window3D window;

	protected DebugRay(Window3D window) {
		super();
		this.window = window;
		invalid = true;
	}

	public void setVector(Vector3 v) {
		this.dir = new Vector3(v);
		invalid = true;
	}

	public void setOrigin(Vector3 orig) {
		this.origin = new Vector3(orig);
		invalid = true;
	}

	public PolyMesh getMesh() {
		if (mesh == null)
			mesh = window.createPolyMesh();

		if (invalid) {

			Vector3 u = dir.multiply(0.0f, new Vector3()).add(origin);
			Vector3 v = dir.multiply(4000.0f, new Vector3()).add(origin);

			float sz = 0.1f;
			int v0 = mesh.addVert(u.x - sz, u.y - sz, u.z, 0, 0, VectorColours.RED);
			int v1 = mesh.addVert(u.x - sz, u.y + sz, u.z, 0, 0, VectorColours.RED);
			int v2 = mesh.addVert(u.x + sz, u.y + sz, u.z, 0, 0, VectorColours.RED);
			int v3 = mesh.addVert(u.x + sz, u.y - sz, u.z, 0, 0, VectorColours.RED);

			int v4 = mesh.addVert(v.x - sz, v.y - sz, v.z, 0, 0, VectorColours.RED);
			int v5 = mesh.addVert(v.x - sz, v.y + sz, v.z, 0, 0, VectorColours.RED);
			int v6 = mesh.addVert(v.x + sz, v.y + sz, v.z, 0, 0, VectorColours.RED);
			int v7 = mesh.addVert(v.x + sz, v.y - sz, v.z, 0, 0, VectorColours.RED);

			mesh.addTri(v0, v1, v2);
			mesh.addTri(v0, v2, v3);
			mesh.addTri(v3, v2, v6);
			mesh.addTri(v3, v6, v7);
			mesh.addTri(v7, v6, v5);
			mesh.addTri(v7, v5, v4);
			mesh.addTri(v4, v5, v1);
			mesh.addTri(v4, v1, v0);
			mesh.addTri(v0, v3, v7);
			mesh.addTri(v0, v7, v4);
			mesh.addTri(v2, v1, v5);
			mesh.addTri(v2, v5, v6);

			invalid = false;
		}

		return mesh;
	}

	public void render(Shader shader) {
		shader.getUniform("offset").bindVec3(0, 0, 0);		
		getMesh().render(0);
	}

	public void destroy() {
		if (mesh != null)
			mesh.destroy();
	}

}
