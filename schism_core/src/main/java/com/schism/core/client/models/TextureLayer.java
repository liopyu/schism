package com.schism.core.client.models;

import com.schism.core.database.DataStore;
import net.minecraft.resources.ResourceLocation;

public class TextureLayer
{
    protected final int index;
    protected final ResourceLocation texture;
    protected final String blending;
    protected final boolean blur;
    protected final boolean mipmap;
    protected final boolean glow;
    protected final String animationName;

    /**
     * A model layer that holds information about a texture layer for rendering.
     * @param dataStore The data store to read from.
     */
    public TextureLayer(DataStore dataStore, ResourceLocation textureBase)
    {
        this.index = dataStore.intProp("index");
        this.texture = new ResourceLocation(textureBase.toString() + dataStore.stringProp("texture_suffix") + ".png");
        this.blending = dataStore.stringProp("blending");
        this.blur = dataStore.booleanProp("blur");
        this.mipmap = dataStore.booleanProp("mipmap");
        this.glow = dataStore.booleanProp("glow");
        this.animationName = dataStore.stringProp("animation_name");
    }

    /**
     * The render index of this layer.
     * @return The layer index.
     */
    public int index()
    {
        return this.index;
    }

    /**
     * The texture for this layer.
     * @return The layer texture.
     */
    public ResourceLocation texture()
    {
        return this.texture;
    }

    /**
     * The blending mode to use.
     * @return The blending mode this layer should use.
     */
    public String blending()
    {
        return this.blending;
    }

    /**
     * Whether this layer should use linear blurring when upscaling.
     * @return True for linear blurring.
     */
    public boolean blur()
    {
        return this.blur;
    }

    /**
     * Whether this texture should mipmap.
     * @return True for mipmapping.
     */
    public boolean mipmap()
    {
        return this.mipmap;
    }

    /**
     * Whether this texture should ignore light level.
     * @return True to ignore light level.
     */
    public boolean glow()
    {
        return this.glow;
    }

    /**
     * A name to animate this layer by, can be shared with other layers.
     * @return The animation name to match this layer with.
     */
    public String animationName()
    {
        return this.animationName;
    }
}
