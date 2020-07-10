package uk.co.stikman.sett.svr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import uk.co.stikman.utils.StikDataOutputStream;
import uk.co.stikman.utils.math.Vector3;

public class SendMessage {

	private ByteArrayOutputStream	bais;
	private StikDataOutputStream	str;

	public SendMessage() {
		bais = new ByteArrayOutputStream();
		str = new StikDataOutputStream(bais);
	}

	public byte[] getBytes() {
		return bais.toByteArray();
	}

	public void writeInt(int n) throws IOException {
		str.writeInt(n);
	}

	public void write4(String s) throws IOException {
		if (s == null || s.length() != 4)
			throw new IllegalArgumentException("String must be 4 chars");
		byte[] buf = s.getBytes(StandardCharsets.ISO_8859_1);
		str.write(buf);
	}

	public void writeString(String s) throws IOException {
		str.writeString(s);
	}

	public void writeVec3(Vector3 v) throws IOException {
		str.writeFloat(v.x);
		str.writeFloat(v.y);
		str.writeFloat(v.z);
	}

	public void writeBuf(byte[] bytes) throws IOException {
		if (bytes == null) {
			str.writeInt(-1);
			return;
		}
		if (bytes.length == 0) {
			str.writeInt(0);
			return;
		}

		str.write(bytes);
	}

}
