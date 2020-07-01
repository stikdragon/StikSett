package uk.co.stikman.sett.gfx;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import uk.co.stikman.sett.gfx.lwjgl.Window3DNative;
import uk.co.stikman.sett.gfx.text.BitmapFont;
import uk.co.stikman.sett.gfx.util.ArrIter;
import uk.co.stikman.sett.gfx.util.Coord;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;
import uk.co.stikman.sett.gfx.util.StreamSource;
import uk.co.stikman.sett.gfx.util.StringTable;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector4;

public class GameResources {

	private Map<String, Sprite>		sprites	= new HashMap<>();
	private Map<String, Image>		images	= new HashMap<>();
	private Map<String, BitmapFont>	fonts	= new HashMap<>();
	private Map<String, Cursor>		cursors	= new HashMap<>();
	private final Window3DNative			wnd;
	private StreamSource			loader;
	private final int				layerCount;

	public GameResources(Window3DNative wnd, int layercount) {
		super();
		this.layerCount = layercount;
		this.wnd = wnd;
	}

	public GameResources(Window3DNative wnd) {
		this(wnd, 4);
	}

	public void load(String catalog, StreamSource loader) throws ResourceLoadError {
		if (catalog == null)
			throw new NullPointerException("Catalog must be supplied");
		if (loader == null)
			throw new NullPointerException("Resource loader must be supplied");
		this.loader = loader;

		ArrIter<String> iter = null;
		PreprocResult res = preprocess(catalog);
		try {
			iter = new ArrIter<>(res.toArray(), s -> s.length() == 0 || s.startsWith("#"));
			while (iter.hasNext()) {
				String line = iter.next();

				if (line.startsWith("sprite "))
					processSprite(line, iter);
				else if (line.startsWith("font "))
					processFont(line, iter);
				else if (line.startsWith("image "))
					processImage(line, iter);
				else if (line.startsWith("cursor "))
					processCursor(line, iter);
				else
					throw new ResourceLoadError("Expected sprite, font, or image");

			}

		} catch (Exception e) {
			throw new ResourceLoadError("Line " + (iter == null ? "?" : (res.translate(iter.position()))) + ": Failed to load resource because: " + e.getMessage(), e);
		}

	}

	private void processCursor(String line, ArrIter<String> iter) throws ResourceLoadError {
		String[] bits = line.split(" ");
		if (bits.length != 3)
			throw new ResourceLoadError("Expected 1 argument on \"cursor\" line");
		String name = bits[1];
		Vector2i hotspot = Vector2i.parse(bits[2]);
		loadCursor(name, hotspot);
	}

	private void loadCursor(String name, Vector2i hotspot) throws ResourceLoadError {
		if (fonts.containsKey(name))
			return;
		try (InputStream is = loader.getStream(name)) {
			Cursor cur = wnd.loadCursor(name, is, hotspot);
			cursors.put(name, cur);
		} catch (IOException ex) {
			throw new ResourceLoadError("Failed to load cursor [" + name + "]", ex);
		}
	}

	private void processImage(String line, ArrIter<String> iter) throws ResourceLoadError {
		//
		// image should have 2 args
		//
		String[] bits = line.split(" ");
		if (bits.length != 3)
			throw new ResourceLoadError("image expects 2 arguments");

		if (!"generate".equals(bits[2]))
			throw new ResourceLoadError("Expected generate as image type");

		String name = bits[1];
		if (findImage(name) != null)
			throw new ResourceLoadError("There is already an image called " + name);

		if (startsWith(iter.peek(), "generator")) {
			generateImage(name, iter);
		}
		expect(iter, "end");
		return;

	}

	private void generateImage(String name, ArrIter<String> mainiter) throws ResourceLoadError {
		String[] bits = mainiter.next().split(" ");
		ArrIter<String> iter = new ArrIter<>(bits);
		if (!"generator".equals(iter.next()))
			throw new ResourceLoadError("Expected line starting with [generator]");

		String typ = iter.next();
		if ("shadow".equals(typ))
			generateShadowImage(name, iter);
		else if ("darken".equals(typ))
			generateDarkerImage(name, iter);
		else
			throw new ResourceLoadError("Unknown generator: " + typ);
	}

	private Image generateDarkerImage(String outputname, ArrIter<String> iter) throws ResourceLoadError {
		Map<String, String> args = new HashMap<>();
		while (iter.hasNext()) {
			String arg = iter.next();
			args.put(arg, iter.next());
		}

		if (!args.containsKey("source"))
			throw new ResourceLoadError("Darken generator missing [source] parameter");
		if (!args.containsKey("colour"))
			throw new ResourceLoadError("Darken generator missing [colour] parameter");
		if (!args.containsKey("alpha"))
			throw new ResourceLoadError("Darken generator missing [alpha] parameter");

		String sourceImage = args.get("source");
		Vector4 colour = VectorColours.parseCSS(args.get("colour"));
		colour.w = (int) (Float.parseFloat(args.get("alpha")) * 255.0f);

		//
		// Get image, make shadow
		//
		try (InputStream is = new BufferedInputStream(loader.getStream(sourceImage))) {
			BufferedImage img = ImageIO.read(is);
			img = ShadowMaker.darken(img, colour, colour.w);

			//			ImageIO.write(img, "png", new File("g:\\junk\\darker.png"));

			Image i = new Image(wnd, img, outputname);
			images.put(outputname, i);
			return i;

		} catch (IOException ex) {
			throw new ResourceLoadError("Failed to load [" + sourceImage + "]", ex);
		}

	}

	private Image generateShadowImage(String outputname, ArrIter<String> iter) throws ResourceLoadError {
		Map<String, String> args = new HashMap<>();
		while (iter.hasNext()) {
			String arg = iter.next();
			args.put(arg, iter.next());
		}

		if (!args.containsKey("source"))
			throw new ResourceLoadError("Shadow generator missing [source] parameter");
		if (!args.containsKey("colour"))
			throw new ResourceLoadError("Shadow generator missing [colour] parameter");
		if (!args.containsKey("alpha"))
			throw new ResourceLoadError("Shadow generator missing [alpha] parameter");
		if (!args.containsKey("blur"))
			throw new ResourceLoadError("Shadow generator missing [blur] parameter");

		String sourceImage = args.get("source");
		Vector4 colour = VectorColours.parseCSS(args.get("colour"));
		colour.w = (int) (Float.parseFloat(args.get("alpha")));
		int blur = Integer.parseInt(args.get("blur"));

		//
		// Get image, make shadow
		//
		try (InputStream is = new BufferedInputStream(loader.getStream(sourceImage))) {
			BufferedImage img = ImageIO.read(is);
			img = ShadowMaker.shadow(img, colour, blur);

			Image i = new Image(wnd, img, outputname);
			images.put(outputname, i);
			return i;

		} catch (IOException ex) {
			throw new ResourceLoadError("Failed to load [" + sourceImage + "]", ex);
		}

	}

	private void processFont(String line, ArrIter<String> iter) throws ResourceLoadError {
		String[] bits = line.split(" ");
		if (bits.length != 2)
			throw new ResourceLoadError("Expected 1 argument on \"font\" line");
		String name = bits[1];
		loadFont(name);
	}

	private void processSprite(String line, ArrIter<String> iter) throws ResourceLoadError, IOException {
		String[] bits = line.split(" ");
		if (bits.length < 3)
			throw new ResourceLoadError("Expected at least one argument on \"sprite\" line");
		String name = bits[1];
		String typ = null;
		boolean animated = false;
		boolean noclip = false;

		for (int i = 2; i < bits.length; ++i) {
			String bit = bits[i];
			if ("poly".equals(bit))
				typ = "poly";
			else if ("rect".equals(bit))
				typ = "rect";
			else if ("animated".equals(bit))
				animated = true;
			else if ("noclip".equals(bit))
				noclip = true;
			else
				throw new ResourceLoadError("Unrecognised directive: " + bit);
		}

		if (typ == null)
			throw new ResourceLoadError("Expected one of 'poly' or 'rect' as a sprite type");

		if (findSprite(name) != null)
			throw new ResourceLoadError("Sprite " + name + " already exists");
		Sprite sprite = null;
		if ("poly".equals(typ)) {
			PolySprite x = new PolySprite(this, name);
			String s = iter.next();
			while (startsWith(s, "vertex ")) {
				s = s.substring(7);
				Coord cd = Coord.parse(s);
				x.addVertex(cd);
				s = iter.next();
			}
			iter.rewind();
			sprite = x;

		} else if ("rect".equals(typ)) {
			RectSprite x = new RectSprite(this, name);
			String s = iter.next();
			String[] bits2 = s.split(", *");
			if (bits2.length != 2 && bits2.length != 6)
				throw new IllegalArgumentException("\"rect\" expects 2 or 6 arguments (width,height[,offsetx,offsety,imgwidth,imgheight])");

			if (bits2.length == 2) {
				int ax = Integer.parseInt(bits2[0]);
				int ay = Integer.parseInt(bits2[1]);
				x.setRect(ax, ay, 0, 0, ax, ay);
				//				x.addVertex(new Coord(0, 0, 0, 0));
				//				x.addVertex(new Coord(ax, 0, ax, 0));
				//				x.addVertex(new Coord(ax, ay, ax, ay));
				//				x.addVertex(new Coord(0, ay, 0, ay));
			} else {
				int ax = Integer.parseInt(bits2[0]);
				int ay = Integer.parseInt(bits2[1]);
				float bx = Float.parseFloat(bits2[2]);
				float by = Float.parseFloat(bits2[3]);
				float cw = Float.parseFloat(bits2[4]);
				float ch = Float.parseFloat(bits2[5]);
				x.setRect(ax, ay, bx, by, bx + cw, by + ch);
			}
			sprite = x;
		} else
			throw new IllegalArgumentException(typ);

		sprite.setAnimated(animated);
		sprite.setNoclip(noclip);

		while (!"end".equals(iter.peek())) {
			// expect layer
			String s = iter.next();

			if (startsWith(s, "layer ")) {
				String[] bits2 = s.split(" ");
				Vector2i off = new Vector2i();
				if (bits2.length == 5) {
					if (!"offset".equals(bits2[3]))
						throw new ResourceLoadError("Expected 'offset'");
					off = Vector2i.parse(bits2[4]);
				}
				sprite.setImage(Integer.parseInt(bits2[1]), loadImage(bits2[2]), off);
			} else if (startsWith(s, "frame ")) {
				if (!animated)
					throw new ResourceLoadError("Sprite is not animated, cannot have 'frame' instruction");
				String[] bits2 = s.split(" ");
				if (bits2.length != 3)
					throw new ResourceLoadError("Expected 3 tokens on 'frame'");
				Vector2i off = new Vector2i();
				off = Vector2i.parse(bits2[2]);
				sprite.addFrame(Integer.parseInt(bits2[1]), off);
			} else if (startsWith(s, "tags ")) {
				sprite.addTags(s.substring(5).split(", *"));
			} else if (!startsWith(s, "layer ")) {
				throw new ResourceLoadError("Expected 'layer'");
			}

		}

		sprites.put(sprite.getName(), sprite);
		expect(iter, "end");
	}

	/**
	 * Loads or returns existing one
	 * 
	 * @param name
	 * @return
	 * @throws ResourceLoadError
	 */
	public Image loadImage(String name) throws ResourceLoadError {
		if (images.containsKey(name))
			return images.get(name);
		try (InputStream is = loader.getStream(name)) {
			Image i = new Image(wnd, is, name);
			images.put(name, i);
			return i;
		} catch (IOException ex) {
			throw new ResourceLoadError("Failed to load [" + name + "]", ex);
		}
	}

	public Image findImage(String name) {
		return images.get(name);
	}

	private void loadFont(String name) throws ResourceLoadError {
		if (fonts.containsKey(name))
			return;
		try (InputStream is = loader.getStream(name)) {
			BitmapFont f = wnd.loadFontZIP(is);
			fonts.put(f.getName(), f);
		} catch (IOException ex) {
			throw new ResourceLoadError("Failed to load [" + name + "]", ex);
		}
	}

	public Sprite findSprite(String name) {
		return sprites.get(name);
	}

	public BitmapFont findFont(String name) {
		return fonts.get(name);
	}

	public Sprite getSprite(String name) {
		Sprite x = findSprite(name);
		if (x == null)
			throw new NoSuchElementException(name);
		return x;
	}

	@Override
	public String toString() {
		StringTable st = new StringTable("Type", "Name", "Info");
		for (Sprite spr : sprites.values())
			st.add("Sprite", spr.getName(), spr.getImage(0).getName());
		for (BitmapFont font : fonts.values())
			st.add("Font", font.getName());
		for (Image img : images.values())
			st.add("Image", img.getName(), img.getWidth() + " x " + img.getHeight());
		return st.toString();
	}

	private static boolean startsWith(String string, String find) {
		if (string == null)
			return false;
		if (find == null)
			throw new NullPointerException();
		return string.startsWith(find);
	}

	private static void expect(ArrIter<String> iter, String s) throws ResourceLoadError {
		if (s == null)
			throw new NullPointerException();
		if (!iter.hasNext())
			throw new ResourceLoadError("Expected [" + s + "]");
		if (!s.equals(iter.next()))
			throw new ResourceLoadError("Expected [" + s + "]");
	}

	/**
	 * This is how many layers the {@link GameResources} allows you to use in
	 * sprites. It has to be fixed since lots of internal arrays get allocated
	 * to this size. Defaults to 4, which seems like plenty. The more layers,
	 * the slower the rendering loop will become since it has to call render on
	 * everything for each layer
	 * 
	 * @return
	 */
	public int getLayerCount() {
		return layerCount;
	}

	/**
	 * Outputs a list of Strings that have had all the @ lines removed and
	 * substituted
	 * 
	 * @param input
	 * @return
	 * @throws ResourceLoadError
	 */
	private static PreprocResult preprocess(String input) throws ResourceLoadError {
		List<String> output = new ArrayList<>();
		List<Integer> linemap = new ArrayList<>();

		Map<String, Block> blocks = new HashMap<>();

		ArrIter<String> iter = new ArrIter<>(input.split("\\r?\\n"));
		Block block = null;
		while (iter.hasNext()) {
			String s = iter.next();
			if (s.startsWith("@")) {
				if (s.startsWith("@defineblock ")) {
					block = new Block(s.substring(13));
					while (iter.hasNext()) {
						s = iter.next();
						if (s.equals("@end"))
							break;
						block.getItems().add(s);
					}
					blocks.put(block.getName(), block);
				} else if (s.startsWith("@block ")) {
					s = s.substring(7);
					block = blocks.get(s);
					if (block == null)
						throw new ResourceLoadError("Block [" + s + "] not defined");
					for (String t : block.getItems()) {
						output.add(t);
						linemap.add(iter.position());
					}
				} else
					throw new ResourceLoadError("Expected @defineblock <id>, or @block <id>");
			} else {
				output.add(s);
				linemap.add(iter.position());
			}
		}

		return new PreprocResult(output, linemap);
	}

	public List<Sprite> getSprites() {
		List<Sprite> lst = new ArrayList<>();
		sprites.values().forEach(lst::add);
		return lst;
	}

	public Window3DNative getWindow() {
		return wnd;
	}

}
