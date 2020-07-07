package uk.co.stikman.sett;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.co.stikman.sett.game.BuildingType;
import uk.co.stikman.sett.game.Flag;
import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.ObstructionType;
import uk.co.stikman.sett.game.Player;
import uk.co.stikman.sett.game.Road;
import uk.co.stikman.sett.game.SceneryType;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.util.SettUtil;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class BaseGame {

	public static final int						TERRAIN_GRASS		= 0;
	public static final int						TERRAIN_DESERT		= 1;
	public static final int						TERRAIN_MOUNTAIN	= 2;
	public static final int						TERRAIN_ICE			= 3;
	public static final int						TERRAIN_WATER		= 4;

	private static final Vector3				UP					= new Vector3(0, 0, 1);

	protected transient FileSource				files;
	protected transient Map<String, VoxelModel>	models				= new HashMap<>();
	protected Map<String, Player>				players				= new HashMap<>();
	protected transient VoxelPalette			palette;
	protected World								world;
	private transient AtomicInteger				sequence			= new AtomicInteger(0);
	private String								name;
	private transient SettApp					app;

	public BaseGame(SettApp app) {
		this.app = app;
		files = Resources::getFileWild;
	}

	public FileSource getFileSource() {
		return files;
	}

	public String getTextFile(String name) throws IOException {
		try (InputStream is = files.get(name)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

	public Map<String, VoxelModel> getModels() {
		return models;
	}

	public VoxelPalette getVoxelPalette() {
		return palette;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int nextId() {
		return sequence.incrementAndGet();
	}

	public void loadResources() throws ResourceLoadError {
		try (FileSourceBatchClose files = new FileSourceBatchClose(this.files)) {
			palette = new VoxelPalette();
			palette.loadFromPNG(files.get("palette.png"));
			loadBuildingDefs(SettUtil.readXML(files.get("buildings.xml")));
			loadSceneryDefs(SettUtil.readXML(files.get("scenery.xml")));

		} catch (IOException e) {
			throw new ResourceLoadError("Failed to load resources: " + e.getMessage(), e);
		}

	}

	private void loadSceneryDefs(Document doc) throws ResourceLoadError {
		if (!"SceneryDefs".equals(doc.getDocumentElement().getTagName()))
			throw new ResourceLoadError("Expected <SceneryDefs>, not: " + doc.getDocumentElement().getTagName());

		try {
			loadModels(doc);

			Map<String, SceneryType> defs = world.getSceneryDefs();
			for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Scenery")) {
				String id = SettUtil.getAttrib(el, "id");

				if (defs.containsKey(id))
					throw new IllegalArgumentException("Scenery " + id + " already defined as: " + defs.get(id));
				SceneryType bt = createSceneryType(el, id);
				defs.put(bt.getName(), bt);
			}
		} catch (Exception e) {
			throw new ResourceLoadError("Failed to load Scenery Definitions: " + e.getMessage(), e);
		}
	}

	private SceneryType createSceneryType(Element root, String id) {
		SceneryType res = new SceneryType(id);
		res.setModelName(SettUtil.getElement(root, "Model").getTextContent());
		return res;
	}

	private void loadBuildingDefs(Document doc) throws ResourceLoadError {
		if (!"BuildingDefs".equals(doc.getDocumentElement().getTagName()))
			throw new ResourceLoadError("Expected <BuildingsDefs>, not: " + doc.getDocumentElement().getTagName());

		try {
			loadModels(doc);

			Map<String, BuildingType> defs = world.getBuildingDefs();
			for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Building")) {
				String id = SettUtil.getAttrib(el, "id");
				if (defs.containsKey(id))
					throw new IllegalArgumentException("BuildingType " + id + " already defined as: " + defs.get(id));
				BuildingType bt = new BuildingType(id, SettUtil.getAttrib(el, "display", id));
				bt.setModelName(SettUtil.getElement(el, "Model").getTextContent());
				bt.setDescription(SettUtil.getElementText(el, "Description", bt.getDisplay()));
				defs.put(bt.getName(), bt);
			}
		} catch (Exception e) {
			throw new ResourceLoadError("Failed to load Building Definitions: " + e.getMessage(), e);
		}
	}

	private void loadModels(Document doc) throws DOMException, IOException {
		for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Model")) {
			String id = SettUtil.getAttrib(el, "id");
			if (models.containsKey(id))
				throw new IllegalArgumentException("Model " + id + " already defined as: " + models.get(id) + ".  (Remember <Model>s are global across the entire game)");
			int rot = parseRotation(SettUtil.getAttrib(el, "rotate", "0"));
			VoxelModel vm = new VoxelModel(id, palette);
			if (SettUtil.optElement(el, "FileName") != null) {
				vm.fromStream(files.get(SettUtil.getElement(el, "FileName").getTextContent()), rot);
			} else {
				for (Element elframe : SettUtil.getElements(el, "Frame"))
					vm.fromStream(files.get(SettUtil.getElement(elframe, "FileName").getTextContent()), rot, parseFrameTime(SettUtil.getAttrib(elframe, "time")));
			}
			models.put(id, vm);
		}
	}

	public MarkerType getPermissableMarkerFor(int x, int y) {
		TerrainNode[] n = world.getTerrain().getNeighbours(x, y);

		//
		// now work out what can go here
		//
		if (isCastleValid(n))
			return MarkerType.LARGE_HOUSE;
		else if (isHouseValid(n))
			return MarkerType.SMALL_HOUSE;
		else if (isMineValid(n))
			return MarkerType.MINE;
		else if (isFlagValid(n[0]))
			return MarkerType.FLAG;
		return MarkerType.NONE;
	}

	private boolean isCastleValid(TerrainNode[] nodes) {
		if (!isBuildingValid(nodes))
			return false;
		if (nodes[0].getType() != TERRAIN_GRASS)
			return false;

		//
		// now check the gradient of the land
		//
		float f = nodes[0].getNormal().dot(UP);
		return f > 0.99f;
	}

	private boolean isHouseValid(TerrainNode[] nodes) {
		if (!isBuildingValid(nodes))
			return false;
		if (nodes[0].getType() != TERRAIN_GRASS)
			return false;

		//
		// now check the gradient of the land
		//
		float f = nodes[0].getNormal().dot(UP);
		return f > 0.98f;
	}

	private boolean isMineValid(TerrainNode[] nodes) {
		if (!isBuildingValid(nodes))
			return false;
		if (nodes[0].getType() != TERRAIN_MOUNTAIN)
			return false;

		//
		// now check the gradient of the land
		//
		float f = nodes[0].getNormal().dot(UP);
		return f > 0.80f;
	}

	private boolean isBuildingValid(TerrainNode[] nodes) {
		TerrainNode n = nodes[0];
		if (n.getObject() != null && n.getObject().getObstructionType() != ObstructionType.NONE)
			return false;
		
		if (n.hasRoad())
			return false;

		//
		// Can only put a building in if it's valid to put a flag in spot 4
		//
		n = nodes[4];
		if ((n.getObjectAs(Flag.class) != null) || isFlagValid(n))
			return true;
		return false;
	}

	private boolean isFlagValid(TerrainNode n) {
		if (n.getObject() != null && (n.getObject().getObstructionType() == ObstructionType.BUILDINGS || n.getObject().getObstructionType() == ObstructionType.ALL))
			return false;
		if (n.getType() == BaseGame.TERRAIN_ICE || n.getType() == TERRAIN_WATER)
			return false;

		TerrainNode[] nodes = world.getTerrain().getNeighbours(n);
		for (int i = 1; i < 7; ++i)
			if (nodes[i].getObjectAs(Flag.class) != null)
				return false;

		float f = n.getNormal().dot(UP);
		return f > 0.40f;
	}

	public void addRoad(List<Vector2i> nodes) {
		if (nodes == null || nodes.size() < 2)
			throw new IllegalStateException("Invalid path: Needs at least two segments");
		Road r = new Road(nextId());
		r.getPath().addAll(nodes);
		applyRoadToTerrain(r);
		world.getRoads().put(r);
	}

	protected void applyRoadToTerrain(Road r) {

		Terrain terr = world.getTerrain();
		List<Vector2i> nodes = r.getPath();
		for (int i = 0; i < nodes.size() - 1; ++i) {
			//
			// find the edge this is on
			//
			Vector2i n1 = nodes.get(i);
			Vector2i n2 = nodes.get(i + 1);
			int dx = n2.x - n1.x;
			int dy = n2.y - n1.y;

			if (Math.abs(dx) > 1 || Math.abs(dy) > 1)
				throw new IllegalStateException("Invalid path node list, dx,dy == " + dx + "," + dy);

			//
			// work out which direction this is in
			// dx  dy  dir ((4dy+1)+(dx+1))
			// --  --  ---
			// -1  -1  0
			//  0  -1  1
			// -1   0  4
			//  1   0  6
			//  0   1  9
			//  1   1  10
			//  
			//
			int dir = (dy + 1) * 4 + (dx + 1);
			TerrainNode n;
			int x = n1.x;
			int y = n1.y;
			switch (dir) {
			case 6:
				terr.get(x, y).setRoad(0, r);
				break;
			case 9:
				terr.get(x, y).setRoad(2, r);
				break;
			case 10:
				terr.get(x, y).setRoad(1, r);
				break;
			case 4:
				terr.get(x - 1, y).setRoad(0, r);
				break;
			case 1:
				terr.get(x, y - 1).setRoad(2, r);
				break;
			case 0:
				terr.get(x - 1, y - 1).setRoad(1, r);
				break;
			default:
				throw new IllegalStateException("Invalid path shape: " + dir + "   dx,dy == " + dx + ", " + dy);
			}

		}
	}

	public void addPlayer(Player p) {
		players.put(p.getName(), p);
	}

	public Map<String, Player> getPlayers() {
		return players;
	}

	private static float parseFrameTime(String s) {
		try {
			float r = Float.parseFloat(s);
			if (r <= 0.0f)
				throw new IllegalArgumentException("Time must be a float > 0.0");
			return r;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Time must be a float > 0.0");
		}
	}

	private static int parseRotation(String s) {
		try {
			int r = Integer.parseInt(s);
			if (r < 0 || r > 3)
				throw new IllegalArgumentException("Rotation must be in range 0..3");
			return r;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Rotation must be in range 0..3");
		}
	}

	public SettApp getApp() {
		return app;
	}

}
