package uk.co.stikman.sett;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.game.BuildingType;
import uk.co.stikman.sett.game.SceneryType;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.util.SettUtil;

public class ClientGame extends BaseGame {
	private static final StikLog				LOGGER	= StikLog.getLogger(ClientGame.class);
	private SettApp								app;
	private FileSource							files;
	private Map<String, VoxelModel>	models	= new HashMap<>();
	private VoxelPalette						palette;

	public ClientGame(SettApp sett) {
		this.app = sett;
		files = Resources::getFileWild;
	}

	public void loadResources() throws ResourceLoadError {
		try (FileSourceBatchClose files = new FileSourceBatchClose(this.files)) {
			palette = new VoxelPalette();
			palette.loadFromPNG(files.get("palette.png"));
			loadBuildingDefs(SettUtil.readXML(files.get("buildings.xml")));
			loadSceneryDefs(SettUtil.readXML(files.get("scenery.xml")));

		} catch (IOException e) {
			throw new ResourceLoadError("Failed to load resources: " + e.getMessage(), e);
		}

	}

	private void loadSceneryDefs(Document doc) throws ResourceLoadError {
		if (!"SceneryDefs".equals(doc.getDocumentElement().getTagName()))
			throw new ResourceLoadError("Expected <SceneryDefs>, not: " + doc.getDocumentElement().getTagName());

		try {
			loadModels(doc);

			Map<String, SceneryType> defs = world.getSceneryDefs();
			for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Scenery")) {
				String id = SettUtil.getAttrib(el, "id");

				if (defs.containsKey(id))
					throw new IllegalArgumentException("Scenery " + id + " already defined as: " + defs.get(id));
				SceneryType bt = createSceneryType(el, id);
				defs.put(bt.getName(), bt);
			}
		} catch (Exception e) {
			throw new ResourceLoadError("Failed to load Scenery Definitions: " + e.getMessage(), e);
		}
	}

	private SceneryType createSceneryType(Element root, String id) {
		SceneryType res = new SceneryType(id);
		res.setModelName(SettUtil.getElement(root, "Model").getTextContent());
		return res;
	}

	private void loadBuildingDefs(Document doc) throws ResourceLoadError {
		if (!"BuildingDefs".equals(doc.getDocumentElement().getTagName()))
			throw new ResourceLoadError("Expected <BuildingsDefs>, not: " + doc.getDocumentElement().getTagName());

		try {
			loadModels(doc);

			Map<String, BuildingType> defs = world.getBuildingDefs();
			for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Building")) {
				String id = SettUtil.getAttrib(el, "id");
				if (defs.containsKey(id))
					throw new IllegalArgumentException("BuildingType " + id + " already defined as: " + defs.get(id));
				BuildingType bt = new BuildingType(id, SettUtil.getAttrib(el, "display", id));
				bt.setModelName(SettUtil.getElement(el, "Model").getTextContent());
				bt.setDescription(SettUtil.getElementText(el, "Description", bt.getDisplay()));
				defs.put(bt.getName(), bt);
			}
		} catch (Exception e) {
			throw new ResourceLoadError("Failed to load Building Definitions: " + e.getMessage(), e);
		}
	}

	private void loadModels(Document doc) throws DOMException, IOException {
		for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Model")) {
			String id = SettUtil.getAttrib(el, "id");
			if (models.containsKey(id))
				throw new IllegalArgumentException("Model " + id + " already defined as: " + models.get(id) + ".  (Remember <Model>s are global across the entire game)");
			int rot = parseRotation(SettUtil.getAttrib(el, "rotate", "0"));
			VoxelModel vm = new VoxelModel(id, palette);
			if (SettUtil.optElement(el, "FileName") != null) {
				vm.fromStream(files.get(SettUtil.getElement(el, "FileName").getTextContent()), rot);
			} else {
				for (Element elframe : SettUtil.getElements(el, "Frame"))
					vm.fromStream(files.get(SettUtil.getElement(elframe, "FileName").getTextContent()), rot, parseFrameTime(SettUtil.getAttrib(elframe, "time")));
			}
			models.put(id, vm);
		}
	}

	private static float parseFrameTime(String s) {
		try {
			float r = Float.parseFloat(s);
			if (r <= 0.0f)
				throw new IllegalArgumentException("Time must be a float > 0.0");
			return r;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Time must be a float > 0.0");
		}
	}

	private static int parseRotation(String s) {
		try {
			int r = Integer.parseInt(s);
			if (r < 0 || r > 3)
				throw new IllegalArgumentException("Rotation must be in range 0..3");
			return r;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Rotation must be in range 0..3");
		}
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
