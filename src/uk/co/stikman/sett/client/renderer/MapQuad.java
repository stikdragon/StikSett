package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class MapQuad {
	private static final StikLog	LOGGER		= StikLog.getLogger(MapQuad.class);
	public int[]					tris;

	/**
	 * This will be:
	 * 
	 * <pre>
	 * nX, x
	 * nY, y
	 * X, -skewed x,y
	 * Y,
	 * Z, - height offset
	 * w0 - barycentric weights for the 4 nodes
	 * w1
	 * w2
	 * w3
	 * f  - 1.0f if it's a road, 0.0f otherwise
	 * 
	 * </pre>
	 */
	public static float[]			verts;

	public static MapQuad[]			quads		= new MapQuad[512];
	public static MapQuad[]			roads		= new MapQuad[512];

	private static final int		P0			= 0;
	private static final int		P1			= 1;
	private static final int		P2			= 2;
	private static final int		P3			= 3;
	private static final int		E0			= 4;
	private static final int		E1			= 5;
	private static final int		E2			= 6;
	private static final int		E3			= 7;
	private static final int		E4			= 8;


	static {
		LOGGER.info("Initialising MapQuad..");
		try {
			Vector3[] verts = new Vector3[40];
			Vector4[] bary = new Vector4[40];
			int[][] tris = new int[20][];

			float pw = SettApp.PATH_WIDTH / 2.0f;

			verts[0] = new Vector3(0, 0, 0);
			verts[1] = new Vector3(pw, 0, 0);
			verts[2] = new Vector3(1.0f - pw, 0, 0);
			verts[3] = new Vector3(1, 0, 0);

			verts[4] = new Vector3(0, pw, 0);
			verts[5] = new Vector3(pw, pw, 0);
			verts[6] = new Vector3(pw * 2, pw, 0);
			verts[7] = new Vector3(1.0f - pw, pw, 0);
			verts[8] = new Vector3(1, pw, 0);

			verts[9] = new Vector3(0, 1.0f - pw, 0);
			verts[10] = new Vector3(pw, 1.0f - pw, 0);
			verts[11] = new Vector3(1.0f - 2 * pw, 1.0f - pw, 0);
			verts[12] = new Vector3(1.0f - pw, 1.0f - pw, 0);
			verts[13] = new Vector3(1, 1.0f - pw, 0);

			verts[14] = new Vector3(0, 1, 0);
			verts[15] = new Vector3(pw, 1, 0);
			verts[16] = new Vector3(1.0f - pw, 1, 0);
			verts[17] = new Vector3(1, 1, 0);

			verts[18] = new Vector3(pw, pw * 2, 0);
			verts[19] = new Vector3(1.0f - pw, 1.0f - pw * 2, 0);

			for (int i = 0; i < 20; ++i)
				verts[i + 20] = new Vector3(verts[i]).sub(new Vector3(0, 0, SettApp.ROAD_DEPTH));

			tris[0] = new int[] { 6, 7, 19 };
			tris[1] = new int[] { 18, 11, 10 };
			tris[2] = new int[] { 1, 2, 7, 6 };
			tris[3] = new int[] { 7, 8, 13, 19 };
			tris[4] = new int[] { 6, 19, 12, 5 };
			tris[5] = new int[] { 10, 11, 16, 15 };
			tris[6] = new int[] { 4, 18, 10, 9 };
			tris[7] = new int[] { 5, 12, 11, 18 };
			tris[8] = new int[] { 4, 5, 18 };
			tris[9] = new int[] { 0, 5, 4 };
			tris[10] = new int[] { 0, 1, 5 };
			tris[11] = new int[] { 1, 6, 5 };
			tris[12] = new int[] { 2, 8, 7 };
			tris[13] = new int[] { 2, 3, 8 };
			tris[14] = new int[] { 19, 13, 12 };
			tris[15] = new int[] { 12, 13, 17 };
			tris[16] = new int[] { 12, 17, 16 };
			tris[17] = new int[] { 11, 12, 16 };
			tris[18] = new int[] { 9, 10, 15 };
			tris[19] = new int[] { 9, 15, 14 };

			MapQuad.verts = new float[verts.length * 10];

			//
			// set up coords and skewed coords
			//
			Matrix3 m = SettApp.skewMatrix(new Matrix3());
			Vector3 v2 = new Vector3();
			int ptr = 0;
			for (Vector3 v : verts) {
				MapQuad.verts[ptr++] = v.x;
				MapQuad.verts[ptr++] = v.y;
				v2 = m.multiply(v, v2);
				MapQuad.verts[ptr++] = v2.x;
				MapQuad.verts[ptr++] = v2.y;
				MapQuad.verts[ptr++] = v2.z;
				ptr += 5;
			}

			//
			// barycentric weights for interpolation height 
			//
			char[] leftOrRight = "LRRRLLRRRLLLLRLLLLLRLRRRLLRRRLLLLRLLLLLR".toCharArray();
			for (int i = 0; i < leftOrRight.length; ++i) {
				if (leftOrRight[i] == 'L') {
					Vector3 a = verts[0];
					Vector3 b = verts[17];
					Vector3 c = verts[14];
					Vector3 out = new Vector3();
					barycentric(verts[i], a, b, c, out);
					MapQuad.verts[i * 10 + 5] = out.x;
					MapQuad.verts[i * 10 + 6] = 0.0f;
					MapQuad.verts[i * 10 + 7] = out.y;
					MapQuad.verts[i * 10 + 8] = out.z;
				} else {
					Vector3 a = verts[0];
					Vector3 b = verts[3];
					Vector3 c = verts[17];
					Vector3 out = new Vector3();
					barycentric(verts[i], a, b, c, out);
					bary[i] = new Vector4(out.x, out.y, out.z, 0.0f);
					MapQuad.verts[i * 10 + 5] = out.x;
					MapQuad.verts[i * 10 + 6] = out.y;
					MapQuad.verts[i * 10 + 7] = out.z;
					MapQuad.verts[i * 10 + 8] = 0.0f;
				}
				
				MapQuad.verts[i * 10 + 9] = i >= 20 ? 1 : 0; // road or normal?
			}

			//
			// generate the 512 combinations that can exist
			//
			for (int i = 0; i < 512; ++i) {
				boolean[] sunk = new boolean[20];

				//@formatter:off
				sunk[0] = false;
				sunk[1] = false;
				sunk[2] = bit(i, E1);
				sunk[3] = bit(i, E2);
				sunk[4] = bit(i, E0);
				sunk[5] = bit(i, E3);
				sunk[6] = bit(i, E4);
				sunk[7] = bit(i, E0);
				sunk[8] = bit(i, E0, E4);
				sunk[9] = bit(i, P0, E0, E4);
				sunk[10] = bit(i, P0, E0, E1);
				sunk[11] = bit(i, E0, E1);
				sunk[12] = bit(i, E1, E2);
				sunk[13] = bit(i, P1, E1, E2);
				sunk[14] = bit(i, E0, E2);
				sunk[15] = bit(i, P2, E0, E2);
				sunk[16] = bit(i, P2, E0, E3);
				sunk[17] = bit(i, E0, E3);
				sunk[18] = bit(i, E3, E4);
				sunk[19] = bit(i, P3, E3, E4);
				//@formatter:on

				int cnt1 = 0;
				for (int k = 0; k < tris.length; ++k) {
					int[] poly = tris[k];
					if (poly.length == 4)
						cnt1 += 6; // 2 tris for these ones
					else
						cnt1 += 3;
				}
				MapQuad mq = new MapQuad();
				mq.tris = new int[cnt1];
				ptr = 0;
				for (int k = 0; k < tris.length; ++k) {
					int[] poly = tris[k];
					int off = sunk[k] ? 20 : 0;
					if (poly.length == 4) {
						mq.tris[ptr++] = poly[0] + off;
						mq.tris[ptr++] = poly[1] + off;
						mq.tris[ptr++] = poly[2] + off;

						mq.tris[ptr++] = poly[0] + off;
						mq.tris[ptr++] = poly[2] + off;
						mq.tris[ptr++] = poly[3] + off;
					} else {
						mq.tris[ptr++] = poly[0] + off;
						mq.tris[ptr++] = poly[1] + off;
						mq.tris[ptr++] = poly[2] + off;
					}
				}
				quads[i] = mq;
			}

			LOGGER.info("  done.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static boolean bit(int n, int... bits) {
		for (int bit : bits)
			if ((n & 1 << bit) != 0)
				return true;
		return false;
	}

	public static void barycentric(Vector3 p, Vector3 b, Vector3 c, Vector3 a, Vector3 out) {
		Vector3 v0 = new Vector3(b).sub(a);
		Vector3 v1 = new Vector3(c).sub(a);
		Vector3 v2 = new Vector3(p).sub(a);
		float den = v0.x * v1.y - v1.x * v0.y;
		out.x = (v2.x * v1.y - v1.x * v2.y) / den;
		out.y = (v0.x * v2.y - v2.x * v0.y) / den;
		out.z = 1.0f - out.x - out.y;
	}

	/**
	 * four flags, and five road segments
	 * 
	 * @param f0
	 * @param f1
	 * @param f2
	 * @param f3
	 * @param r0
	 * @param r1
	 * @param r2
	 * @param r3
	 * @param r4
	 * @return
	 */
	public static int key(boolean f0, boolean f1, boolean f2, boolean f3, boolean r0, boolean r1, boolean r2, boolean r3, boolean r4) {
		int n = 0;
		if (f0)
			n |= 1;
		if (f1)
			n |= 2;
		if (f2)
			n |= 4;
		if (f3)
			n |= 8;
		if (r0)
			n |= 16;
		if (r1)
			n |= 32;
		if (r2)
			n |= 64;
		if (r3)
			n |= 128;
		if (r4)
			n |= 256;
		return n;
	}

}
