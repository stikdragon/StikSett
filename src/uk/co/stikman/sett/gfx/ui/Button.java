package uk.co.stikman.sett.gfx.ui;

import java.util.function.Consumer;

import uk.co.stikman.sett.gfx.Sprite;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.sett.gfx.text.HAlign;
import uk.co.stikman.sett.gfx.text.OutlineMode;
import uk.co.stikman.sett.gfx.text.RenderTextOptions;
import uk.co.stikman.sett.gfx.text.VAlign;
import uk.co.stikman.sett.gfx.util.Rect;
import uk.co.stikman.utils.math.Vector4;


public class Button extends Component {

	private String				caption;
	private RenderTextOptions	rto				= new RenderTextOptions();
	private RenderTextOptions	rtoShadow		= new RenderTextOptions();
	private boolean				hover;
	private boolean				pressed;
	private boolean				down;
	private boolean				toggle;
	private Rect				tmpR			= new Rect();
	private EasingFloat			textHighlight	= EasingFloat.fixedRate(12.0f, 0.0f);
	private EasingFloat			imageMerge		= EasingFloat.fixedRate(16.0f, 0.0f);
	private Vector4				tmpC			= new Vector4();
	private Vector4				buttonColour	= new Vector4(VectorColours.WHITE);
	private Sprite				sprite;
	private Consumer<Button>	onClick;
	private boolean				flat			= false;
	private Object				userData;

	public Button(SimpleWindow owner, String name, String caption) {
		super(owner, name);
		this.caption = caption;
		this.sprite = null;
		rto.setAlignV(VAlign.CENTRE);
		rto.setAlignH(HAlign.CENTRE);

		rtoShadow.copy(rto);
		rtoShadow.setOutlineMode(OutlineMode.SHADOW);
	}

	public Button(SimpleWindow owner, String name, Sprite sprite) {
		this(owner, name, sprite, false);
	}

	public Button(SimpleWindow owner, String name, Sprite sprite, boolean toggle) {
		super(owner, name);
		this.caption = null;
		this.sprite = sprite;
		this.toggle = toggle;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	@Override
	public void render() {
		Rect r = getBounds();
		Vector4.lerp(theme().getFontColour(), VectorColours.WHITE, textHighlight.get(), tmpC);
		float f = imageMerge.get();
		if (f > 0.0f) {
			if (!flat)
				getOwner().getWindow().drawSmartQuad(theme().getButtonSprite(), getBounds(), buttonColour, theme().getButtonSpriteSM(), 0);
			tmpC.copy(buttonColour);
			tmpC.w = f;
			getOwner().getWindow().drawSmartQuad(flat ? theme().getFlatButtonSpriteDown() : theme().getButtonSpriteDown(), getBounds(), tmpC, theme().getButtonSpriteSM(), 0);
		} else {
			if (!flat)
				getOwner().getWindow().drawSmartQuad(theme().getButtonSprite(), getBounds(), buttonColour, theme().getButtonSpriteSM(), 0);
		}

		if (caption != null)
			getOwner().getWindow().drawText((int) r.x, (int) r.y, (int) r.w, (int) r.h, getCaption(), theme().getFont(), rtoShadow, tmpC);
		if (sprite != null) {
			tmpC.copy(VectorColours.WHITE);
			if (hover) {
				tmpC.x *= 0.85f;
				tmpC.y *= 0.85f;
				tmpC.z *= 0.85f;
			}
			tmpR.set(sprite.getBounds());
			tmpR.centreIn(r);
			sprite.renderLayer(getOwner().getWindow(), 0, (int) tmpR.x, (int) tmpR.y, -1, -1, 0, time, tmpC);
		}

	}

	@Override
	public void mouseEnter(int x, int y) {
		hover = true;
		textHighlight.set(1.0f);
	}

	@Override
	public void mouseExit(int x, int y) {
		hover = false;
		pressed = false;
		textHighlight.set(0.0f);
	}

	@Override
	public void mouseDown(int x, int y, int button) {
		if (!toggle) {
			pressed = true;
			imageMerge.set(1.0f);
		}
	}

	@Override
	public void mouseUp(int x, int y, int button) {
		if (toggle) {
			setDown(!down);
		} else {
			pressed = false;
			imageMerge.set(0.0f);
		}
		if (onClick != null)
			onClick.accept(this);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		textHighlight.update(dt);
		imageMerge.update(dt);
	}

	public Vector4 getButtonColour() {
		return buttonColour;
	}

	public void setButtonColour(Vector4 buttonColour) {
		this.buttonColour.copy(buttonColour);
	}

	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		if (toggle) {
			this.down = down;
			imageMerge.set(down ? 1.0f : 0.0f);
		}
	}

	public boolean isToggle() {
		return toggle;
	}

	public void setToggle(boolean toggle) {
		this.toggle = toggle;
	}

	public Consumer<Button> getOnClick() {
		return onClick;
	}

	public void setOnClick(Consumer<Button> onClick) {
		this.onClick = onClick;
	}

	public boolean isFlat() {
		return flat;
	}

	public void setFlat(boolean flat) {
		this.flat = flat;
	}

	public void setUserData(Object obj) {
		this.userData = obj;
	}

	public Object getUserData() {
		return userData;
	}

}
