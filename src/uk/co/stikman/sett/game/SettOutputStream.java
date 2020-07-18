package uk.co.stikman.sett.game;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import uk.co.stikman.utils.StikDataOutputStream;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class SettOutputStream extends StikDataOutputStream {

	private IdentityHashMap<IsSerializable, Integer>		objects	= new IdentityHashMap<>();
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
				writeInt(0); // new object
				Integer idx = Integer.valueOf(objects.size() + 1);
				objects.put(o, idx);
				writeClass(o.getClass());
				o.toStream(this);
			} else {
				writeInt(i.intValue()); // existing object
			}
		}

	}

	private void writeClass(Class<? extends IsSerializable> cls) throws IOException {
		Integer i = classes.get(cls);
		if (i == null) {
			writeInt(0);
			writeString(cls.getName());
			classes.put(cls, Integer.valueOf(classes.size() + 1));
		} else {
			writeInt(i.intValue());
		}
	}

	public void writeVec3(Vector3 v) throws IOException {
		writeFloat(v.x);
		writeFloat(v.y);
		writeFloat(v.z);
	}

	public void writeVec2i(Vector2i v) throws IOException {
		writeInt(v.x);
		writeInt(v.y);
	}

	public void writeBoolean(boolean b) throws IOException {
		writeByte((byte) (b ? 1 : 0));
	}
}
