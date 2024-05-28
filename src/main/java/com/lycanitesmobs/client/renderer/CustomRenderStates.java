package com.lycanitesmobs.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class CustomRenderStates extends RenderState {
	public static final Vector4f WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);

	public static VertexFormat POS_COL_TEX_LIGHT_FADE_NORMAL;
	public static VertexFormat POS_COL_TEX_NORMAL;
	public static final VertexFormat POS_TEX_NORMAL = new VertexFormat(ImmutableList.of(
			DefaultVertexFormats.ELEMENT_POSITION,
			DefaultVertexFormats.ELEMENT_UV0,
			DefaultVertexFormats.ELEMENT_NORMAL,
			DefaultVertexFormats.ELEMENT_PADDING));

	public enum BLEND {
		NORMAL(0), ADD(1), SUB(2);
		public final int id;
		BLEND(int value) { this.id = value; }
		public int getValue() { return id; }
	}

	protected static final RenderState.TransparencyState ADDITIVE_TRANSPARENCY = new RenderState.TransparencyState("lm_additive_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});

	protected static final RenderState.TransparencyState SUBTRACTIVE_TRANSPARENCY = new RenderState.TransparencyState("lm_subtractive_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});

	private static final RenderType[] OBJ_RENDER_TYPES = new RenderType[BLEND.values().length * 2];
	static {
		for (BLEND blend : BLEND.values()) {
			for (int glow = 0; glow < 2; glow++) {
				OBJ_RENDER_TYPES[blend.id * 2 + glow] = RenderType.create("lm_obj_" + blend.toString() + (glow == 1 ? "_glow" : ""), POS_TEX_NORMAL, GL11.GL_TRIANGLES, 256, true, false, RenderType.State.builder()
						.setTransparencyState(blend == BLEND.ADD ? ADDITIVE_TRANSPARENCY : (blend == BLEND.SUB ? SUBTRACTIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY))
						.setDiffuseLightingState(glow == 1 ? NO_DIFFUSE_LIGHTING : DIFFUSE_LIGHTING)
						.setAlphaState(DEFAULT_ALPHA)
						.setCullState(NO_CULL)
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(false));
			}
		}
	}
	public static final RenderType OBJ_OUTLINE_RENDER_TYPE = RenderType.create("lm_obj_outline_no_cull", POS_TEX_NORMAL, GL11.GL_TRIANGLES, 256, true, false, RenderType.State.builder()
			.setAlphaState(DEFAULT_ALPHA)
			.setDepthTestState(NO_DEPTH_TEST)
			.setCullState(NO_CULL)
			.setFogState(NO_FOG)
			.setOutputState(OUTLINE_TARGET)
			.setTexturingState(OUTLINE_TEXTURING)
			.createCompositeState(false));

	public CustomRenderStates(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
		super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
	}

	public static RenderType getObjVBORenderType(int blending, boolean glow) {
		return OBJ_RENDER_TYPES[(blending << 1) | (glow ? 1 : 0)];
	}

	public static RenderType getObjRenderType(ResourceLocation texture, int blending, boolean glow) {
		if(POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
			List<VertexFormatElement> vertexFormatValues = new ArrayList<>();
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_POSITION);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_COLOR);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV0);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV1);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV2);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_NORMAL);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_PADDING);
			POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(ImmutableList.copyOf(vertexFormatValues));
		}

		RenderState.TransparencyState transparencyState = TRANSLUCENT_TRANSPARENCY;
		if(blending == BLEND.ADD.getValue()) {
			transparencyState = ADDITIVE_TRANSPARENCY;
		}
		else if(blending == BLEND.SUB.getValue()) {
			transparencyState = SUBTRACTIVE_TRANSPARENCY;
		}
		RenderState.DiffuseLightingState lightingState = DIFFUSE_LIGHTING;
		if(glow) {
			lightingState = NO_DIFFUSE_LIGHTING;
		}
		RenderType.State renderTypeState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(texture, false, false)) // Texture
				.setTransparencyState(transparencyState)
				.setDiffuseLightingState(lightingState)
				.setAlphaState(DEFAULT_ALPHA)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("lm_obj_translucent_no_cull", POS_COL_TEX_LIGHT_FADE_NORMAL, GL11.GL_TRIANGLES, 256, true, false, renderTypeState);
	}

	public static RenderType getObjColorOnlyRenderType(ResourceLocation texture, int blending, boolean glow) {
		if(POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
			List<VertexFormatElement> vertexFormatValues = new ArrayList<>();
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_POSITION);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_COLOR);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_NORMAL);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_PADDING);
			POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(ImmutableList.copyOf(vertexFormatValues));
		}

		RenderState.TransparencyState transparencyState = TRANSLUCENT_TRANSPARENCY;
		if(blending == BLEND.ADD.getValue()) {
			transparencyState = ADDITIVE_TRANSPARENCY;
		}
		else if(blending == BLEND.SUB.getValue()) {
			transparencyState = SUBTRACTIVE_TRANSPARENCY;
		}
		RenderState.DiffuseLightingState lightingState = DIFFUSE_LIGHTING;
		if(glow) {
			lightingState = NO_DIFFUSE_LIGHTING;
		}
		RenderType.State renderTypeState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(texture, false, false)) // Texture
				.setTransparencyState(transparencyState)
				.setDiffuseLightingState(lightingState)
				.setAlphaState(DEFAULT_ALPHA)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("lm_obj_translucent_no_cull", POS_COL_TEX_LIGHT_FADE_NORMAL, GL11.GL_TRIANGLES, 256, true, false, renderTypeState);
	}

	public static RenderType getObjOutlineRenderType(ResourceLocation texture) {
		if(POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
			List<VertexFormatElement> vertexFormatValues = new ArrayList<>();
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_POSITION);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_COLOR);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV0);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV1);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV2);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_NORMAL);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_PADDING);
			POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(ImmutableList.copyOf(vertexFormatValues));
		}

		RenderType.State renderTypeState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(texture, false, false))
				.setCullState(NO_CULL)
				.setDepthTestState(NO_DEPTH_TEST)
				.setAlphaState(DEFAULT_ALPHA)
				.setTexturingState(OUTLINE_TEXTURING)
				.setFogState(NO_FOG)
				.setOutputState(OUTLINE_TARGET)
				.createCompositeState(false);
		return RenderType.create("lm_obj_outline_no_cull", POS_COL_TEX_LIGHT_FADE_NORMAL, GL11.GL_TRIANGLES, 256, true, false, renderTypeState);
	}

	public static RenderType getSpriteRenderType(ResourceLocation texture) {
		if(POS_COL_TEX_NORMAL == null) {
			List<VertexFormatElement> vertexFormatValues = new ArrayList<>();
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_POSITION);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_COLOR);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_UV0);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_NORMAL);
			vertexFormatValues.add(DefaultVertexFormats.ELEMENT_PADDING);
			POS_COL_TEX_NORMAL = new VertexFormat(ImmutableList.copyOf(vertexFormatValues));
		}

		RenderType.State renderTypeState = RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(texture, false, false))
				.setAlphaState(DEFAULT_ALPHA)
				.createCompositeState(true);
		return RenderType.create("lm_sprite", POS_COL_TEX_NORMAL, 7, 256, true, false, renderTypeState);
	}
}
