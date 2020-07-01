package uk.co.stikman.sett.gfx;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import uk.co.stikman.utils.math.Vector4;

public class ShadowMaker {
	public static BufferedImage shadow(BufferedImage src, Vector4 colour, float blur) {
		int width = src.getWidth();
		int height = src.getHeight();

		ColorModel cm = src.getColorModel();
		BufferedImage dst = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), null);

		// Make a black mask from the image's alpha channel 
		float[][] extractAlpha = { { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 0, 0, 0, colour.w } };
		BufferedImage shadow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		new BandCombineOp(extractAlpha, null).filter(src.getRaster(), shadow.getRaster());
		if (blur < 1)
			return shadow;
		shadow = createBlurFilter((int) blur).filter(shadow, null);

		Graphics2D g = dst.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, colour.w));
		g.drawRenderedImage(shadow, AffineTransform.getTranslateInstance(0, 0));
		g.dispose();

		return dst;
	}

	public static BufferedImage darken(BufferedImage src, Vector4 colour, float alpha) {
		float f = 1.0f - alpha;
		float r = colour.x * alpha;
		float g = colour.y * alpha;
		float b = colour.z * alpha;

		//@formatter:off
	    float[][] mat = {
	               {f, 0, 0, r},
	               {0, f, 0, g},
	               {0, 0, f, b},
	               {0, 0, 0, 1}};				
	    //@formatter:on

		BandCombineOp op = new BandCombineOp(mat, null);
		Raster source = src.getRaster();
		WritableRaster destination = op.filter(source, null);
		BufferedImage dest = new BufferedImage(src.getColorModel(), destination, false, null);
		return dest;
	}

	public static ConvolveOp createBlurFilter(int r) {
		if (r < 1)
			throw new IllegalArgumentException("Radius must be >= 1");

		int sz = r * 2 + 1;
		float[] m = new float[sz * sz];
		float t = 0.0f;
		float mx = r * r * 2; // max it can be
		int idx = 0;
		for (int y = -r; y <= r; ++y) {
			for (int x = -r; x <= r; ++x) {
				float d = (float) x * x + y * y;
				if (d == 0)
					m[idx] = 1;
				else
					m[idx] = mx / d;
				t += m[idx];
				++idx;
			}
		}

		for (int i = 0; i < m.length; i++)
			m[i] /= t;

		Kernel kernel = new Kernel(sz, sz, m);
		return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}

}
