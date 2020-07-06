package uk.co.stikman.sett.svr;

import uk.co.stikman.sett.SettApp;

public class GameServerConfig {
	private int	port			= SettApp.DEFAULT_PORT;
	private int	maxConnections	= 150;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

}
