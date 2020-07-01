package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.game.IsNodeObject;

public class SceneObject {

	private IsNodeObject	gameObject;
	private GameView		view;
	private VoxelMesh		mesh;

	public SceneObject(GameView view, IsNodeObject gameobj, VoxelMesh mesh) {
		if (mesh == null)
			throw new NullPointerException("Mesh is missing");
		this.view = view;
		this.gameObject = gameobj;
		this.mesh = mesh;
	}

	public IsNodeObject getGameObject() {
		return gameObject;
	}

	public GameView getView() {
		return view;
	}

	public VoxelMesh getMesh() {
		return mesh;
	}

	public void render() {
		mesh.render();
	}

}
