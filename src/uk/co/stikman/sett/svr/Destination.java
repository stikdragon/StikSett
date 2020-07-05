package uk.co.stikman.sett.svr;

import java.io.IOException;

public class Destination {
	public interface DestinationHandler {
		void handle(NetSession sesh, ReceivedMessage message, SendMessage reply) throws IOException;
	}

	private final String				name;
	private final DestinationHandler	handler;
	private final boolean				needsGame;

	public Destination(String name, boolean game, DestinationHandler handler) {
		super();
		this.name = name;
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

}
