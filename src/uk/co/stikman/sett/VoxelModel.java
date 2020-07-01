package uk.co.stikman.sett;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.util.ChunkHeader;
import uk.co.stikman.sett.util.VoxelInputStream;
import uk.co.stikman.utils.math.Matrix4;
import uk.co.stikman.utils.math.Vector3;

public class VoxelModel {
	private static final StikLog	LOGGER				= StikLog.getLogger(VoxelModel.class);
	private static final int		VOX_FILE_VERSION	= 150;
	private final String			name;
	private int						sizeX;
	private int						sizeY;
	private int						sizeZ;
	private short[]					voxels;

	public VoxelModel(String name, VoxelPalette pal) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * rotation must be 0, 1, 2, 3 (which is 0, 90, 180, 270 degrees)
	 * 
	 * @param is
	 * @param zRotation
	 * @throws IOException
	 */
	public void fromStream(InputStream is, int zRotation) throws IOException {
		try (VoxelInputStream dis = new VoxelInputStream(new BufferedInputStream(is))) {
			if (!"VOX ".equals(dis.read4()))
				throw new IOException("Invalid VOX file: header invalid");
			int v = dis.readInt();
			if (v != VOX_FILE_VERSION)
				throw new IOException("Invalid VOX file: Can only import Version " + VOX_FILE_VERSION + ".  Attempted to read: " + v);

			ChunkHeader chunk = dis.readChunkHeader();
			if (!"MAIN".equals(chunk.id))
				throw new IOException("Invalid VOX file: MAIN chunk missing");
			chunk.assertSize(0, chunk.childLen);

			int modelcount = 1;
			chunk = dis.readChunkHeader();
			if ("PACK".equals(chunk.id)) {
				chunk.assertSize(4, 0);
				modelcount = dis.readInt();
				chunk = dis.readChunkHeader();
			}

			if (modelcount != 1)
				throw new IOException("Invalid VOX file: Expected a single model");

			if (!"SIZE".equals(chunk.id))
				throw new IOException("Invalid VOX file: Expected SIZE chunk but found " + chunk.id);
			chunk.assertSize(12, 0);
			sizeX = dis.readInt();
			sizeY = dis.readInt();
			sizeZ = dis.readInt();

			if (zRotation == 1 || zRotation == 3) {
				int t = sizeX;
				sizeX = sizeY;
				sizeY = t;
			}

			voxels = new short[sizeX * sizeY * sizeZ];
			for (int i = 0; i < voxels.length; ++i)
				voxels[i] = -1;

			chunk = dis.readChunkHeader();
			if (!"XYZI".equals(chunk.id))
				throw new IOException("Invalid VOX file: Expected XYZI chunk but found " + chunk.id);
			chunk.assertSize(chunk.contentLen, 0);
			int vc = dis.readInt();

			for (int i = 0; i < vc; ++i) {
				int x0 = dis.read();
				int y0 = dis.read();
				int z0 = dis.read();

				int x, y, z = z0;

				switch (zRotation) {
				case 0:
					x = x0;
					y = y0;
					break;
				case 1:
					x = sizeX - y0 - 1;
					y = x0;
					break;
				case 2:
					x = sizeX - x0 - 1;
					y = sizeY - y0 - 1;
					break;
				case 3:
					x = y0;
					y = sizeY - x0 - 1;
					break;
				default:
					throw new IllegalArgumentException("Rotation must be 0..3");
				}

				voxels[z * sizeY * sizeX + y * sizeX + x] = (short) dis.read();
			}

			chunk = dis.optionalChunkHeader();
			if (chunk != null) {
				// read palette
				// TODO...
			}

			LOGGER.info("Read VOX [" + getName() + "] (" + vc + " voxels)");
		}
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public int getSizeZ() {
		return sizeZ;
	}

	public short[] getVoxels() {
		return voxels;
	}

	public int get(int x, int y, int z) {
		if (x < 0 || x >= sizeX)
			return -1;
		if (y < 0 || y >= sizeY)
			return -1;
		if (z < 0 || z >= sizeZ)
			return -1;
		return voxels[z * sizeX * sizeY + y * sizeX + x];
	}

}
