package uk.co.stikman.sett;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import uk.co.stikman.utils.math.Vector4;

public class VoxelPalette {
	private Vector4[] entries;

	public VoxelPalette() {
		super();
		entries = new Vector4[257];
		for (int i = 0; i < entries.length; ++i)
			entries[i] = new Vector4(0, 0, 0, 1);
	}

	public Vector4 get(int idx) {
		return entries[idx];
	}

	public void loadFromPNG(InputStream is) throws IOException {
		BufferedImage img = ImageIO.read(is);
		if (img.getWidth() * img.getHeight() != 256)
			throw new IOException("Palette should have 256 pixels");

		entries[0].set(0, 0, 0, 1);
		int ptr = 0;
		for (int y = 0; y < img.getHeight(); ++y) {
			for (int x = 0; x < img.getWidth(); ++x) {
				Color c = new Color(img.getRGB(x, y));
				
				entries[++ptr].set(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
			}
		}
	}

}
