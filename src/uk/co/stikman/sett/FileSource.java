package uk.co.stikman.sett;

import java.io.IOException;
import java.io.InputStream;

public interface FileSource {
	/**
	 * accepts wildcards as '*' eg. <code>model1-*-top.vox</code>
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	InputStream get(String name) throws IOException;
}
