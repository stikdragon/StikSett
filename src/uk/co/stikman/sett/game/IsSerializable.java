package uk.co.stikman.sett.game;

import java.io.IOException;

public interface IsSerializable {

	void toStream(SettOutputStream str) throws IOException;

}
