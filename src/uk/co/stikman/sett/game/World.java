package uk.co.stikman.sett.game;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.client.renderer.Ray;
import uk.co.stikman.sett.util.ThingList;

public class World {
	private WorldParameters					params;
	private Terrain							terrain;
	private final Map<String, BuildingType>	buildingDefs	= new HashMap<>();
	private final Map<String, SceneryType>	sceneryDefs		= new HashMap<>();
	private final ThingList<Building>		buildings		= new ThingList<>("Building");
	private final ThingList<Flag>			flags			= new ThingList<>("Flag");
	private final ThingList<Road>			roads			= new ThingList<>("Road");

	private int								width;
	private int								height;

	public World() {
	}

	public void generate(WorldParameters params) {
		terrain = new Terrain(this, params.getSize() * SettApp.CHUNK_SIZE, params.getSize() * SettApp.CHUNK_SIZE);
		terrain.generate(new GenerateOptions());
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

	public SceneryType getScenaryDef(String name) {
		SceneryType x = sceneryDefs.get(name);
		if (x == null)
			throw new NoSuchElementException("SceneryDef [" + name + "] not found");
		return x;

	}


}
