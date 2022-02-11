package uk.co.stikman.sett.astar;

public class MutableBoolean {
	public boolean value;

	public MutableBoolean() {

	}

	public MutableBoolean(boolean b) {
		this.value = b;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MutableBoolean [value=" + value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableBoolean other = (MutableBoolean) obj;
		if (value != other.value)
			return false;
		return true;
	}


}
