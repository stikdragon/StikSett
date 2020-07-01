package uk.co.stikman.sett.gfx;

public interface Buffer {

	void delete();

	int getId();

	void bind(int target);

	void unbind();

}