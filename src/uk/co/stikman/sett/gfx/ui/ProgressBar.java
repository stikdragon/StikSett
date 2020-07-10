package uk.co.stikman.sett.gfx.ui;

import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.utils.math.Vector4;

public class ProgressBar extends Component {

	private static final float	SMOOTH_SPEED		= 4.0f;

	private Vector4				backgroundColour	= new Vector4(.6f, .6f, .6f, 0.1f);
	private Vector4				colour				= new Vector4(.6f, .6f, .6f, 1);
	private int					min					= 0;
	private int					max					= 100;
	private EasingFloat			value				= EasingFloat.fixedRate(200.0f, 0);
	private Rect				tmpR				= new Rect();

	public ProgressBar(SimpleWindow owner) {
		super(owner, null);
		rangeChange();
	}

	@Override
	public void render() {
		tmpR.copy(getBounds());
		getOwner().getWindow().drawFlatRect(tmpR, backgroundColour);
		if (max - min <= 0)
			return;

		float f = (value.get() - min) / (max - min);
		tmpR.w = (int) (getBounds().w * f);
		getOwner().getWindow().drawFlatRect(tmpR, colour);
	}

	/**
	 * if <code>null</code> then uses the theme colour
	 * 
	 * @param col
	 */
	public void setColour(Vector4 col) {
		this.colour = col;
	}

	public Vector4 getColour() {
		return colour;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
		rangeChange();
	}

	private void rangeChange() {
		if (max - min <= 0)
			return;
		int v = (int) value.getTarget();
		if (v > max)
			v = max;
		if (v < min)
			v = min;
		value = EasingFloat.fixedRate((max - min) * SMOOTH_SPEED, v);
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		rangeChange();
	}

	public int getValue() {
		return (int) value.getTarget();
	}

	public void setValue(int value) {
		if (value > max)
			value = max;
		if (value < min)
			value = min;
		this.value.set(value);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		value.update(dt);
	}

}
