package uk.co.stikman.sett.gfx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Makes it easier to work with zip files. Is very inefficient since it just
 * immediately unpacks them into memory. Use it with archives that have a lot of
 * small files in them. For big stuff you should use the normal
 * {@link ZipInputStream} stuff yourself
 * 
 * @author stik
 *
 */
public class ZipUtil {
	private Map<String, byte[]> entries = new HashMap<>();

	public ZipUtil(InputStream source) throws IOException {
		byte[] buf = new byte[1024];
		ZipInputStream zis = new ZipInputStream(source);
		ZipEntry e = zis.getNextEntry();
		while (e != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int n;
			while ((n = zis.read(buf)) > 0)
				baos.write(buf, 0, n);
			baos.close();
			entries.put(e.getName(), baos.toByteArray());
			e = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	public List<String> find(String regex) {
		List<String> res = new ArrayList<>();
		for (String s : entries.keySet()) {
			if (s.matches(regex))
				res.add(s);
		}
		return res;
	}

	public InputStream getStream(String name) {
		byte[] b = entries.get(name);
		if (b == null)
			throw new NoSuchElementException(name);
		return new ByteArrayInputStream(b);
	}

}
