package uk.co.stikman.sett.gfx.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Handy class to format a little table of strings. Useful in debugging stuff
 * mostly, and visualising things
 * 
 * @author stik
 *
 */
public class StringTable {
	private List<List<String>>	data	= new ArrayList<>();
	private List<String>		fields	= new ArrayList<>();

	public StringTable(String... fields) {
		for (String s : fields)
			this.fields.add(s);
	}

	public void add(String... vals) {
		List<String> lst = new ArrayList<>();
		for (String s : vals)
			lst.add(s);
		data.add(lst);
	}

	@Override
	public String toString() {
		int n = fields.size();
		int[] sizes = new int[n];
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String s : fields)
			sizes[i++] = Math.max(3, s.length());
		for (List<String> lst : data) {
			i = 0;
			for (String s : lst) {
				if (s != null)
					sizes[i] = Math.max(sizes[i], s.length());
				++i;
			}
		}

		for (int j = 0; j < sizes.length; ++j)
			sizes[j] += 2;

		i = 0;
		for (String s : fields) {
			int sz = sizes[i++];
			sb.append(expand(s, sz));
		}
		sb.append("\n");
		i = 0;
		for (String s : fields) {
			int sz = sizes[i++];
			sb.append(expand("=================================================", sz - 2)).append("  "); // derp
		}
		sb.append("\n");

		for (List<String> lst : data) {
			i = 0;
			for (String s : lst) {
				int sz = sizes[i++];
				sb.append(expand(s, sz));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private String expand(String s, int sz) {
		//
		// pad with spaces
		//
		if (s == null) {
			char[] ch = new char[sz];
			for (int i = 0; i < sz; ++i)
				ch[i] = ' ';
			return new String(ch);
		}
		char[] ch = new char[sz];
		int len = s.length();
		for (int i = 0; i < sz; ++i) {
			if (i < len)
				ch[i] = s.charAt(i);
			else
				ch[i] = ' ';
		}
		return new String(ch);
	}

}
