package uk.co.stikman.sett.game;

import java.util.Arrays;

import uk.co.stikman.utils.math.Vector3;

public class TerrainNode {
	private final int		x;
	private final int		y;
	private float			height;
	private int				owner;
	private int				type;
	private Road[]			roads	= new Road[3];
	private Vector3			normal	= new Vector3();
	private IsNodeObject	object;

	public TerrainNode(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Road getRoad(int edge) {
		return roads[edge];
	}

	public void setRoad(int edge, Road r) {
		roads[edge] = r;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float h) {
		this.height = h;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public void clearRoads() {
		roads[0] = null;
		roads[1] = null;
		roads[2] = null;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Flag getFlag() {
		if (object == null)
			return null;
		if (object instanceof Flag)
			return (Flag) object;
		return null;
	}

	public void setNormal(Vector3 n) {
		normal.copy(n);
	}

	public Vector3 getNormal() {
		return normal;
	}

	public IsNodeObject getObject() {
		return object;
	}

	public void setObject(IsNodeObject object) {
		this.object = object;
	}

	@Override
	public String toString() {
		return "TerrainNode [x=" + x + ", y=" + y + ", height=" + height + ", owner=" + owner + ", type=" + type + ", roads=" + Arrays.toString(roads) + ", normal=" + normal + ", object=" + object + "]";
	}

}
