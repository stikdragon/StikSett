package uk.co.stikman.sett;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.ObstructionType;
import uk.co.stikman.sett.game.Road;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.utils.MutableInteger;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class BaseGame {
	public static final int			TERRAIN_GRASS		= 0;
	public static final int			TERRAIN_DESERT		= 1;
	public static final int			TERRAIN_MOUNTAIN	= 2;
	public static final int			TERRAIN_ICE			= 3;
	public static final int			TERRAIN_WATER		= 4;

	private static final Vector3	UP					= new Vector3(0, 0, 1);
	protected World					world;
	private AtomicInteger			sequence			= new AtomicInteger(0);

	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public int nextId() {
		return sequence.incrementAndGet();
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
