package uk.co.stikman.sett.gfx.util;

import java.io.IOException;
import java.io.InputStream;

public interface StreamSource {

	/**
	 * Expected to return a valid {@link InputStream}. The thing that calls this
	 * is expected to close the stream
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	InputStream getStream(String name) throws IOException;

}
