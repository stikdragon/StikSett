package uk.co.stikman.sett.util;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.co.stikman.utils.math.Vector3i;

public class SettUtil {

	public static Document readXML(InputStream file) throws IOException {
		if (file == null)
			throw new NullPointerException();
		try {
			DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = b.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (Exception e) {
			throw new IOException("Failed to read XML: " + e.getMessage(), e);
		}
	}

	public static List<Element> getElements(Element root, String tagname) {
		if (tagname == null || root == null)
			throw new NullPointerException();
		List<Element> lst = new ArrayList<>();
		Node n = root.getFirstChild();
		while (n != null) {
			if (n instanceof Element && tagname.equals(((Element) n).getTagName()))
				lst.add((Element) n);
			n = n.getNextSibling();
		}
		return lst;
	}

	public static String getAttrib(Element el, String name) {
		if (el == null || name == null)
			throw new NullPointerException();
		if (!el.hasAttribute(name))
			throw new NoSuchElementException(name);
		return el.getAttribute(name);
	}

	public static String getAttrib(Element el, String name, String def) {
		if (el == null || name == null)
			throw new NullPointerException();
		if (!el.hasAttribute(name))
			return def;
		return el.getAttribute(name);
	}

	public static Element getElement(Element root, String childname) {
		if (childname == null || root == null)
			throw new NullPointerException();
		Node n = root.getFirstChild();
		while (n != null) {
			if (n instanceof Element && childname.equals(((Element) n).getTagName()))
				return (Element) n;
			n = n.getNextSibling();
		}
		throw new NoSuchElementException("No child element called: " + childname);
	}

	public static Element optElement(Element root, String childname) {
		if (childname == null || root == null)
			throw new NullPointerException();
		Node n = root.getFirstChild();
		while (n != null) {
			if (n instanceof Element && childname.equals(((Element) n).getTagName()))
				return (Element) n;
			n = n.getNextSibling();
		}
		return null;
	}

	public static String getElementText(Element root, String childname, String def) {
		Element el = optElement(root, childname);
		if (el == null)
			return def;
		return el.getTextContent();
	}

	public static int byteToInt(byte[] fourbytes) {
		int ch1 = fourbytes[0] & 0xff;
		int ch2 = fourbytes[1] & 0xff;
		int ch3 = fourbytes[2] & 0xff;
		int ch4 = fourbytes[3] & 0xff;
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public static byte[] intToByte(int n) {
		byte[] r = new byte[4];
		for (int i = 3; i >= 0; --i) {
			r[i] = (byte) (n & 0xff);
			n >>>= 8;
		}
		return r;
	}

	public interface DrawLineVisitor {
		boolean visit(Vector3i v);
	}

	/**
	 * return <code>true</code> from {@link DrawLineVisitor} to continue
	 * searching, return <code>false</code> to stop
	 * 
	 * @param from
	 * @param to
	 * @param visit
	 */
	public static void drawLine3d(Vector3i from, Vector3i to, DrawLineVisitor visit) {
		Vector3i v = new Vector3i();

		int dx = to.x - from.x;
		int dy = to.y - from.y;
		int dz = to.z - from.z;

		int ax = abs(dx) << 1;
		int ay = abs(dy) << 1;
		int az = abs(dz) << 1;

		int sx = dx == 0 ? 0 : (dx < 0 ? -1 : 1);
		int sy = dy == 0 ? 0 : (dy < 0 ? -1 : 1);
		int sz = dz == 0 ? 0 : (dz < 0 ? -1 : 1);

		int x = from.x;
		int y = from.y;
		int z = from.z;
		int xe = to.x;
		int ye = to.y;
		int ze = to.z;

		if (ax >= max(ay, az)) {
			int yd = ay - (ax >> 1);
			int zd = az - (ax >> 1);
			for (;;) {
				if (!visit.visit(v.set(x, y, z)))
					return;
				if (x == xe)
					return;

				if (yd >= 0) {
					y += sy;
					yd -= ax;
				}

				if (zd >= 0) {
					z += sz;
					zd -= ax;
				}

				x += sx;
				yd += ay;
				zd += az;
			}
		} else if (ay >= max(ax, az)) {
			int xd = ax - (ay >> 1);
			int zd = az - (ay >> 1);
			for (;;) {
				if (!visit.visit(v.set(x, y, z)))
					return;
				if (y == ye)
					return;

				if (xd >= 0) {
					x += sx;
					xd -= ay;
				}

				if (zd >= 0) {
					z += sz;
					zd -= ay;
				}

				y += sy;
				xd += ax;
				zd += az;
			}
		} else if (az >= max(ax, ay)) {
			int xd = ax - (az >> 1);
			int yd = ay - (az >> 1);
			for (;;) {
				if (!visit.visit(v.set(x, y, z)))
					return;
				if (z == ze)
					return;

				if (xd >= 0) {
					x += sx;
					xd -= az;
				}

				if (yd >= 0) {
					y += sy;
					yd -= az;
				}

				z += sz;
				xd += ax;
				yd += ay;
			}
		}
	}

}
