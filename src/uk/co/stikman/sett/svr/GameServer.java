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
import uk.co.stikman.sett.Game;
import uk.co.stikman.sett.SettApp;
import uk.co.stikman.sett.game.Building;
import uk.co.stikman.sett.game.BuildingType;
import uk.co.stikman.sett.game.Flag;
import uk.co.stikman.sett.game.GenerateOptions;
import uk.co.stikman.sett.game.Noddy;
import uk.co.stikman.sett.game.Player;
import uk.co.stikman.sett.game.Road;
import uk.co.stikman.sett.game.SettOutputStream;
import uk.co.stikman.sett.game.Terrain;
import uk.co.stikman.sett.game.TerrainNode;
import uk.co.stikman.sett.game.World;
import uk.co.stikman.sett.game.WorldParameters;
import uk.co.stikman.sett.gameevents.GameEvent;
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

		handlers.add(new Destination("BILD", true, true, this::handleBILD));
		handlers.add(new Destination("POLL", true, true, this::handlePOLL));
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
					throw new ServerException(ServerException.E_UNSPECIFIED, "No handler for: " + inst);
				else
					throw new ServerException(ServerException.E_UNAUTHORISED, "Not Authorised");

			}

			ServerGame game = null;
			if (dest.requiresGame()) {
				game = sesh.getObject("game", ServerGame.class);
				if (game == null)
					throw new ServerException(ServerException.E_UNSPECIFIED, dest + " requires a Game");
			}

			if (dest.requiresUser()) {
				SettUser user = sesh.getObject("user", SettUser.class);
				if (user == null)
					throw new ServerException(ServerException.E_UNAUTHORISED, dest + " requires a User");
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
			throw se;
			//			return handleError(se);
		} catch (Exception ex) {
			LOGGER.error(ex);
			throw new ServerException(ServerException.E_UNSPECIFIED, "Unspecified internal error");
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
			throw new ServerException(ServerException.E_LOGIN_FAILED, "Login Failed");
		}
		if (!users.tryLogin(u, pwd)) {
			LOGGER.warn("Login failed for user: " + usr);
			throw new ServerException(ServerException.E_LOGIN_FAILED, "Login Failed");
		}
		sesh.setObject("user", u);
	}

	private void handleNEWG(NetSession sesh, ReceivedMessage message, StikDataOutputStream out) throws Exception {
		//
		// Create a new game, and join this user to it as the first player
		//
		ServerGame game;
		String name = message.readString();
		WorldParameters params = new WorldParameters();
		params.fromBytes(message.readBuf());
		synchronized (games) {
			game = new ServerGame(app);
			games.put(name, game);
			game.setName(name);
		}

		synchronized (game) {
			SettUser u = getUser(sesh);

			game.setWorld(new World(game));
			game.loadResources();
			game.getWorld().setParams(params);
			GenerateOptions opts = new GenerateOptions();
			game.getWorld().generate(opts);

			game.addPlayer(new Player(game, 1, u.getName(), VectorColours.HSLtoRGB(new Vector3(0, 1.0f, 0.6f))));
			game.addPlayer(new Player(game, 2, "Player2", VectorColours.HSLtoRGB(new Vector3(0.75f, 1.0f, 0.7f))));
			game.addPlayer(new Player(game, 3, "Player3", VectorColours.HSLtoRGB(new Vector3(0.6f, 1.0f, 0.6f))));
			randomRoads(game);
			randomFlags(game);
			randomNoddies(game);
			sesh.setObject("game", game);
		}
	}

	private void randomNoddies(Game game) {
		Random rng = new Random();
		Noddy n = new Noddy(game, game.nextId());
		n.setOwner(game.getPlayer("Stik"));
		n.setPosition(4, 6, 10);
		n.setType(game.getNoddyDef("carrier"));
		game.getWorld().getNoddies().put(n);
	}
	private void randomFlags(Game game) {
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

	private void randomRoads(Game game) {
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
			writeKeys(out, g.getBuildingDefs());
			writeKeys(out, g.getSceneryDefs());
			out.writeInt(g.getNoddyDefs().getKeys().size());
			for (String s : g.getNoddyDefs().getKeys())
				out.writeString(s);

			out.writeInt(w.getBuildings().size());
			for (Building b : w.getBuildings())
				out.writeObject(b);

			out.writeInt(w.getFlags().size());
			for (Flag f : w.getFlags())
				out.writeObject(f);

			out.writeInt(w.getRoads().size());
			for (Road r : w.getRoads())
				out.writeObject(r);

			out.writeInt(w.getNoddies().size());
			for (Noddy n : w.getNoddies())
				out.writeObject(n);

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

	private void handlePOLL(NetSession sesh, ReceivedMessage message, StikDataOutputStream sdos) throws Exception {
		LOGGER.info("Poll...");
		SettOutputStream out = new SettOutputStream(sdos);
		ServerGame g = getGame(sesh);
		synchronized (g) {
			Player player = g.getPlayer(getUser(sesh).getName());
			List<GameEvent> lst = player.extractEvents();
			out.writeInt(lst.size());
			for (GameEvent ev : lst) {
				out.writeString(ev.getClass().getSimpleName());
				ev.toStream(out);
			}
		}
	}

	private void handleBILD(NetSession sesh, ReceivedMessage message, StikDataOutputStream sdos) throws Exception {
		SettOutputStream out = new SettOutputStream(sdos);
		String id = message.readString();
		int posx = message.readInt();
		int posy = message.readInt();

		ServerGame g = getGame(sesh);
		synchronized (g) {
			Player player = g.getPlayer(getUser(sesh).getName());
			g.build(player, id, posx, posy);
			out.writeString("OK");
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
