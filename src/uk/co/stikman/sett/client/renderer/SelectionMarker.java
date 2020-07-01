package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.gfx.Shader;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class SelectionMarker {
	private VoxelMesh	mesh;
	private Vector3		position	= new Vector3();
	private Vector3		tv			= new Vector3();

	public SelectionMarker(GameView gameview, VoxelMesh mesh) {
		this.mesh = mesh;
	}

	public void render(Shader shd, Matrix3 skew) {
		shd.getUniform("offset").bindVec3(skew.multiply(position, tv));
		mesh.render();
	}

	public void setPosition(Vector3 pos) {
		position.copy(pos);
	}

}
