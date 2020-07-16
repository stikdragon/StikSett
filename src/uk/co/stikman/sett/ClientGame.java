package uk.co.stikman.sett;

import java.io.IOException;
import java.util.List;

import uk.co.stikman.sett.game.Building;
import uk.co.stikman.sett.game.Flag;
import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.Player;
import uk.co.stikman.sett.game.Road;
import uk.co.stikman.sett.game.Rock;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.Tree;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gameevents.GameEvent;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.utils.StikDataInputStream;

public class ClientGame extends Game {

	private final ObjectConstructors OBJECT_CONSTRUCTORS = new ObjectConstructors();

	public ClientGame(SettApp app) {
		super(app);
		OBJECT_CONSTRUCTORS.add(Building.class, () -> new Building(this));
		OBJECT_CONSTRUCTORS.add(Flag.class, () -> new Flag(this));
		OBJECT_CONSTRUCTORS.add(Rock.class, () -> new Rock(this));
		OBJECT_CONSTRUCTORS.add(Tree.class, () -> new Tree(this));
		OBJECT_CONSTRUCTORS.add(Player.class, () -> new Player(this));
	}

	public void fromStream(StikDataInputStream in) throws IOException, ResourceLoadError {
		SettInputStream str = new SettInputStream(in);
		str.setObjectConstructors(OBJECT_CONSTRUCTORS);

		//
		// this is fairly complex to do
		//
		WorldParameters params = new WorldParameters();
		params.fromStream(str);
		World w = new World(this);
		w.setParams(params);
		setName(str.readString());
		this.world = w;
		loadResources();

		int cnt = str.readInt();
		while (cnt-- > 0) {
			Player p = str.readObject(Player.class);
			players.put(p.getName(), p);
		}

		// TODO: validate these
		List<String> modelNames = str.readStringList();
		List<String> buildingDefNames = str.readStringList();
		List<String> sceneryDefNames = str.readStringList();

		cnt = str.readInt();
		while (cnt-- > 0)
			w.getBuildings().put(str.readObject(Building.class));

		cnt = str.readInt();
		while (cnt-- > 0)
			w.getFlags().put(str.readObject(Flag.class));

		cnt = str.readInt();
		while (cnt-- > 0)
			w.getRoads().put(str.readObject(Road.class));

		Terrain t = w.getTerrain();
		int nw = str.readInt();
		int nh = str.readInt();
		for (int y = 0; y < nh; ++y) {
			for (int x = 0; x < nw; ++x) {
				TerrainNode n = t.get(x, y);
				int ownr = str.readByte();
				float alt = str.readFloat();
				int typ = str.readByte();
				IsNodeObject no = str.readObject(IsNodeObject.class);
				t.get(x, y).set(alt, typ, ownr, no);
			}
		}

		//
		// now there's a bunch of stuff to map up
		//
		w.getRoads().forEach(this::applyRoadToTerrain);
		w.getTerrain().recalculateNormals();
	}

	public void update(float dt) {
		
	}

}
