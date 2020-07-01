package uk.co.stikman.sett.gfx;

import uk.co.stikman.utils.math.Vector2i;

/**
 * Used to represent a target for opengl rendering. Most commonly used for
 * rendering to a texture, rather than the video device. Is meant to return the
 * dimensions (width and height) of the target. Gets a couple of lifecycle
 * events, when the target is bound and unbound
 * 
 * @author stikd
 *
 */
public interface RenderTarget {

	Vector2i getDimensions(Vector2i out);

	void targetUnbound();

	void targetBound();
	void apply(Vector2i v);
}
