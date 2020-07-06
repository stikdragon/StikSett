package uk.co.stikman.sett.game;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import uk.co.stikman.utils.StikDataOutputStream;
import uk.co.stikman.utils.math.Vector3;

public class SettOutputStream extends StikDataOutputStream {

	private Map<IsSerializable, Integer>					objects	= new HashMap<>();
	private Map<Class<? extends IsSerializable>, Integer>	classes	= new HashMap<>();

	public SettOutputStream(OutputStream out) {
		super(out);
	}

	public void writeObject(IsSerializable o) throws IOException {
		if (o == null) {
			writeInt(-1);
		} else {
			Integer i = objects.get(o);
			if (i == null) {
				out.write(0); // new object
				objects.put(o, Integer.valueOf(objects.size() + 1));
				writeClass(o.getClass());
				o.toStream(this);
			} else {
				out.write(i.intValue()); // existing object
			}
		}

	}

	private void writeClass(Class<? extends IsSerializable> cls) throws IOException {
		Integer i = classes.get(cls);
		if (i == null) {
			write(0);
			writeString(cls.getName());
			classes.put(cls, Integer.valueOf(classes.size() + 1));
		} else {
			write(i.intValue());
		}
	}

	public void writeVec3(Vector3 v) throws IOException {
		writeFloat(v.x);
		writeFloat(v.y);
		writeFloat(v.z);
	}
}
