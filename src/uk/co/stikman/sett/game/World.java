package uk.co.stikman.sett.game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import uk.co.stikman.sett.BaseGame;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.util.ThingList;

public class World {
	private WorldParameters								params;
	private Terrain										terrain;
	private transient final Map<String, BuildingType>	buildingDefs	= new HashMap<>();
	private transient final Map<String, SceneryType>	sceneryDefs		= new HashMap<>();
	private final ThingList<Building>					buildings		= new ThingList<>("Building");
	private final ThingList<Flag>						flags			= new ThingList<>("Flag");
	private final ThingList<Road>						roads			= new ThingList<>("Road");

	private int											width;
	private int											height;
	private BaseGame									game;

	public World(BaseGame game) {
		this.game = game;
	}

	public void setParams(WorldParameters params) {
		terrain = new Terrain(this, params.getSize() * SettApp.CHUNK_SIZE, params.getSize() * SettApp.CHUNK_SIZE);
		this.width = params.getSize() * SettApp.CHUNK_SIZE;
		this.height = params.getSize() * SettApp.CHUNK_SIZE;
		this.params = params;
	}
	
	public ThingList<Building> getBuildings() {
		return buildings;
	}

	public ThingList<Flag> getFlags() {
		return flags;
	}

	public ThingList<Road> getRoads() {
		return roads;
	}

	public Map<String, BuildingType> getBuildingDefs() {
		return buildingDefs;
	}

	public Map<String, SceneryType> getSceneryDefs() {
		return sceneryDefs;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public WorldParameters getParams() {
		return params;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public IsNodeObject getObjectAt(int x, int y) {
		return terrain.get(x, y).getObject();

	}

	/**
	 * Throws {@link NoSuchElementException} if missing
	 * 
	 * @param name
	 * @return
	 */
	public SceneryType getScenaryDef(String name) {
		SceneryType x = sceneryDefs.get(name);
		if (x == null)
			throw new NoSuchElementException("SceneryType [" + name + "] not found");
		return x;
	}

	/**
	 * Throws {@link NoSuchElementException} if missing
	 * 
	 * @param name
	 * @return
	 */
	public BuildingType getBuildingDef(String name) {
		BuildingType x = buildingDefs.get(name);
		if (x == null)
			throw new NoSuchElementException("BuildingType [" + name + "] not found");
		return x;
	}

	public List<Road> getRoadsAt(TerrainNode node, List<Road> out) {
		Road r = node.getRoad(0);
		if (r != null)
			out.add(r);
		r = node.getRoad(1);
		if (r != null)
			out.add(r);
		r = node.getRoad(2);
		if (r != null)
			out.add(r);
		r = terrain.get(node.getX(), node.getY() - 1).getRoad(2);
		if (r != null)
			out.add(r);
		r = terrain.get(node.getX() - 1, node.getY() - 1).getRoad(1);
		if (r != null)
			out.add(r);
		r = terrain.get(node.getX() - 1, node.getY()).getRoad(0);
		if (r != null)
			out.add(r);
		return out;
	}

	public BaseGame getGame() {
		return game;
	}

	public void generate(GenerateOptions opts) {
		terrain.generate(opts);
	}

}
