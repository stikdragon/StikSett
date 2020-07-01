package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.gfx.PolyMesh;
import uk.co.stikman.sett.gfx.Window3D;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector4;

public class WaterPlane {
	private PolyMesh mesh;

	public WaterPlane(Window3D window, World world) {
		mesh = window.createPolyMesh();
		Vector4 col = new Vector4(0.7f, 0.3f, 1.0f, 1.0f);
		float w = world.getWidth();
		float h = world.getHeight();

		Matrix3 m = SettApp.skewMatrix(new Matrix3());
		Vector2 a = m.multiply(new Vector2(0, 0), new Vector2());
		Vector2 b = m.multiply(new Vector2(w, 0), new Vector2());
		Vector2 c = m.multiply(new Vector2(w, h), new Vector2());
		Vector2 d = m.multiply(new Vector2(0, h), new Vector2());

		mesh.addVert(a.x, a.y, 0, a.x, a.y, col);
		mesh.addVert(b.x, b.y, 0, b.x, b.y, col);
		mesh.addVert(c.x, c.y, 0, c.x, c.y, col);
		mesh.addVert(d.x, d.y, 0, d.x, d.y, col);
		mesh.addTri(0, 1, 2);
		mesh.addTri(0, 2, 3);
	}

	public void render() {
		mesh.render(0);
	}
}
