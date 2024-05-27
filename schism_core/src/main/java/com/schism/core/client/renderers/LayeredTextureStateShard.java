package com.schism.core.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.schism.core.client.models.TextureLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.TextureManager;

import java.util.List;

public class LayeredTextureStateShard extends RenderStateShard.TextureStateShard.EmptyTextureStateShard
{
    public LayeredTextureStateShard(List<TextureLayer> layers)
    {
        super(() -> {
            RenderSystem.enableTexture();
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            layers.forEach(layer -> {
                texturemanager.getTexture(layer.texture()).setFilter(layer.blur(), layer.mipmap());
                RenderSystem.setShaderTexture(layer.index(), layer.texture());
            });
        }, () -> {
            // No clear function.
        });
    }
}
