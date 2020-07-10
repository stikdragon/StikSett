package uk.co.stikman.sett.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import uk.co.stikman.utils.StikDataInputStream;
import uk.co.stikman.utils.StikDataOutputStream;

public class WorldParameters {
	private int size;

	public WorldParameters() {
		super();
	}

	public WorldParameters(int size) {
		setSize(size);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if (size < 1 || size > 96)
			throw new IllegalArgumentException("World Size must be between 1 and 96");
		this.size = size;
	}

	public void toStream(StikDataOutputStream str) throws IOException {
		str.writeInt(size);
	}

	public void fromStream(StikDataInputStream str) throws IOException {
		size = str.readInt();
	}

	public byte[] toBytes() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		StikDataOutputStream s2 = new StikDataOutputStream(bs);
		try {
			toStream(s2);
			s2.flush();
			return bs.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void fromBytes(byte[] buf) {
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		StikDataInputStream s = new StikDataInputStream(bais);
		try {
			fromStream(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
