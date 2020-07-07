package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.BaseGame;
import uk.co.stikman.sett.MarkerType;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.gfx.Shader;
import uk.co.stikman.sett.gfx.Shader.Uniform;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class SelectionMarker {
	private VoxelMesh	caret;
	private VoxelMesh	caret2;
	private VoxelMesh	flag;
	private VoxelMesh	bighouse;
	private VoxelMesh	house;

	private Vector3[]	positions	= new Vector3[7];
	private Vector3		tv			= new Vector3();
	private Vector3		tv2			= new Vector3();
	private BaseGame	game;
	private MarkerType	markerType;
	private Matrix4		tm			= new Matrix4();
	private GameView	gameview;
	private float		time		= 0;
	private Vector2i	position	= new Vector2i();

	public SelectionMarker(GameView gameview, VoxelMesh caret, VoxelMesh caret2, VoxelMesh small, VoxelMesh large, VoxelMesh flag) {
		this.gameview = gameview;
		this.caret = caret;
		this.caret2 = caret2;
		this.house = small;
		this.bighouse = large;
		this.flag = flag;
		this.game = gameview.getGame();
		for (int i = 0; i < 7; ++i)
			positions[i] = new Vector3();
	}

	public void render(Shader shd, Matrix3 skew) {
		MarkerType mt = markerType;
		if (mt == null)
			mt = MarkerType.NONE;
		VoxelMesh m = caret;
		switch (mt) {
		case FLAG:
			m = flag;
			break;
		case LARGE_HOUSE:
			m = bighouse;
			break;
		case MINE:
			break;
		case SMALL_HOUSE:
			m = house;
			break;
		}

		//
		// rotate it back so it always faces the user, ish
		//
		Uniform u = shd.getUniform("model");
		tm.makeRotation(0, 0, 1, -gameview.getRotation().x + 3.14159f / 3.0f);
		u.bindMat4(tm);
		shd.getUniform("offset").bindVec3(skew.multiply(positions[0], tv));
		m.render(0);

		for (int i = 1; i < 7; ++i) {
			shd.getUniform("offset").bindVec3(skew.multiply(tv2.copy(positions[i]), tv));
			if (i == 4 && mt != MarkerType.FLAG && mt != MarkerType.NONE) {
				tm.makeRotation(0, 0, 1, -gameview.getRotation().x + 3.14159f / 3.0f);
				u.bindMat4(tm);
				flag.render(0);
			} else {
				tm.makeRotation(0, 0, 1, time * 3);
				u.bindMat4(tm);
				caret2.render(0);
			}
		}
	}

	public void setPosition(Vector2i pos) {
		this.position.copy(pos);
		TerrainNode[] n = game.getWorld().getTerrain().getNeighbours(pos.x, pos.y);
		markerType = game.getPermissableMarkerFor(pos.x, pos.y);
		for (int i = 0; i < 7; ++i)
			positions[i].set(n[i].getX(), n[i].getY(), n[i].getHeight());
	}

	public void update(float dt) {
		time += dt;
	}
	
	public Vector2i getPosition() {
		return position;
	}

}
