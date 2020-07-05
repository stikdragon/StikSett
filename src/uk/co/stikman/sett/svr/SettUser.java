package uk.co.stikman.sett.svr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.co.stikman.users.User;

public class SettUser extends User{

	public SettUser(String id, String name) {
		super(id, name);
	}

	@Override
	public void saveTo(OutputStream output) throws IOException {
		
	}

	@Override
	public void readFrom(InputStream input) throws IOException {
		
	}

}
