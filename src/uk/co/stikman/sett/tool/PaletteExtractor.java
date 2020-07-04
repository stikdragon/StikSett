package uk.co.stikman.sett.tool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * takes all the PNG files in a directory and works out the 256 most commonly
 * used colours in them, then tries to sort them into a pleasing order
 * 
 * @author stik
 *
 */
public class PaletteExtractor {
	// where your PNGs are
	public static final String	ROOT			= "G:\\junk\\settimgs\\";
	// this is a big one that you can look at as a human, 16x16 squares
	private static final String	OUTPUT_READABLE	= "G:\\junk\\pal1.png";
	// this is the actual 256x1 palette that things like magicavoxel work with
	private static final String	OUTPUT_PAL		= "G:\\junk\\pal1_line.png";

	private static class ColEnt {
		Color	colour;
		int		counter	= 1;
		float[]	hsb;

		public ColEnt(Color c) {
			this.colour = c;
		}
	}

	public static void main(String[] args) throws IOException {
		Map<Color, ColEnt> map = new HashMap<>();

		for (File f : new File(ROOT).listFiles()) {
			if (f.getName().endsWith(".png")) {
				BufferedImage img = ImageIO.read(f);
				for (int y = 0; y < img.getHeight(); ++y) {
					for (int x = 0; x < img.getWidth(); ++x) {
						Color c = new Color(img.getRGB(x, y));
						ColEnt e = map.get(c);
						if (e == null)
							map.put(c, e = new ColEnt(c));
						e.counter++;
					}
				}
			}
		}

		List<ColEnt> lst = new ArrayList<>(map.values());
		lst.sort((a, b) -> b.counter - a.counter);
		//		lst.forEach(x -> System.out.println(x.colour + "  -->  " + x.counter));

		System.out.println("Size === " + lst.size());
		while (lst.size() > 256)
			lst.remove(lst.size() - 1);

		for (ColEnt e : lst) {
			float[] out = new float[3];
			Color.RGBtoHSB(e.colour.getRed(), e.colour.getGreen(), e.colour.getBlue(), out);
			e.hsb = out;
			System.out.println(Arrays.toString(e.hsb) + "  -->  " + e.counter);
		}

		//
		// here's various sorting parameters for trying to 
		// make a nice looking palette
		//
		//@formatter:off
		int[][] params = new int[][] {
			new int[] {0, 1, 2, 256, 256, 256},	 // 0 pretty good
			new int[] {1, 0, 2, 256, 256, 256},  // 1 no
			new int[] {2, 0, 1, 256, 256, 256},  // 2 no
			new int[] {0, 2, 1, 256, 256, 256},  // 3 pretty good
			new int[] {0, 2, 1, 4, 256, 256},    // 4 no 
			new int[] {0, 1, 2, 8, 64, 256},     // 5 okish 
			new int[] {0, 1, 2, 8, 32, 256},     // 6 okish 
			new int[] {0, 1, 2, 8, 8, 256},      // 7 okish 
			new int[] {0, 2, 1, 256, 8, 8},	     // 8 moderate 
			new int[] {0, 2, 1, 12, 16, 256},    // 9 	      
		};
		//@formatter:on

		int group = params.length - 1;

		group = 3; // change this

		int sk1 = params[group][0];
		int sk2 = params[group][1];
		int sk3 = params[group][2];

		float divs1 = params[group][3];
		float divs2 = params[group][4];
		float divs3 = params[group][5];

		lst.sort((a, b) -> {
			int a1 = (int) (a.hsb[sk1] * divs1);
			int b1 = (int) (b.hsb[sk1] * divs1);
			int a2 = (int) (a.hsb[sk2] * divs2);
			int b2 = (int) (b.hsb[sk2] * divs2);
			int a3 = (int) (a.hsb[sk3] * divs3);
			int b3 = (int) (b.hsb[sk3] * divs3);

			// force all the greys to one end
			int aGray = a.hsb[1] <= 0.002f ? 1 : 0;
			int bGray = b.hsb[1] <= 0.002f ? 1 : 0;
			int n;

			n = bGray - aGray;
			if (n != 0)
				return n;

			n = b1 - a1;
			if (n != 0)
				return n;
			n = b2 - a2;
			if (n != 0)
				return n;
			return b3 - a3;
		});

		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		for (int i = 0; i < lst.size(); ++i) {
			int x = i % 16;
			int y = i / 16;
			g.setColor(lst.get(i).colour);
			g.fillRect(x * 16, y * 16, 16, 16);
		}
		ImageIO.write(img, "PNG", new File(OUTPUT_READABLE));

		img = new BufferedImage(256, 1, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < lst.size(); ++i) {
			Color c = lst.get(i).colour;
			img.setRGB(i, 0, c.getRGB());
		}
		ImageIO.write(img, "PNG", new File(OUTPUT_PAL));
	}

}
