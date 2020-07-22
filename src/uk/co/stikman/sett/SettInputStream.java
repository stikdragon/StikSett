package uk.co.stikman.sett;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import uk.co.stikman.sett.game.IsSerializable;
import uk.co.stikman.sett.game.NoddyAim;
import uk.co.stikman.utils.StikDataInputStream;
import uk.co.stikman.utils.math.Vector2;
import uk.co.stikman.utils.math.Vector2i;
import uk.co.stikman.utils.math.Vector3;

public class SettInputStream extends StikDataInputStream {

	private interface ConstructObject {
		IsSerializable construct() throws IOException;
	}

	private List<ConstructObject>	classSupps	= new ArrayList<>();
	private List<IsSerializable>	objects		= new ArrayList<>();
	private ObjectConstructors		xtors		= new ObjectConstructors();

	public SettInputStream(InputStream src) {
		super(src);
	}

	public SettInputStream(byte[] data) {
		this(new ByteArrayInputStream(data));
	}

	@SuppressWarnings("unchecked")
	public <T extends IsSerializable> T readObject(Class<T> type) throws IOException {
		int id = readInt();
		if (id == -1)
			return null;
		if (id == 0) {
			ConstructObject supp = readClass();
			IsSerializable r = supp.construct();
			objects.add(r);
			r.fromStream(this);
			return (T) r;
		} else {
			return (T) objects.get(id - 1);
		}

	}

	private ConstructObject readClass() throws IOException {
		int id = readInt();
		if (id == 0) {
			String name = readString();
			try {
				Class<?> cls = Class.forName(name);
				if (!IsSerializable.class.isAssignableFrom(cls))
					throw new IOException("Invalid class in stream: " + name);
				for (Constructor<?> c : cls.getConstructors()) {
					if (c.getParameterCount() == 0) {
						ConstructObject x = () -> {
							try {
								return (IsSerializable) c.newInstance();
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								throw new IOException("Failed to construct object: " + name);
							}
						};
						classSupps.add(x);
						return x;
					}
				}
				Supplier<IsSerializable> supp = xtors.find(cls);
				if (supp == null)
					throw new IOException("Class " + name + " does not have a default constructor or a provider");
				ConstructObject x = () -> (IsSerializable) supp.get();
				classSupps.add(x);
				return x;

			} catch (ClassNotFoundException e) {
				throw new IOException("Unknown class: " + name);
			}
		} else {
			return classSupps.get(id - 1);
		}

	}

	public List<String> readStringList() throws IOException {
		int cnt = readInt();
		List<String> lst = new ArrayList<>();
		while (cnt-- > 0)
			lst.add(readString());
		return lst;
	}

	public void readVec3(Vector3 out) throws IOException {
		out.x = readFloat();
		out.y = readFloat();
		out.z = readFloat();
	}

	public void setObjectConstructors(ObjectConstructors x) {
		this.xtors = x;
	}

	public Vector2i readVec2i(Vector2i out) throws IOException {
		out.x = readInt();
		out.y = readInt();
		return out;
	}

	public boolean readBoolean() throws IOException {
		return readByte() == 1;
	}

	public void readVec2(Vector2 v) throws IOException {
		v.x = readFloat();
		v.y = readFloat();
	}

	public <T extends IsSerializable> List<T> readList(Class<T> itemtyp, List<T> result) throws IOException {
		int i = readInt();
		while (i-- > 0)
			result.add(readObject(itemtyp));
		return result;
	}

}
