package uk.co.stikman.sett.game;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector3;

public class Noddy implements GameObject {

	private final Game				game;
	private int						id;
	private Player					owner;
	private NoddyType				type;
	private Vector3					position	= new Vector3();
	private LinkedList<NoddyAim>	targets		= new LinkedList<>();

	public Noddy(Game game) {
		this.game = game;
	}

	public Noddy(Game game, int id) {
		this.game = game;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

	public void toStream(SettOutputStream out) throws IOException {
		out.writeInt(id);
		out.writeString(type.getName());
		out.writeVec3(position);
		out.writeObject(owner);
		out.writeList(targets);
	}

	public void fromStream(SettInputStream str) throws IOException {
		id = str.readInt();
		type = getGame().getNoddyDef(str.readString());
		str.readVec3(position);
		owner = str.readObject(Player.class);
		targets = (LinkedList<NoddyAim>) str.readList(NoddyAim.class, new LinkedList<>());
	}

	public NoddyType getType() {
		return type;
	}

	public void setType(NoddyType type) {
		this.type = type;
	}

	public Game getGame() {
		return game;
	}

	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position.copy(position);
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public void setPosition(int x, int y, int z) {
		position.set(x, y, z);
	}

	public Vector2 interpolatePosition(float t, Vector2 out) {
		//
		// work along the list of targets we've got, find where 
		// we are.  can shortcut if there's nothing
		//
		if (targets.isEmpty())
			return out.set(position.x, position.y);
		NoddyAim a = targets.getLast();
		if (t >= a.getTime())
			return out.set(a.getX(), a.getY());

		Iterator<NoddyAim> iter = targets.iterator();
		NoddyAim last = iter.next();
		while (iter.hasNext()) {
			NoddyAim next = iter.next();
			if (t >= last.getTime() && t < next.getTime()) {
				float mu = (t - last.getTime()) / (next.getTime() - last.getTime());
				return out.set(last.getX() + mu * (next.getX() - last.getX()), last.getY() + mu * (next.getY() - last.getY()));
			}
			last = next;
		}

		//
		// i don't think it should be possible to get here
		//
		a = targets.getLast();
		return out.set(a.getX(), a.getY());
	}

	public LinkedList<NoddyAim> getTargets() {
		return targets;
	}

	public void setTargets(LinkedList<NoddyAim> targets) {
		this.targets = targets;
	}

}
