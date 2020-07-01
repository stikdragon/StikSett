package uk.co.stikman.sett.gfx.text;

import uk.co.stikman.sett.gfx.Texture;
import uk.co.stikman.sett.gfx.util.ResourceLoadError;

public interface TextureLoader {

	Texture loadTexture(String name) throws  ResourceLoadError;

}
