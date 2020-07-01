package uk.co.stikman.sett.util;

import java.io.IOException;

public class ChunkHeader {
	public String	id;
	public int		contentLen;
	public int		childLen;

	public void assertSize(int content, int child) throws IOException {
		if (content != contentLen)
			throw new IOException("Chunk " + id + " should have contentlength=" + content);
		if (child != childLen)
			throw new IOException("Chunk " + id + " should have childlength=" + child);
	}
}
