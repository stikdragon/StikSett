package uk.co.stikman.sett.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import uk.co.stikman.utils.math.Vector2i;

/**
 * this isn't thread safe
 * 
 * @author stik
 *
 */
public class ScanlineConverter {

	public interface SetPixel {
		void set(int x, int y);
	}

	private int[]	edges	= new int[0];
	private int		hite;
	private int		offy;

	public void convert(SetPixel setpix, Vector2i a, Vector2i b, Vector2i c) {
		offy = Math.min(Math.min(a.y, b.y), c.y);
		int maxy = Math.max(Math.max(a.y, b.y), c.y);
		hite = maxy - offy;

		if (edges.length / 2 < hite)
			edges = new int[2 * hite];

		for (int i = 0; i < edges.length; ++i) {
			edges[i++] = Integer.MAX_VALUE;
			edges[i] = Integer.MIN_VALUE;
		}

		edge(a.x, a.y - offy, b.x, b.y - offy);
		edge(b.x, b.y - offy, c.x, c.y - offy);
		edge(c.x, c.y - offy, a.x, a.y - offy);

		for (int y = 0; y < hite; ++y) {
			if (edges[y * 2] < edges[y * 2 + 1]) {
				int x = edges[y * 2];
				int len = edges[y * 2 + 1] - x + 1;
				while (len-- > 0)
					setpix.set(x++, y + offy);
			}
		}
	}

	/**
	 * Converts a <b>convex</b> poly with 4 verts
	 * 
	 * @param setpix
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 */
	public void convert(SetPixel setpix, Vector2i a, Vector2i b, Vector2i c, Vector2i d) {
		convert(setpix, a, b, c);
		convert(setpix, a, c, d);
	}

	private void edge(int x0, int y0, int x1, int y1) {
		int dx = x1 - x0;
		int dy = y1 - y0;

		int dx0;
		if (dx > 0)
			dx0 = 1;
		else if (dx < 0)
			dx0 = -1;
		else
			dx0 = 0;

		int dy0;
		if (dy > 0)
			dy0 = 1;
		else if (dy < 0)
			dy0 = -1;
		else
			dy0 = 0;

		int m = dx < 0 ? -dx : dx;
		int n = dy < 0 ? -dy : dy;
		int dx1 = dx0;
		int dy1 = 0;

		if (m < n) {
			m = dy < 0 ? -dy : dy;
			n = dx < 0 ? -dx : dx;
			dx1 = 0;
			dy1 = dy0;
		}

		int x = x0;
		int y = y0;
		int i = m + 1;
		int k = n / 2;

		while (i-- > 0) {
			if (y >= 0 && y < hite) {
				if (x > edges[y * 2 + 1])
					edges[y * 2 + 1] = x;
				if (x < edges[y * 2])
					edges[y * 2] = x;
			}

			k += n;
			if (k >= m) {
				k -= m;
				x += dx0;
				y += dy0;
			} else {
				x += dx1;
				y += dy1;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		ScanlineConverter conv = new ScanlineConverter();

		BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		Vector2i v0 = new Vector2i(60, 40);
		Vector2i v1 = new Vector2i(170, 20);
		Vector2i v2 = new Vector2i(190, 90);
		Vector2i v3 = new Vector2i(65, 105);
		conv.convert((x, y) -> img.setRGB(x, y, 0xff0000), v0, v1, v2, v3);
		ImageIO.write(img, "PNG", new File("G:\\junk\\tri.png"));
	}

}
