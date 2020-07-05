package uk.co.stikman.sett.svr;

public class GameServerConfig {
	private ServerMode	mode			= ServerMode.LOCAL;
	private int			port			= 20202;
	private int			maxConnections	= 150;

	public ServerMode getMode() {
		return mode;
	}

	public void setMode(ServerMode mode) {
		this.mode = mode;
	}

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
