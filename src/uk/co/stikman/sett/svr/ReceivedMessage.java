package uk.co.stikman.sett.svr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import uk.co.stikman.utils.StikDataInputStream;

public class ReceivedMessage {

	private StikDataInputStream	str;
	private byte[]				data;

	public ReceivedMessage(byte[] msg) {
		this.data = msg;
		str = new StikDataInputStream(new ByteArrayInputStream(msg));
	}

	public String getInst() throws IOException {
		return new String(str.readNBytes(4), StandardCharsets.ISO_8859_1);
	}

}
