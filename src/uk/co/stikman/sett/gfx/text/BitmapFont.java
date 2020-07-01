package uk.co.stikman.sett.gfx.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.co.stikman.sett.gfx.Texture;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;


public class BitmapFont {

	private class Props {
		private Map<String, String> map = new HashMap<>();

		public String get(String key) {
			String s = map.get(key);
			if (s == null)
				throw new RuntimeException("Missing property: " + key);
			return s;
		}

		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}
	}

	public class FontPage {
		private Texture	image;
		private int		id;

		/**
		 * @param image
		 * @param id
		 */
		public FontPage(Texture image, int id) {
			super();
			this.image = image;
			this.id = id;
		}

		public Texture getImage() {
			return image;
		}

		public int getId() {
			return id;
		}

	}

	public class BMChar {
		private int	index;
		private int	id;
		public int	x;
		public int	y;
		public int	width;
		public int	height;
		public int	offsetx;
		public int	offsety;
		public int	xadvance;
		public int	page;
		public int	chnl;

		public BMChar(int index, int id) {
			this.index = index;
			this.id = id;
		}

		public int getIndex() {
			return index;
		}

		public int getId() {
			return id;
		}
	}

	private String								name;
	private int									size;
	private FontPage[]							pages;
	private Map<Integer, BMChar>				chars		= new HashMap<>();
	private int									lineHeight;
	private int									base;
	private int									mapHeight;
	private int									mapWidth;
	private Map<Integer, Map<Integer, Integer>>	kernings	= new HashMap<>();
	private TextureLoader						resources;

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public FontPage[] getPages() {
		return pages;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public int getBase() {
		return base;
	}

	/**
	 * @param name
	 * @param resources
	 * @param size
	 * @param image
	 */
	public BitmapFont(String name, TextureLoader loader) {
		super();
		this.name = name;
		this.resources = loader;
	}

	public void loadFrom(List<String> lines, String prependTextures) throws ResourceLoadError {

		if (lines.size() < 4)
			throw new RuntimeException("BitmapFont source not valid");
		if (!lines.get(0).startsWith("info face"))
			throw new RuntimeException("BitmapFont source not valid");

		Props props = parsePropsLine(lines.get(0));
		this.size = props.getInt("size");

		props = parsePropsLine(lines.get(1));
		this.lineHeight = props.getInt("lineHeight");
		this.base = props.getInt("base");
		this.pages = new FontPage[props.getInt("pages")];
		this.mapHeight = props.getInt("scaleH");
		this.mapWidth = props.getInt("scaleW");

		Iterator<String> iter = lines.iterator();
		iter.next();
		iter.next();
		while (iter.hasNext()) {
			String line = iter.next();
			if (line.startsWith("page ")) {
				//
				// Page definition
				// 
				Props pairs = parsePropsLine(line);
				int id = pairs.getInt("id");

				pages[id] = new FontPage(resources.loadTexture(prependTextures + pairs.get("file").replace("\"", "")), id);

			} else if (line.startsWith("char ")) {
				Props pairs = parsePropsLine(line);
				int id = pairs.getInt("id");
				BMChar bmc = new BMChar(chars.size(), id);
				bmc.chnl = pairs.getInt("chnl");
				bmc.page = pairs.getInt("page");
				bmc.xadvance = pairs.getInt("xadvance");
				bmc.offsety = pairs.getInt("yoffset");
				bmc.offsetx = pairs.getInt("xoffset");
				bmc.height = pairs.getInt("height");
				bmc.width = pairs.getInt("width");
				bmc.y = pairs.getInt("y");
				bmc.x = pairs.getInt("x");
				chars.put(id, bmc);
			} else if (line.startsWith("kerning ")) {
				Props pairs = parsePropsLine(line);
				Map<Integer, Integer> map = kernings.get(pairs.getInt("second"));
				if (map == null) {
					map = new HashMap<>();
					kernings.put(pairs.getInt("second"), map);
				}
				map.put(pairs.getInt("first"), pairs.getInt("amount"));
			}
		}
	}

	private Props parsePropsLine(String line) {
		Props res = new Props();
		String[] bits = line.split("\\s+");
		for (String p : bits) {
			if (p.indexOf("=") == -1) {
				res.map.put(p, null);
			} else {
				String[] two = p.split("=");
				res.map.put(two[0], two[1]);
			}
		}
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitmapFont other = (BitmapFont) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	public BMChar get(char ch) {
		return chars.get((int) ch);
	}

	public int getMapWidth() {
		return mapWidth;
	}

	public int getMapHeight() {
		return mapHeight;
	}

	public boolean isLoaded() {
		return !chars.isEmpty();
	}

	public int getKerning(BMChar first, BMChar second) {
		if (first == null || second == null)
			return 0;
		Map<Integer, Integer> map = kernings.get(second.id);
		if (map == null)
			return 0;
		Integer i = map.get(first.id);
		if (i != null)
			return i;
		return 0;
	}

}