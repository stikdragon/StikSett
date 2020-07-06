package uk.co.stikman.sett.svr;

import uk.co.stikman.utils.StikDataOutputStream;

public class Destination {
	public interface DestinationHandler {
		void handle(NetSession sesh, ReceivedMessage message, StikDataOutputStream reply) throws Exception;
	}

	private final String				name;
	private final DestinationHandler	handler;
	private final boolean				needsGame;
	private boolean						needsUser;

	public Destination(String name, boolean user, boolean game, DestinationHandler handler) {
		super();
		this.name = name;
		this.needsUser = user;
		this.needsGame = game;
		this.handler = handler;
	}

	public DestinationHandler getHandler() {
		return handler;
	}

	public boolean requiresGame() {
		return needsGame;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public boolean requiresUser() {
		return needsUser;
	}

}
