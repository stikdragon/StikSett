package uk.co.stikman.sett.gfx.ui;

public class EasingFloat {
	private float		target;
	private float		current;
	private float		rate		= 1.0f;
	private float		remaining	= 0.0f;
	private EasingMode	mode		= EasingMode.FIXED_TIME;
	private float		fixedRate	= 1.0f;
	private float		fixedTime	= 1.0f;

	public static EasingFloat fixedRate(float rate, float initialValue) {
		EasingFloat f = new EasingFloat();
		f.mode = EasingMode.FIXED_RATE;
		f.current = initialValue;
		f.fixedRate = rate;
		return f;
	}

	public static EasingFloat fixedTime(float time, float initialValue) {
		EasingFloat f = new EasingFloat();
		f.mode = EasingMode.FIXED_TIME;
		f.current = initialValue;
		f.fixedTime = time;
		return f;
	}

	private EasingFloat() {
	}

	public float get() {
		return current;
	}

	public void set(float f) {
		target = f;
		float d = target - current;
		if (d == 0)
			return;
		if (mode == EasingMode.FIXED_RATE) {
			if (d < 0)
				rate = -fixedRate;
			else
				rate = fixedRate;
			remaining = d / rate;
		} else {
			remaining = fixedTime;
			rate = remaining / d;
		}
	}

	public void update(float dt) {
		if (remaining <= 0)
			return;
		remaining -= dt;
		if (remaining <= 0) {
			current = target;
			remaining = 0.0f;
		} else {
			current = target - remaining * rate;
		}
	}

	public EasingMode getMode() {
		return mode;
	}

	public float getTarget() {
		return target;
	}

}
