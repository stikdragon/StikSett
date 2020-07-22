package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.SettInputStream;

public class NoddyAim implements IsSerializable {
	private float	time;
	private int		x;
	private int		y;

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void toStream(SettOutputStream str) throws IOException {
		str.writeFloat(time);
		str.writeInt(x);
		str.writeInt(y);
	}

	@Override
	public void fromStream(SettInputStream str) throws IOException {
		time = str.readFloat();
		x = str.readInt();
		y = str.readInt();
	}

}
