package uk.co.stikman.sett.game;

public class BuildingType {
	private final String	name;
	private String			display;
	private String			description;
	private String			modelName;

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getName() {
		return name;
	}

	public BuildingType(String name, String display) {
		super();
		this.name = name;
		this.display = display;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

}
