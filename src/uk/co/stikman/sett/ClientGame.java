package uk.co.stikman.sett;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.game.BuildingType;
import uk.co.stikman.sett.game.SceneryType;
import uk.co.stikman.sett.game.VoxelModelParams;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.res.Resources;
import uk.co.stikman.sett.util.SettUtil;

public class ClientGame extends BaseGame {
	private static final StikLog				LOGGER	= StikLog.getLogger(ClientGame.class);
	private SettApp								app;
	private FileSource							files;
	private Map<VoxelModelParams, VoxelModel>	models	= new HashMap<>();
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

			Set<VoxelModelParams> models = new HashSet<>();
			world.getBuildingDefs().values().stream().map(BuildingType::getVoxelModelInfo).forEach(models::add);
			world.getSceneryDefs().values().stream().map(SceneryType::getVoxelModelInfo).forEach(models::add);

			loadModels(models);
		} catch (IOException e) {
			throw new ResourceLoadError("Failed to load resources: " + e.getMessage(), e);
		}

	}

	private void loadModels(Set<VoxelModelParams> models2) throws IOException {
		for (VoxelModelParams vmp : models2) {
			String name = vmp.getName();
			VoxelModel v = new VoxelModel(name, palette);
			int rot = 0;
			if (vmp.getRotation() == 'W')
				rot = 3;
			else if (vmp.getRotation() == 'E')
				rot = 1;
			else if (vmp.getRotation() == 'S')
				rot = 2;
			v.fromStream(files.get(name), rot);

			models.put(vmp, v);
		}
	}

	private void loadSceneryDefs(Document doc) throws ResourceLoadError {
		if (!"SceneryDefs".equals(doc.getDocumentElement().getTagName()))
			throw new ResourceLoadError("Expected <SceneryDefs>, not: " + doc.getDocumentElement().getTagName());

		Map<String, SceneryType> defs = world.getSceneryDefs();
		for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Scenery")) {
			String id = SettUtil.getAttrib(el, "id");

			if (Boolean.parseBoolean(SettUtil.getAttrib(el, "createRotations", "false"))) {
				for (char ch : "NWSE".toCharArray()) {
					String s = id.replace('?', ch);
					if (defs.containsKey(s))
						throw new IllegalArgumentException("Scenery " + s + " already defined as: " + defs.get(s));
					SceneryType bt = createSceneryType(el, s, ch);
					defs.put(bt.getName(), bt);
				}
			} else {
				if (defs.containsKey(id))
					throw new IllegalArgumentException("Scenery " + id + " already defined as: " + defs.get(id));
				SceneryType bt = createSceneryType(el, id, ' ');
				defs.put(bt.getName(), bt);
			}
		}
	}

	private SceneryType createSceneryType(Element root, String id, char rotation) {
		SceneryType res = new SceneryType(id);
		res.setModelName(SettUtil.getElement(root, "Model").getTextContent());
		res.setRotation(rotation);
		return res;
	}

	private void loadBuildingDefs(Document doc) throws ResourceLoadError {
		if (!"BuildingDefs".equals(doc.getDocumentElement().getTagName()))
			throw new ResourceLoadError("Expected <BuildingsDefs>, not: " + doc.getDocumentElement().getTagName());

		Map<String, BuildingType> defs = world.getBuildingDefs();
		for (Element el : SettUtil.getElements(doc.getDocumentElement(), "Building")) {
			String id = SettUtil.getAttrib(el, "id");
			if (defs.containsKey(id))
				throw new IllegalArgumentException("BuildingType " + id + " already defined as: " + defs.get(id));
			BuildingType bt = new BuildingType(id, SettUtil.getAttrib(el, "display", id));
			VoxelModelParams vmp = new VoxelModelParams();
			vmp.setName(SettUtil.getElement(el, "Model").getTextContent());
			bt.setVoxelModelInfo(vmp);
			bt.setDescription(SettUtil.getElementText(el, "Description", bt.getDisplay()));
			defs.put(bt.getName(), bt);
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

	public Map<VoxelModelParams, VoxelModel> getModels() {
		return models;
	}

	public VoxelPalette getVoxelPalette() {
		return palette;
	}

}
