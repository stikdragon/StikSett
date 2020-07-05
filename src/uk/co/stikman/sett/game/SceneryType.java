package uk.co.stikman.sett.game;

public class SceneryType {
	private final String	name;
	private String			modelName;

	public SceneryType(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

}
