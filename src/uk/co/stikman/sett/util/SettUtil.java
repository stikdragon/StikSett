package uk.co.stikman.sett.util;

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
		for (int i = 3; i >=0; --i) {
			r[i] = (byte) (n & 0xff);
			n >>>= 8;
		}
		return r;
	}

}
