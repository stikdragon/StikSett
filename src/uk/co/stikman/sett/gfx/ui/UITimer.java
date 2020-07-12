package uk.co.stikman.sett.gfx.ui;

public class UITimer {

	private int			interval;
	private UI			ui;
	private Runnable	onTimer;

	long				countdown	= 0;
	private boolean		oneshot;

	public UITimer(UI ui, int interval, Runnable ontimer, boolean oneshot) {
		this.ui = ui;
		this.interval = interval;
		this.onTimer = ontimer;
		this.oneshot = oneshot;
		countdown = interval;
	}

	public int getInterval() {
		return interval;
	}

	public UI getUi() {
		return ui;
	}

	public void cancel() {
		ui.cancelTimer(this);
	}

	boolean update(float dt) {
		countdown -= (dt * 1000.f);
		if (countdown < 0) {
			countdown += interval;
			return true;
		}
		return false;
	}

	void trigger() {
		onTimer.run();
	}

	public Runnable getOnTimer() {
		return onTimer;
	}

	public boolean isOneshot() {
		return oneshot;
	}

}
