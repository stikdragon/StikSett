package uk.co.stikman.sett.client.renderer;

public abstract class MainView {

	public abstract  void setViewport(int w, int h);

	public  abstract void mouseMove(int x, int y);

	public abstract  void mouseDown(int x, int y, int button);

	public abstract  void mouseUp(int x, int y, int button);

	public  abstract void mouseWheel(int dy);

	public abstract  void render();

	public  abstract void update(float dt);

}
