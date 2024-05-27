package com.schism.core.client.renderers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.schism.core.Schism;
import com.schism.core.client.models.TextureLayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RenderTypes extends RenderStateShard
{
    public enum Blending {
        NORM(RenderStateShard.TRANSLUCENT_TRANSPARENCY),
        ADD(ADDITIVE_TRANSPARENCY),
        SUB(SUBTRACTIVE_TRANSPARENCY);
        public final RenderStateShard.TransparencyStateShard state;
        Blending(RenderStateShard.TransparencyStateShard state) { this.state = state; }
    }

    protected static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

    protected static final RenderStateShard.TransparencyStateShard SUBTRACTIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

    protected static final VertexFormat MODEL_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("Texture", DefaultVertexFormat.ELEMENT_UV0)
            .put("Light", DefaultVertexFormat.ELEMENT_UV1)
            .put("Fade", DefaultVertexFormat.ELEMENT_UV2)
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .put("Padding", DefaultVertexFormat.ELEMENT_PADDING)
            .build()
    );

    protected static final VertexFormat SPRITE_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("Texture", DefaultVertexFormat.ELEMENT_UV0)
            .put("Normal", DefaultVertexFormat.ELEMENT_NORMAL)
            .put("Padding", DefaultVertexFormat.ELEMENT_PADDING)
            .build()
    );

    /**
     * Creates a render type for rendering a model.
     * @param layers A list of texture layers to render.
     * @param blending The blending mode to use.
     * @return A model render type.
     */
    public static RenderType modelRenderType(List<TextureLayer> layers, String blending)
    {
        TransparencyStateShard transparencyState = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        if (blending.equals("additive")) {
            transparencyState = ADDITIVE_TRANSPARENCY;
        } else if (blending.equals("subtractive")) {
            transparencyState = SUBTRACTIVE_TRANSPARENCY;
        }
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new LayeredTextureStateShard(layers.stream().filter(layer -> layer.blending().equals(blending)).toList()))
                .setTransparencyState(transparencyState)
                .createCompositeState(true);
        return RenderType.create(Schism.NAMESPACE + "_sprite", MODEL_FORMAT, VertexFormat.Mode.TRIANGLES, 256, true, false, compositeState);
    }

    /**
     * Creates a render type for rendering a single texture sprite.
     * @param texture The texture to render.
     * @param blending The alpha blending to use.
     * @return A simple sprite render type.
     */
    public static RenderType spriteRenderType(ResourceLocation texture, Blending blending)
    {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, true)) // Blur, Mipmap
                .setTransparencyState(blending.state)
                .createCompositeState(true);
        return RenderType.create(Schism.NAMESPACE + "_sprite", SPRITE_FORMAT, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
    }

    public RenderTypes(String name, Runnable setupState, Runnable clearState)
    {
        super(name, setupState, clearState);
    }
}
