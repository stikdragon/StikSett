package uk.co.stikman.sett;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import uk.co.stikman.sett.gameevents.BuildingOrderedEvent;
import uk.co.stikman.sett.gameevents.GameEvent;

public class GameEvents {

	public static List<GameEvent> read(byte[] data) throws IOException {
		try (SettInputStream str = new SettInputStream(data)) {
			List<GameEvent> lst = new ArrayList<>();
			int c = str.readInt();
			while (c-- > 0) {
				String cls = str.readString();
				GameEvent ev = createGameEvent(cls);
				ev.fromStream(str);
				lst.add(ev);
			}
			return lst;
		}
	}

	private static GameEvent createGameEvent(String cls) {
		// TODO: reflection, maybe?
		switch (cls) {
		case "BuildingOrderedEvent":
			return new BuildingOrderedEvent();
		}
		throw new NoSuchElementException("GameEvent [" + cls + "] not found");
	}

}
