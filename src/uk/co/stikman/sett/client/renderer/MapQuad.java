package uk.co.stikman.sett.client.renderer;

import java.util.ArrayList;
import java.util.List;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.utils.math.Matrix3;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector3;
import uk.co.stikman.utils.math.Vector4;

public class MapQuad {
	private static final StikLog	LOGGER	= StikLog.getLogger(MapQuad.class);
	public int[]					tris;

	/**
	 * This will be:
	 * 
	 * <pre>
	 * nX, 
	 * nY,
	 * X,
	 * Y,
	 * w0 - barycentric weights for the 4 nodes
	 * w1
	 * w2
	 * w3
	 * 
	 * </pre>
	 */
	public static float[]			verts;

	public static MapQuad[]			quads	= new MapQuad[512];

	private static final int		P0		= 0;
	private static final int		P1		= 1;
	private static final int		P2		= 2;
	private static final int		P3		= 3;
	private static final int		E0		= 4;
	private static final int		E1		= 5;
	private static final int		E2		= 6;
	private static final int		E3		= 7;
	private static final int		E4		= 8;

	static {
		LOGGER.info("Initialising MapQuad..");
		Vector2[] verts = new Vector2[20];
		Vector4[] bary = new Vector4[20];
		int[][] tris = new int[20][];

		float pw = SettApp.PATH_WIDTH / 2.0f;

		verts[0] = new Vector2(0, 0);
		verts[1] = new Vector2(pw, 0);
		verts[2] = new Vector2(1.0f - pw, 0);
		verts[3] = new Vector2(1, 0);

		verts[4] = new Vector2(0, pw);
		verts[5] = new Vector2(pw, pw);
		verts[6] = new Vector2(pw * 2, pw);
		verts[7] = new Vector2(1.0f - pw, pw);
		verts[8] = new Vector2(1, pw);

		verts[9] = new Vector2(0, 1.0f - pw);
		verts[10] = new Vector2(pw, 1.0f - pw);
		verts[11] = new Vector2(1.0f - 2 * pw, 1.0f - pw);
		verts[12] = new Vector2(1.0f - pw, 1.0f - pw);
		verts[13] = new Vector2(1, 1.0f - pw);

		verts[14] = new Vector2(0, 1);
		verts[15] = new Vector2(pw, 1);
		verts[16] = new Vector2(1.0f - pw, 1);
		verts[17] = new Vector2(1, 1);

		verts[18] = new Vector2(pw, pw * 2);
		verts[19] = new Vector2(1.0f - pw, 1.0f - pw * 2);

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

		MapQuad.verts = new float[verts.length * 8];

		Matrix3 m = SettApp.skewMatrix(new Matrix3());
		Vector2 v2 = new Vector2();
		int ptr = 0;
		for (Vector2 v : verts) {
			MapQuad.verts[ptr++] = v.x;
			MapQuad.verts[ptr++] = v.y;
			v2 = m.multiply(v, v2);
			MapQuad.verts[ptr++] = v2.x;
			MapQuad.verts[ptr++] = v2.y;
			ptr += 4;
		}

		//
		// barycentric weights for interpolation height 
		//
		char[] leftOrRight = "LRRRLLRRRLLLLRLLLLLR".toCharArray();
		for (int i = 0; i < leftOrRight.length; ++i) {
			if (leftOrRight[i] == 'L') {
				Vector2 a = verts[0];
				Vector2 b = verts[17];
				Vector2 c = verts[14];
				Vector3 out = new Vector3();
				barycentric(verts[i], a, b, c, out);
				MapQuad.verts[i * 8 + 4] = out.x;
				MapQuad.verts[i * 8 + 5] = 0.0f;
				MapQuad.verts[i * 8 + 6] = out.y;
				MapQuad.verts[i * 8 + 7] = out.z;
			} else {
				Vector2 a = verts[0];
				Vector2 b = verts[3];
				Vector2 c = verts[17];
				Vector3 out = new Vector3();
				barycentric(verts[i], a, b, c, out);
				bary[i] = new Vector4(out.x, out.y, out.z, 0.0f);
				MapQuad.verts[i * 8 + 4] = out.x;
				MapQuad.verts[i * 8 + 5] = out.y;
				MapQuad.verts[i * 8 + 6] = out.z;
				MapQuad.verts[i * 8 + 7] = 0.0f;
			}
		}
		
		//
		// generate the 1024 combinations that can exist
		//
		for (int i = 0; i < 512; ++i) {
			List<int[]> use = new ArrayList<>();

			//@formatter:off
			use.add(tris[0]);
			use.add(tris[1]);
			
			if (!bit(i, E1)) use.add(tris[2]);
			if (!bit(i, E2)) use.add(tris[3]);
			if (!bit(i, E0)) use.add(tris[4]);
			if (!bit(i, E3)) use.add(tris[5]);
			if (!bit(i, E4)) use.add(tris[6]);
			if (!bit(i, E0)) use.add(tris[7]);

			if (!bit(i, E0, E4))     use.add(tris[8]);
			if (!bit(i, P0, E0, E4)) use.add(tris[9]);
			if (!bit(i, P0, E0, E1)) use.add(tris[10]);
			if (!bit(i, E0, E1))     use.add(tris[11]);
			if (!bit(i, E1, E2))     use.add(tris[12]);
			if (!bit(i, P1, E1, E2)) use.add(tris[13]);
			if (!bit(i, E0, E2))     use.add(tris[14]);
			if (!bit(i, P2, E0, E2)) use.add(tris[15]);
			if (!bit(i, P2, E0, E3)) use.add(tris[16]);
			if (!bit(i, E0, E3))     use.add(tris[17]);
			if (!bit(i, E3, E4 ))    use.add(tris[18]);
			if (!bit(i, P3, E3, E4)) use.add(tris[19]);
			//@formatter:on

			int cnt = 0;
			for (int[] poly : use) {
				if (poly.length == 4)
					cnt += 6; // 2 tris for these ones
				else
					cnt += 3;
			}
			MapQuad mq = new MapQuad();
			mq.tris = new int[cnt];
			ptr = 0;
			for (int[] poly : use) {
				if (poly.length == 4) {
					mq.tris[ptr++] = poly[0];
					mq.tris[ptr++] = poly[1];
					mq.tris[ptr++] = poly[2];

					mq.tris[ptr++] = poly[0];
					mq.tris[ptr++] = poly[2];
					mq.tris[ptr++] = poly[3];
				} else {
					mq.tris[ptr++] = poly[0];
					mq.tris[ptr++] = poly[1];
					mq.tris[ptr++] = poly[2];
				}
			}
			quads[i] = mq;
		}

		LOGGER.info("  done.");
	}

	private static boolean bit(int n, int... bits) {
		for (int bit : bits)
			if ((n & 1 << bit) != 0)
				return true;
		return false;
	}

	public static void barycentric(Vector2 p, Vector2 b, Vector2 c, Vector2 a, Vector3 out) {
		Vector2 v0 = new Vector2(b).sub(a);
		Vector2 v1 = new Vector2(c).sub(a);
		Vector2 v2 = new Vector2(p).sub(a);
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
