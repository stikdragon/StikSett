package uk.co.stikman.sett;

import java.io.IOException;
import java.util.List;

import uk.co.stikman.sett.game.Building;
import uk.co.stikman.sett.game.Flag;
import uk.co.stikman.sett.game.Player;
import uk.co.stikman.utils.StikDataInputStream;

public class ClientGame extends BaseGame {

	public ClientGame(SettApp app) {
		super(app);
	}

	public void fromStream(StikDataInputStream in) throws IOException {
		SettInputStream str = new SettInputStream(in);
		str.addObjectConstructor(Building.class, () -> new Building(this));
		str.addObjectConstructor(Flag.class, () -> new Flag(this));
		
		//
		// this is fairly complex to do
		//
		getWorld().getParams().fromStream(str);
		setName(str.readString());

		int cnt = str.readInt();
		while (cnt-- > 0) {
			Player p = str.readObject(Player.class);
			players.put(p.getName(), p);
		}

		// TODO: validate these
		List<String> modelNames = str.readStringList();
		List<String> buildingDefNames = str.readStringList();
		List<String> sceneryDefNames = str.readStringList();

		cnt = str.readInt();
		while (cnt --> 0) 
			getWorld().getBuildings().put(str.readObject(Building.class));
		
		cnt = str.readInt();
		while (cnt --> 0) 
			getWorld().getFlags().put(str.readObject(Flag.class));
		
		
		
		
		
	}

}
