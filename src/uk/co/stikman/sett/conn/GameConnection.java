package uk.co.stikman.sett.conn;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.stikman.log.StikLog;
import uk.co.stikman.sett.svr.ServerException;
import uk.co.stikman.utils.StikDataInputStream;
import uk.co.stikman.utils.StikDataOutputStream;

public class GameConnection {
	private static final StikLog	LOGGER	= StikLog.getLogger(GameConnection.class);
	private Socket					sock;

	private LinkedList<Response>	recvQ	= new LinkedList<>();
	private AtomicInteger			ids		= new AtomicInteger();

	private StikDataOutputStream	os;


	public void connect(String host, int port) throws UnknownHostException, IOException {
		sock = new Socket(host, port);
		os = new StikDataOutputStream(sock.getOutputStream());
		StikDataInputStream is = new StikDataInputStream(sock.getInputStream());
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					for (;;) {
						int id = is.readInt();
						int len = is.readInt();
						int status = is.readByte();
						if (status == 2) {
							int errcode = is.readInt();
							byte[] buf = is.readNBytes(len);
							synchronized (recvQ) {
								recvQ.add(new Response(id, new ServerException(errcode, new String(buf, StandardCharsets.UTF_8))));
							}
						} else if (status == 1) {
							byte[] buf = is.readNBytes(len);
							synchronized (recvQ) {
								recvQ.add(new Response(id, buf));
							}
						} else
							throw new RuntimeException("Invalid stream");
					}
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		};
		t.start();
	}

	/**
	 * this can be either a byte[] or a ServerException (or null if there isn't
	 * anything in the buffer)
	 * 
	 * @return
	 */
	public Response extract() {
		synchronized (recvQ) {
			if (recvQ.isEmpty())
				return null;

			return recvQ.removeFirst();
		}
	}

	/**
	 * returns an ID to identify it when you call {@link #extract()}
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public int send(byte[] request) throws IOException {
		int id = ids.incrementAndGet();
		os.writeInt(id);
		os.writeInt(request.length);
		os.write(request);
		return id;
	}

}
