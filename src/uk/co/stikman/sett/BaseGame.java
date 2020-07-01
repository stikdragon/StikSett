package uk.co.stikman.sett;

import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.ObstructionType;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.utils.math.Vector3;

public class BaseGame {
	public static final int			TERRAIN_GRASS		= 0;
	public static final int			TERRAIN_DESERT		= 1;
	public static final int			TERRAIN_MOUNTAIN	= 2;
	public static final int			TERRAIN_ICE			= 3;
	public static final int			TERRAIN_WATER		= 4;

	private static final Vector3	UP					= new Vector3(0, 0, 1);
	protected World					world;

	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public MarkerType getPermissableMarkerFor(int x, int y) {
		TerrainNode[] n = world.getTerrain().getNeighbours(x, y);

		//
		// if there's anything else at our neighbours then 
		// you can't build here at all
		//
		for (int i = 1; i < n.length; ++i)
			if (n[i].getObject() != null)
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
		if (n[0].getType() == BaseGame.TERRAIN_GRASS) {
			if (f > 0.9f)
				return MarkerType.LARGE_HOUSE;
			if (f >= 0.7f)
				return MarkerType.SMALL_HOUSE;
			return MarkerType.FLAG;
		} else if (n[0].getType() == BaseGame.TERRAIN_DESERT) {
			return MarkerType.FLAG;
		} else if (n[0].getType() == BaseGame.TERRAIN_MOUNTAIN) {
			if (f >= 0.4f)
				return MarkerType.MINE;
			return MarkerType.NONE;
		}

		return MarkerType.NONE;

	}

}
