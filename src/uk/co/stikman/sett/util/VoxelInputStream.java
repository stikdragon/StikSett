package uk.co.stikman.sett.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import uk.co.stikman.utils.StikDataInputStream;

public class VoxelInputStream extends StikDataInputStream {

	private final byte[] buf = new byte[4];

	public VoxelInputStream(InputStream in) {
		super(in);
	}

	public String read4() throws IOException {
		readBytes(4, buf);
		return new String(buf, StandardCharsets.ISO_8859_1);
	}

	@Override
	public int readInt() throws IOException {
		int ch4 = in.read();
		int ch3 = in.read();
		int ch2 = in.read();
		int ch1 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	public ChunkHeader readChunkHeader() throws IOException {
		ChunkHeader h = new ChunkHeader();
		h.id = read4();
		h.contentLen = readInt();
		h.childLen = readInt();
		return h;
	}

	public void skipContent(ChunkHeader ch) throws IOException {
		skip(ch.childLen);
		skip(ch.contentLen);
	}

	/**
	 * returns <code>null</code> if end of stream reached
	 * 
	 * @return
	 * @throws IOException
	 */
	public ChunkHeader optionalChunkHeader() throws IOException {
		int n = read(buf, 0, 4);
		if (n == -1)
			return null;
		ChunkHeader h = new ChunkHeader();
		h.id = new String(buf, StandardCharsets.ISO_8859_1);
		h.contentLen = readInt();
		h.childLen = readInt();
		return h;
	}

}
