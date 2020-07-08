package uk.co.stikman.sett.client.renderer;

import uk.co.stikman.sett.SettApp;

public abstract class BaseView {
	private SettApp app;

	public BaseView(SettApp app) {
		this.app = app;
	}

	public abstract void setViewport(int w, int h);

	public void shown() {
	}

	public void hidden() {
	}

	public void mouseMove(int x, int y) {
	}

	public void mouseDown(int x, int y, int button) {
	}

	public void mouseUp(int x, int y, int button) {
	}

	public void mouseWheel(int dy) {
	}

	public void render() {
	}

	public void update(float dt) {
	}

	public SettApp getApp() {
		return app;
	}

}
