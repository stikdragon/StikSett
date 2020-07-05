package uk.co.stikman.sett.svr;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalServerConnector {
	private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();

	public byte[] recv() throws InterruptedException {
		return queue.take();
	}

	public void send(NetSession session, byte[] msg) throws InterruptedException {
		queue.put(msg);
	}

}
