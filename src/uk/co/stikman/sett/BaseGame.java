package uk.co.stikman.sett;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.co.stikman.sett.game.BuildingType;
import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.ObstructionType;
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
	public static final int			TERRAIN_GRASS		= 0;
	public static final int			TERRAIN_DESERT		= 1;
	public static final int			TERRAIN_MOUNTAIN	= 2;
	public static final int			TERRAIN_ICE			= 3;
	public static final int			TERRAIN_WATER		= 4;

	private static final Vector3	UP					= new Vector3(0, 0, 1);

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

	protected FileSource				files;
	protected Map<String, VoxelModel>	models		= new HashMap<>();
	protected VoxelPalette				palette;
	protected World						world;
	private AtomicInteger				sequence	= new AtomicInteger(0);

	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
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
		// if there's anything else that blocks all at our neighbours then 
		// you can't build here at all
		//
		for (int i = 1; i < n.length; ++i)
			if (n[i].getObject() != null && n[i].getObject().getObstructionType() == ObstructionType.ALL)
				return MarkerType.NONE;

		IsNodeObject no = n[0].getObject();
		if (no != null) {
			if (no.getObstructionType() == ObstructionType.ALL || no.getObstructionType() == ObstructionType.BUILDINGS)
				return MarkerType.NONE;
		}

		//
		// find angle of groud at centre
		//
		float f = n[0].getNormal().dot(UP);
		System.out.println("dotprod = " + f);
		if (n[0].getType() == BaseGame.TERRAIN_GRASS) {
			if (f > 0.99f)
				return MarkerType.LARGE_HOUSE;
			if (f >= 0.98f)
				return MarkerType.SMALL_HOUSE;
			return MarkerType.FLAG;
		} else if (n[0].getType() == BaseGame.TERRAIN_DESERT) {
			return MarkerType.FLAG;
		} else if (n[0].getType() == BaseGame.TERRAIN_MOUNTAIN) {
			if (f >= 0.8f)
				return MarkerType.MINE;
			return MarkerType.NONE;
		}

		return MarkerType.NONE;
	}

	public void addRoad(List<Vector2i> nodes) {
		if (nodes == null || nodes.size() < 2)
			throw new IllegalStateException("Invalid path: Needs at least two segments");
		Road r = new Road(nextId());
		r.getPath().addAll(nodes);
		Terrain terr = world.getTerrain();

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
		world.getRoads().put(r);

	}

}
