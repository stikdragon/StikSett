package uk.co.stikman.sett.gfx.ui;

import uk.co.stikman.sett.gfx.util.Rect;

public class Component {
	private Rect				bounds	= new Rect();
	private final SimpleWindow	owner;
	protected float				time;
	private final String		name;

	public Component(SimpleWindow owner, String name) {
		this.owner = owner;
		this.name = name;
		owner.getComponents().add(this);
		time = 0.0f;
	}

	public Rect getBounds() {
		return bounds;
	}

	public void setBounds(Rect bounds) {
		this.bounds.set(bounds);
	}

	public void render() {
	}

	public void update(float dt) {
		time += dt;
	}

	public SimpleWindow getOwner() {
		return owner;
	}

	public WindowTheming theme() {
		return owner.getTheming();
	}

	public void mouseMove(int x, int y) {
	}

	public void mouseEnter(int x, int y) {
	}

	public void mouseExit(int x, int y) {
	}

	public void mouseDown(int x, int y, int button) {
	}

	public void mouseUp(int x, int y, int button) {
	}

	public String getName() {
		return name;
	}
}
