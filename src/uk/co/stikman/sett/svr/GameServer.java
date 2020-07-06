package uk.co.stikman.sett.svr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.BaseGame;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.game.Building;
import uk.co.stikman.sett.game.Flag;
import uk.co.stikman.sett.game.IsNodeObject;
import uk.co.stikman.sett.game.Player;
import uk.co.stikman.sett.game.Road;
import uk.co.stikman.sett.game.SettOutputStream;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gfx.VectorColours;
import uk.co.stikman.users.Users;
import uk.co.stikman.utils.StikDataOutputStream;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class GameServer extends BaseGameServer {
	private final SettApp			app;
	private static final StikLog	LOGGER			= StikLog.getLogger(GameServer.class);
	private static final int		SERVER_VERSION	= SettApp.VERSION;
	private DestHandlers			handlers		= new DestHandlers();
	private final Users<SettUser>	users;

	private Map<String, ServerGame>	games			= new HashMap<>();

	public GameServer(GameServerConfig config, SettApp app) {
		super(config);
		this.app = app;

		users = new Users<>() {
			@Override
			protected SettUser createUserInstance(String uid, String name) {
				return new SettUser(uid, name);
			}
		};

		users.create("Stik", "password", true);

		handlers.add(new Destination("INFO", false, false, this::handleINFO));
		handlers.add(new Destination("AUTH", false, false, this::handleAUTH));
		handlers.add(new Destination("NEWG", true, false, this::handleNEWG));
		handlers.add(new Destination("GINI", true, false, this::handleGINI));
	}

	public Users<SettUser> getUsers() {
		return users;
	}

	private static SettUser getUser(NetSession sesh) {
		SettUser u = sesh.getObject("user", SettUser.class);
		if (u == null)
			throw new NoSuchElementException("User not present");
		return u;
	}

	private static ServerGame getGame(NetSession sesh) {
		ServerGame g = sesh.getObject("game", ServerGame.class);
		if (g == null)
			throw new NoSuchElementException("Game not set");
		return g;
	}

	@Override
	protected byte[] handle(NetSession sesh, byte[] data) throws ServerException {
		//
		// figure out which game this is for, if any
		//
		try {
			ReceivedMessage msg = new ReceivedMessage(data);
			String inst = msg.read4();

			Destination dest = handlers.get(inst);
			if (dest == null) {
				SettUser user = sesh.getObject("user", SettUser.class);
				if (user != null)
					throw new ServerException(ServerException.UNSPECIFIED, "No handler for: " + inst);
				else
					throw new ServerException(ServerException.UNAUTHORISED, "Not Authorised");

			}

			ServerGame game = null;
			if (dest.requiresGame()) {
				game = sesh.getObject("game", ServerGame.class);
				if (game == null)
					throw new ServerException(ServerException.UNSPECIFIED, dest + " requires a Game");
			}

			if (dest.requiresUser()) {
				SettUser user = sesh.getObject("user", SettUser.class);
				if (user == null)
					throw new ServerException(ServerException.UNAUTHORISED, dest + " requires a User");
			}

			Object syncon = game == null ? new Object() : game;
			synchronized (syncon) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StikDataOutputStream sdos = new StikDataOutputStream(baos);
				dest.getHandler().handle(sesh, msg, sdos);
				return baos.toByteArray();
			}
		} catch (ServerException se) {
			LOGGER.warn("ServerException: " + se.getMessage());
			return handleError(se);
		} catch (Exception ex) {
			LOGGER.error(ex);
			return handleError(new ServerException(ServerException.UNSPECIFIED, "Unspecified internal error"));
		}
	}

	private byte[] handleError(ServerException se) {
		try {
			SendMessage msg = new SendMessage();
			msg.write4("ERRO");
			msg.writeInt(se.getCode());
			msg.writeString(se.getMessage());
			return msg.getBytes();
		} catch (IOException ex) {
			throw new RuntimeException("Unexpected internal error: " + ex.getMessage(), ex);
		}
	}

	private void handleINFO(NetSession sesh, ReceivedMessage message, StikDataOutputStream out) throws Exception {
		out.writeInt(SERVER_VERSION);
	}

	private void handleAUTH(NetSession sesh, ReceivedMessage message, StikDataOutputStream out) throws Exception {
		//
		// try and log user in
		//
		String usr = message.readString();
		String pwd = message.readString();
		SettUser u = users.find(usr);
		if (u == null) {
			LOGGER.warn("Login for missing user: " + usr);
			throw new ServerException(ServerException.LOGIN_FAILED, "Login Failed");
		}
		if (!users.tryLogin(u, pwd)) {
			LOGGER.warn("Login failed for user: " + usr);
			throw new ServerException(ServerException.LOGIN_FAILED, "Login Failed");
		}
		sesh.setObject("user", u);
	}

	private void handleNEWG(NetSession sesh, ReceivedMessage message, StikDataOutputStream out) throws Exception {
		//
		// Create a new game, and join this user to it as the first player
		//
		ServerGame game;
		synchronized (games) {
			String name = message.readString();
			game = new ServerGame(app);
			games.put(name, game);
			game.setName(name);
		}

		synchronized (game) {
			SettUser u = getUser(sesh);

			game.setWorld(new World(game));
			game.loadResources();
			WorldParameters params = new WorldParameters(1);
			game.getWorld().generate(params);

			game.addPlayer(new Player(game, 1, u.getId(), VectorColours.HSLtoRGB(new Vector3(0, 1.0f, 0.6f))));
			game.addPlayer(new Player(game, 2, "Player2", VectorColours.HSLtoRGB(new Vector3(0.75f, 1.0f, 0.7f))));
			game.addPlayer(new Player(game, 3, "Player3", VectorColours.HSLtoRGB(new Vector3(0.6f, 1.0f, 0.6f))));
			randomRoads(game);
			randomFlags(game);
			sesh.setObject("game", game);
		}
	}

	private void randomFlags(BaseGame game) {
		List<Player> players = game.getPlayers().values().stream().collect(Collectors.toList());
		Random rng = new Random();
		for (int y = 0; y < game.getWorld().getHeight(); ++y) {
			for (int x = 0; x < game.getWorld().getWidth(); ++x) {
				TerrainNode n = game.getWorld().getTerrain().get(x, y);
				List<Road> lst = game.getWorld().getRoadsAt(n, new ArrayList<>());
				if (lst.size() > 2 || lst.size() == 1) {
					int idx = rng.nextInt(players.size());
					n.setObject(new Flag(game, players.get(idx)));
				}
			}
		}
	}

	private void randomRoads(BaseGame game) {
		Random rng = new Random();
		int n = 40 * game.getWorld().getWidth() * game.getWorld().getHeight() / 50000;
		for (int k = 0; k < n; ++k) {
			List<Vector2i> nodes = new ArrayList<>();
			float dir = 0.0f;
			int chg = 0;
			int x = rng.nextInt(game.getWorld().getWidth());
			int y = rng.nextInt(game.getWorld().getHeight());
			for (int i = 0; i < 100; ++i) {
				if (chg-- < 0) {
					chg = rng.nextInt(2);
					dir += rng.nextFloat() - 0.5f;
				}
				int dx = (int) Math.round(Math.sin(dir));
				int dy = (int) Math.round(Math.cos(dir));
				if (dx == 0 && dy == 0)
					continue;
				if ((dx == -1 && dy == 1) || (dx == 1 && dy == -1))
					continue; // illegal shape

				x += dx;
				y += dy;
				Vector2i nuw = new Vector2i(x, y);
				nodes.add(nuw);
			}
			game.addRoad(nodes);
		}
	}

	private void handleGINI(NetSession sesh, ReceivedMessage message, StikDataOutputStream sdos) throws Exception {
		ServerGame g = getGame(sesh);
		SettOutputStream out = new SettOutputStream(sdos);
		synchronized (g) {
			World w = g.getWorld();
			w.getParams().toStream(out);
			out.writeString(g.getName());
			out.writeInt(g.getPlayers().size());
			for (Player p : g.getPlayers().values()) 
				out.writeObject(p);

			//
			// write the keylists for the various definitions, we don't actually
			// transfer these but send all their names so the client can check it has
			// everything and hasn't got corrupted somehow.  We alreayd do the version
			// check when they first connect, but can't hurt to be safe
			//
			writeKeys(out, g.getModels());
			writeKeys(out, w.getBuildingDefs());
			writeKeys(out, w.getSceneryDefs());

			out.writeInt(w.getBuildings().size());
			for (Building b : w.getBuildings()) {
				b.toStream(out);
			}

			out.writeInt(w.getFlags().size());
			for (Flag f : w.getFlags())
				f.toStream(out);

			out.writeInt(w.getRoads().size());
			for (Road r : w.getRoads())
				r.toStream(out);

			Terrain t = w.getTerrain();
			out.writeInt(t.getWidth());
			out.writeInt(t.getHeight());
			for (int y = 0; y < t.getHeight(); ++y) {
				for (int x = 0; x < t.getWidth(); ++x) {
					TerrainNode n = t.get(x, y);
					out.writeByte((byte) n.getOwner());
					out.writeFloat(n.getHeight());
					out.writeByte((byte) n.getType());
					out.writeObject(n.getObject());
				}
			}
		}

	}

	public static void writeVec3(StikDataOutputStream str, Vector3 v) throws IOException {
		str.writeFloat(v.x);
		str.writeFloat(v.y);
		str.writeFloat(v.z);
	}

	private void writeKeys(StikDataOutputStream out, Map<String, ?> map) throws IOException {
		out.writeInt(map.size());
		for (String s : map.keySet())
			out.writeString(s);
	}

}
