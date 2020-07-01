package uk.co.stikman.sett;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.log.StikLog;

public class FileSourceBatchClose implements FileSource, Closeable {
	private static final StikLog	LOGGER	= StikLog.getLogger(FileSourceBatchClose.class);
	private List<InputStream>		streams	= new ArrayList<>();

	private final FileSource		src;

	public FileSourceBatchClose(FileSource src) {
		this.src = src;
	}

	@Override
	public InputStream get(String name) throws IOException {
		InputStream s = src.get(name);
		streams.add(s);
		return s;
	}

	public void closeAll() {
		for (InputStream is : streams)
			try {
				is.close();
			} catch (IOException e) {
				LOGGER.error(e);
			}
	}

	@Override
	public void close() throws IOException {
		closeAll();
	}
}
