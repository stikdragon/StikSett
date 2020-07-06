package uk.co.stikman.sett;

import uk.co.stikman.sett.gfx.ui.UI;

public class SettUI extends UI {

	private SettApp sett;

	public SettUI(SettApp sett) {
		super(sett.getWindow());
		this.sett = sett;
	}

}
