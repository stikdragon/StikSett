package uk.co.stikman.sett.svr;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.util.SettUtil;

public abstract class BaseGameServer {
	private static final StikLog	LOGGER		= StikLog.getLogger(BaseGameServer.class);
	private GameServerConfig		config;

	private boolean					terminate	= false;
	private Thread					mainthread;
	private ExecutorService			exec		= new ThreadPoolExecutor(0, 32, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	private ServerSocket			socket;
	private Set<Socket>				open		= new HashSet<Socket>();

	public BaseGameServer(GameServerConfig config) {
		this.config = config;
	}

	public void start() throws IOException {
		socket = new ServerSocket();
		socket.bind(new InetSocketAddress(config.getPort()));
		mainthread = new Thread(new Runnable() {
			@Override
			public void run() {
				do {
					try {
						handleConnection(socket.accept());
					} catch (IOException e) {
						if (!terminate) {
							LOGGER.error("Error on termination");
							LOGGER.error(e);
						}
					}
				} while (!socket.isClosed());
			}
		});
		mainthread.setDaemon(true);
		mainthread.setName("Settlers Accept Thread");
		mainthread.start();
	}

	protected void handleConnection(final Socket sock) throws SocketException {
		synchronized (open) {
			open.add(sock);
		}
		sock.setSoTimeout(0);
		Runnable t = new Runnable() {
			@Override
			public void run() {
				NetSession sesh = new NetSession();

				InputStream inputStream = null;
				OutputStream outputStream = null;
				try {
					try {
						inputStream = sock.getInputStream();
						outputStream = sock.getOutputStream();

						while (sock.isConnected()) {
							//
							// read length
							//
							int id = SettUtil.byteToInt(inputStream.readNBytes(4));
							int n = SettUtil.byteToInt(inputStream.readNBytes(4));
							byte[] data = inputStream.readNBytes(n);
							try {
								data = handle(sesh, data);
								outputStream.write(SettUtil.intToByte(id));
								outputStream.write(SettUtil.intToByte(data.length)); // length
								outputStream.write(1); // "success"
								outputStream.write(data); // bytes
//								LOGGER.debug("Message size recv/sent: " + data.length + " / " + n);
							} catch (ServerException e) {
								LOGGER.warn("Sending error to client: " + e.toString());
								byte[] x = e.getMessage().getBytes(StandardCharsets.UTF_8);
								outputStream.write(SettUtil.intToByte(id));
								outputStream.write(SettUtil.intToByte(x.length)); // length
								outputStream.write(2); // "error"
								outputStream.write(SettUtil.intToByte(e.getCode())); // code
								outputStream.write(x); // bytes
							}
							outputStream.flush();
						}

					} catch (EOFException eof) {
						// connection closed
					} catch (IOException e) {
						LOGGER.error(e);
					}
				} finally {
					close(inputStream);
					close(outputStream);
					close(sock);
					synchronized (open) {
						open.remove(sock);
					}
				}
			}

		};
		exec.execute(t);
	}

	private void close(Closeable x) {
		try {
			x.close();
		} catch (IOException e) {
			LOGGER.warn("Error on closing " + x.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	protected abstract byte[] handle(NetSession sesh, byte[] msg) throws ServerException;

	public void terminate() {
		this.terminate = true;
		close(socket);
		exec.shutdown();
		open.forEach(this::close);
	}

	public GameServerConfig getConfig() {
		return config;
	}

}
