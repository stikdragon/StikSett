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

	public String read4() throws IOException {
		return new String(str.readNBytes(4), StandardCharsets.ISO_8859_1);
	}

	public String readString() throws IOException {
		return str.readString();
	}

	public byte[] readBuf() throws IOException {
		int n = str.readInt();
		if (n == -1)
			return null;
		if (n == 0)
			return new byte[0];
		return str.readBytes(n);
	}

}
