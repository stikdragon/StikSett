package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.VoxelFrame;
import uk.co.stikman.sett.VoxelModel;
import uk.co.stikman.sett.game.GameObject;

public class SceneObject {

	private GameObject	gameObject;
	private GameView	view;
	private VoxelMesh	mesh;
	private VoxelModel	model;

	private float		lastTime;
	private float		time		= 0.0f;
	private float		animTime;
	private float[]		frameStarts;
	private int			curFrame	= 0;

	public SceneObject(GameView view, GameObject gameobj, VoxelModel mdl, VoxelMesh mesh) {
		if (mesh == null)
			throw new NullPointerException("Mesh is missing");
		this.view = view;
		this.gameObject = gameobj;
		this.mesh = mesh;
		this.model = mdl;
		this.animTime = 0.0f;
		if (mdl.isAnimated()) {
			frameStarts = new float[mdl.getFrames().size()];
			frameStarts[0] = 0.0f;
			animTime = mdl.getFrames().get(mdl.getFrames().size() - 1).getDuration();
			for (int i = 0; i < mdl.getFrames().size() - 1; ++i) {
				VoxelFrame fr = mdl.getFrames().get(i);
				frameStarts[i + 1] = frameStarts[i] + fr.getDuration();
				animTime += fr.getDuration();
			}
		}
	}

	public GameObject getGameObject() {
		return gameObject;
	}

	public GameView getView() {
		return view;
	}

	public VoxelMesh getMesh() {
		return mesh;
	}

	public void render(float t) {
		if (lastTime == 0.0f)
			lastTime = t;
		float dt = t - lastTime;
		lastTime = t;

		if (model.isAnimated()) {
			//
			// work out what time we're on
			//
			time += dt;
			int n = (int) (time / animTime);
			time -= n * animTime;

			int l = frameStarts.length;
			for (curFrame = 0; curFrame < l; ++curFrame) {
				if (time < frameStarts[curFrame]) {
					break;
				}
			}
			if (curFrame > 0)
				--curFrame;

			mesh.render(curFrame);
		} else {
			mesh.render(0);
		}
	}

	public void update(float dt) {

	}

}
