package uk.co.stikman.sett.conn;

import java.io.ByteArrayInputStream;

import uk.co.stikman.sett.svr.ServerException;
import uk.co.stikman.utils.StikByteArrayInputStream;
import uk.co.stikman.utils.StikDataInputStream;
import uk.co.stikman.utils.Utils;

public class Response {
	private int				id;
	private byte[]			data;
	private ServerException	error;

	public int getId() {
		return id;
	}

	/**
	 * return the data, or throw the exception
	 * 
	 * @return
	 * @throws ServerException
	 */
	public byte[] getData() throws ServerException {
		if (isError())
			throw error;
		return data;
	}

	public ServerException getError() {
		return error;
	}

	public boolean isError() {
		return error != null;
	}

	public Response(int id, byte[] data) {
		super();
		this.id = id;
		this.data = data;
	}

	public Response(int id, ServerException error) {
		super();
		this.id = id;
		this.error = error;
	}

	@Override
	public String toString() {
		if (isError())
			return "Response: Error - " + error.toString();
		else
			return "Response: " + Utils.formatBytes(data);
	}

	public StikDataInputStream asStream() throws ServerException {
		return new StikDataInputStream(new StikByteArrayInputStream(getData()));
	}

}