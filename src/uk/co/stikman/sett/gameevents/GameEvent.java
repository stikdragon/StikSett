package uk.co.stikman.sett.gameevents;

import java.io.IOException;

import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettInputStream;
import uk.co.stikman.sett.game.SettOutputStream;

public abstract class GameEvent {

	private float time;

	public GameEvent() {
		time = System.currentTimeMillis() / 1000.0f;
	}

	public void toStream(SettOutputStream str) throws IOException {
		str.writeFloat(time);
	}

	public void fromStream(SettInputStream str) throws IOException {
		time = str.readFloat();
	}

	public float getTime() {
		return time;
	}

	public abstract void applyTo(Game game);
}
