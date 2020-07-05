package uk.co.stikman.sett.svr;

public enum ServerMode {
	LOCAL, // runs in-process, single player only
	SOCKET // listens on a socket, multiplayer
}
