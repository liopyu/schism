package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.ModelManager;
import com.lycanitesmobs.client.model.EquipmentModel;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRenderer extends ItemStackTileEntityRenderer implements IItemModelRenderer {

	@Override
	public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int brightness, int unknown) {
		if(!(itemStack.getItem() instanceof ItemEquipment)) {
			return;
		}

		Hand hand = null;

		// Position:
		matrixStack.pushPose();
		matrixStack.translate(0.5F, 0.35F, 0.6F); // translate
		matrixStack.mulPose(new Vector3f(1.0F, 0.0F, 0.0F).rotationDegrees(90)); // rotate
		matrixStack.mulPose(new Vector3f(0.0F, 0.0F, 1.0F).rotationDegrees(-100)); // rotate
		matrixStack.translate(0F, -1.5F, 0F);
		EquipmentModel equipmentModel = ModelManager.getInstance().getEquipmentModel();

		float loop = 0;
		if(Minecraft.getInstance().player != null) {
			loop = Minecraft.getInstance().player.tickCount;
		}
		equipmentModel.render(itemStack, hand, matrixStack, renderTypeBuffer, this, loop, brightness);

		matrixStack.popPose();

		// Mana Bar:
		ItemEquipment itemEquipment = (ItemEquipment)itemStack.getItem();
		double manaNormal = (double)itemEquipment.getMana(itemStack) / ItemEquipment.MANA_MAX;
		if (manaNormal < 1) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.disableAlphaTest();
			RenderSystem.disableBlend();
			IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(RenderType.lines());
			double barX = -0.375D;
			double barWidth = 0.825D;
			double barFill = barWidth * manaNormal;

			for (int i = 0; i <= 2; i++) {
				vertexBuilder.vertex(barX, -0.25D + (i * 0.05D), 1).color(0, 0, 0, 255).endVertex();
				vertexBuilder.vertex(barX + barWidth, -0.25D + (i * 0.05D), 1).color(0, 0, 0, 255).endVertex();
			}
			vertexBuilder.vertex(barX, -0.2D, 1).color(64, 0, 128, 255).endVertex();
			vertexBuilder.vertex(barX + barFill, -0.2D, 1).color(0, 128, 255, 255).endVertex();

			RenderSystem.enableBlend();
			RenderSystem.enableAlphaTest();
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}
	}

	protected void drawBar(IVertexBuilder vertexBuilder, int x, int y, int width, int height, int r, int g, int b, int a) {
		double z = 1D;
		vertexBuilder.vertex(x + 0, (y + 0), z).color(r, g, b, a).endVertex();
		vertexBuilder.vertex((x + 0), (y + height), z).color(r, g, b, a).endVertex();
		vertexBuilder.vertex((x + width), (y + height), z).color(r, g, b, a).endVertex();
		vertexBuilder.vertex((x + width), (y + 0), z).color(r, g, b, a).endVertex();
	}

	@Override
	public void bindItemTexture(ResourceLocation location) {
		if(location == null) {
			return;
		}
		Minecraft.getInstance().getTextureManager().bind(location);
	}

	@Override
	public List<LayerItem> addLayer(LayerItem renderLayer) {
		return new ArrayList<>();
	}
}
