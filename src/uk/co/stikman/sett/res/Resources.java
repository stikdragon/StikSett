package uk.co.stikman.sett.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Resources {

	public static InputStream getFile(String name) throws IOException {
		File f = new File("res\\" + name);
		if (!f.exists())
			throw new FileNotFoundException(name);
		InputStream x = new FileInputStream(f);
		return x;
	}

	/**
	 * allows wildcards, returns the first thing it finds that matches
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static InputStream getFileWild(String name) throws IOException {
		if (!name.contains("*"))
			return getFile(name);
			
		String reg = name.replaceAll("\\*", ".*");
		File root = new File("res");
		for (File f : root.listFiles()) 
			if (f.getName().matches(reg)) 
				return new FileInputStream(f);
		throw new FileNotFoundException(name);
	}

}
