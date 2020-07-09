package uk.co.stikman.sett.gfx.ui;

public class EasingBool {
	private EasingFloat	floatVal;
	private boolean		val;

	public EasingBool(float rate, boolean initial) {
		floatVal = EasingFloat.fixedRate(rate, initial ? 1 : 0);
		val = initial;
	}

	public void update(float dt) {
		floatVal.update(dt);
	}

	public float getLerpFactor() {
		return floatVal.get();
	}

	public boolean get() {
		return val;
	}

	public void set(boolean b) {
		if (this.val == b)
			return;
		this.val = b;
		floatVal.set(b ? 1 : 0);
	}
}
