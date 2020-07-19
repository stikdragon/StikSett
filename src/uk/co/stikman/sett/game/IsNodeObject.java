package uk.co.stikman.sett.game;

/**
 * a NodeObject exists at a node in the terrain, like a house or a tree
 * 
 * @author stikd
 *
 */
public interface IsNodeObject extends IsSerializable {

	String getModelName();

	ObstructionType getObstructionType();

}
