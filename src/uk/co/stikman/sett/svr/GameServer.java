package uk.co.stikman.sett.svr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.co.stikman.log.StikLog;

public class GameServer extends BaseGameServer {
	private static final StikLog	LOGGER		= StikLog.getLogger(GameServer.class);
	private DestHandlers			handlers	= new DestHandlers();

	public GameServer(GameServerConfig config) {
		super(config);

		handlers.add(new Destination("INFO", false, this::handleINFO));
	}

	@Override
	protected byte[] handle(NetSession sesh, byte[] data) throws ServerException {
		//
		// figure out which game this is for, if any
		//
		try {
			ReceivedMessage msg = new ReceivedMessage(data);
			String inst = msg.getInst();

			Destination dest = handlers.get(inst);
			if (dest == null)
				throw new ServerException("No handler for: " + inst);

			ServerGame game = null;
			if (dest.requiresGame()) {
				game = sesh.getObject("game", ServerGame.class);
				if (game == null)
					throw new ServerException(dest + " requires a Game");
			}

			Object syncon = game == null ? new Object() : game;
			synchronized (syncon) {
				SendMessage reply = new SendMessage();
				dest.getHandler().handle(sesh, msg, reply);
				return reply.getBytes();
			}
		} catch (IOException ex) {
			LOGGER.error(ex);
			throw new ServerException("Unspecified internal error");
		}
	}

	private void handleINFO(NetSession sesh, ReceivedMessage message, SendMessage out) {

	}

	@Override
	protected void handleError(NetSession sesh, Exception error) {

	}

}
