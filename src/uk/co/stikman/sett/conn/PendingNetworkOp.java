package uk.co.stikman.sett.conn;

public class PendingNetworkOp {

	private ResponseHandler handler;

	public ResponseHandler getHandler() {
		return handler;
	}

	public PendingNetworkOp(ResponseHandler handler) {
		super();
		this.handler = handler;
	}

}
