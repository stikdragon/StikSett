package uk.co.stikman.sett.client.renderer;

/**
 * <p>
 * This is a sphere that will be placed around the model at a given radius, any
 * rays hitting it are counted as being able to see the sky. The coverage value
 * is how far down the sphere goes, imagine a dome placed over the scene. 0 is
 * none, 0.5 is a hemisphere and 1.0 is an entire sphere
 * </p>
 * <p>
 * It's important that the sphere is bigger than any model
 * </p>
 * 
 * @author stikd
 *
 */
public class AOSkySphere {
	private float	radius;
	private float	coverage;

	public AOSkySphere(float radius, float coverage) {
		super();
		this.radius = radius;
		this.coverage = coverage;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getCoverage() {
		return coverage;
	}

	public void setCoverage(float coverage) {
		this.coverage = coverage;
	}

	@Override
	public String toString() {
		return "AOSkySphere [radius=" + radius + ", coverage=" + coverage + "]";
	}

}
