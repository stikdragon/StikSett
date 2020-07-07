package uk.co.stikman.sett.game;

import java.io.IOException;

import uk.co.stikman.sett.SettInputStream;

public interface IsSerializable {

	void toStream(SettOutputStream str) throws IOException;

	void fromStream(SettInputStream str) throws IOException;

}
