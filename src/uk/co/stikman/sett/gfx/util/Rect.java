package uk.co.stikman.sett.gfx.util;

import uk.co.stikman.utils.math.Vector2;

public class Rect {

	public float	x;
	public float	y;
	public float	w;
	public float	h;

	public Rect() {
	}

	public Rect(float x, float y, float w, float h) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Rect(Rect copy) {
		this.x = copy.x;
		this.y = copy.y;
		this.w = copy.w;
		this.h = copy.h;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	public float getH() {
		return h;
	}

	public void setH(float h) {
		this.h = h;
	}

	@Override
	public String toString() {
		return "Rect [x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + "]";
	}

	public Rect set(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		return this;
	}

	public void set(Rect copy, float scaleX, float scaleY) {
		this.x = (copy.x * scaleX);
		this.y = (copy.y * scaleY);
		this.w = (copy.w * scaleX);
		this.h = (copy.h * scaleY);
	}

	public Rect set(Rect copy) {
		this.x = copy.x;
		this.y = copy.y;
		this.w = copy.w;
		this.h = copy.h;
		return this;
	}

	public Rect centreIn(Rect container) {
		float dx = container.w - w;
		float dy = container.h - h;
		x = container.x + dx / 2;
		y = container.y + dy / 2;
		return this;
	}

	public Rect centreIn(int w, int h) {
		float dx = w - this.w;
		float dy = h - this.h;
		this.x = x + dx / 2;
		this.y = y + dy / 2;
		return this;
	}

	/**
	 * Round everything to integer (with a simple cast)
	 */
	public void quantize() {
		x = (int) x;
		y = (int) y;
		w = (int) w;
		h = (int) h;
	}

	public boolean contains(int x, int y) {
		return (x >= this.x && x <= this.x + this.w) && (y >= this.y && y <= this.y + this.h);
	}

	public void set(Vector2 size) {
		x = 0;
		y = 0;
		w = size.x;
		h = size.y;
	}

	public Rect offset(float dx, float dy) {
		x += dx;
		y += dy;
		return this;
	}

	public Rect expand(int d) {
		x -= d;
		y -= d;
		w += 2 * d;
		h += 2 * d;
		return this;
	}

	public void scale(float f) {
		w *= f;
		h *= f;
	}

	public void copy(Rect r) {
		this.x = r.x;
		this.y = r.y;
		this.w = r.w;
		this.h = r.h;
	}

}
