package com.lycanitesmobs.client.obj;

import com.lycanitesmobs.client.renderer.CustomRenderStates;
import com.lycanitesmobs.client.renderer.VBOBatcher;
import com.lycanitesmobs.client.renderer.VBOBatcher.VBODrawCommand;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;

public class VBOObjModel extends ObjModel {

	public static RenderType renderType;
	public static ResourceLocation tex;
	public static boolean renderNormal;
	public static boolean renderOutline;

	public VBOObjModel(ResourceLocation resourceLocation) {
		super(resourceLocation);
	}

	@Override
	public void renderPart(IVertexBuilder vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f, int brightness, int fade, ObjPart objPart, Vector4f color, Vector2f textureOffset) {
		if (vertexBuilder != null) {
			super.renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
			return;
		}

		if (renderNormal) {
			VBOBatcher.getInstance().queue(renderType, 
					new VBODrawCommand(objPart.mesh.getVbo(), objPart.mesh.indices.length, renderType.format(), matrix4f, tex)
							.setColor(color)
							.setTextureOffset(textureOffset)
							.setOverlayOffset(0.0F, 10.0F - fade)
							.setLightOffset(brightness));
		}
		if (renderOutline) {
			VBOBatcher.getInstance().queue(CustomRenderStates.OBJ_OUTLINE_RENDER_TYPE, 
					new VBODrawCommand(objPart.mesh.getVbo(), objPart.mesh.indices.length, renderType.format(), matrix4f, tex));
		}
	}

}
