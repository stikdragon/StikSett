package uk.co.stikman.sett;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import uk.co.stikman.log.StikLog;

public class ClientGame extends BaseGame {
	private static final StikLog	LOGGER	= StikLog.getLogger(ClientGame.class);
	private SettApp					app;
	public ClientGame(SettApp sett) {
		this.app = sett;
		files = Resources::getFileWild;
	}

	public FileSource getFileSource() {
		return files;
	}

	public String getTextFile(String name) throws IOException {
		try (InputStream is = files.get(name)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		}
	}

	public Map<String, VoxelModel> getModels() {
		return models;
	}

	public VoxelPalette getVoxelPalette() {
		return palette;
	}

}
