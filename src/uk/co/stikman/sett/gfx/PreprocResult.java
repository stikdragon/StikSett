package uk.co.stikman.sett.gfx;

import java.util.List;

class PreprocResult {

	private List<Integer>	linemap;
	private List<String>	output;

	public PreprocResult(List<String> output, List<Integer> linemap) {
		this.output = output;
		this.linemap = linemap;
	}

	public List<Integer> getLinemap() {
		return linemap;
	}

	public List<String> getOutput() {
		return output;
	}

	public String[] toArray() {
		String[] res = new String[output.size()];
		int i = 0;
		for (String s : output)
			res[i++] = s;
		return res;
	}

	public int translate(int line) {
		return linemap.get(line);
	}

}
