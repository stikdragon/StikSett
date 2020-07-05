package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.VoxelModel;
import uk.co.stikman.sett.game.IsNodeObject;

public class SceneObject {

	private IsNodeObject	gameObject;
	private GameView		view;
	private VoxelMesh		mesh;
	private float			lastTime;
	private VoxelModel		model;

	public SceneObject(GameView view, IsNodeObject gameobj, VoxelModel mdl, VoxelMesh mesh) {
		if (mesh == null)
			throw new NullPointerException("Mesh is missing");
		this.view = view;
		this.gameObject = gameobj;
		this.mesh = mesh;
		this.model = mdl;
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

	public void render(float time) {
		if (lastTime == 0.0f)
			lastTime = time;
		float dt = time - lastTime;

		if (model.isAnimated()) {
			int n = (int) (time * 1.0f);
			n %= model.getFrames().size();
			mesh.render(n);
		} else {
			mesh.render(0);
		}
	}

	public void update(float dt) {

	}

}
