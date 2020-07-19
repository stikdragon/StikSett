package uk.co.stikman.sett.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.util.ThingList;

public class World {
	private WorldParameters				params;
	private Terrain						terrain;
	private final ThingList<Building>	buildings	= new ThingList<>("Building");
	private final ThingList<Flag>		flags		= new ThingList<>("Flag");
	private final ThingList<Road>		roads		= new ThingList<>("Road");
	private final ThingList<Noddy>		noddies		= new ThingList<>("Noddy");

	private int							width;
	private int							height;
	private Game						game;

	public World(Game game) {
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

	public ThingList<Noddy> getNoddies() {
		return noddies;
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

	public Game getGame() {
		return game;
	}

	public void generate(GenerateOptions opts) {
		terrain.generate(opts);
	}

}
