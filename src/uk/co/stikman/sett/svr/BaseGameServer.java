package uk.co.stikman.sett.svr;

import uk.co.stikman.users.Users;

public abstract class BaseGameServer {
	
	private GameServerConfig		config;
	private final Users<SettUser>	users;
	private ServerMode				mode;
	private Thread					localThread;
	private LocalServerConnector	localConnector;
	private boolean					terminate	= false;

	public BaseGameServer(GameServerConfig config) {
		this.config = config;
		users = new Users<>() {
			@Override
			protected SettUser createUserInstance(String uid, String name) {
				return new SettUser(uid, name);
			}
		};
	}

	public void start() {
		this.mode = config.getMode();
		if (mode == ServerMode.LOCAL) {
			localConnector = new LocalServerConnector();
			localThread = new Thread() {
				@Override
				public void run() {
					NetSession sesh = new NetSession();
					for (;;) {
						if (terminate)
							break;
						try {
							byte[] msg = localConnector.recv();
							localConnector.send(sesh, handle(sesh, msg));
						} catch (InterruptedException | ServerException e) {
							handleError(sesh, e);
						}
					}
				}

			};
		} else
			throw new UnsupportedOperationException("Non local mode not supported yet");
	}

	protected abstract void handleError(NetSession sesh, Exception error) ;

	protected abstract byte[] handle(NetSession sesh, byte[] msg) throws ServerException;

	public void terminate() {
		this.terminate = true;
		if (localThread != null)
			localThread.interrupt();
	}

	public Users<SettUser> getUsers() {
		return users;
	}
}
