package uk.co.stikman.sett.game;

public class GenerateOptions {
	private int		seed			= 1;
	private float	scale			= 0.1f;
	private float	baseRippleSize	= 1.5f;
	private float	mountainAmount	= 0.44f;
	private int		mountainSize	= 3;
	private float	waterAmount		= 0.84f;
	private float	desertAmount	= 0.50f;
	private float	snowLevel		= (5.0f / 9.0f);

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getBaseRippleSize() {
		return baseRippleSize;
	}

	public void setBaseRippleSize(float baseRippleSize) {
		this.baseRippleSize = baseRippleSize;
	}

	public float getMountainAmount() {
		return mountainAmount;
	}

	public void setMountainAmount(float mountainAmount) {
		this.mountainAmount = mountainAmount;
	}

	public int getMountainSize() {
		return mountainSize;
	}

	public void setMountainSize(int mountainSize) {
		this.mountainSize = mountainSize;
	}

	public float getSnowLevel() {
		return snowLevel;
	}

	public void setSnowLevel(float snowLevel) {
		this.snowLevel = snowLevel;
	}

	public void setDesertAmount(float desertAmount) {
		this.desertAmount = desertAmount;
	}

	public float getDesertAmount() {
		return desertAmount;
	}

	public float getWaterAmount() {
		return waterAmount;
	}

	public void setWaterAmount(float waterAmount) {
		this.waterAmount = waterAmount;
	}
}
