package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.ModelManager;
import com.lycanitesmobs.client.model.AnimationPart;
import com.lycanitesmobs.client.model.ItemObjModel;
import com.lycanitesmobs.client.obj.VBOObjModel;
import com.lycanitesmobs.client.renderer.layer.LayerItem;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class EquipmentPartRenderer extends ItemStackTileEntityRenderer implements IItemModelRenderer {
	protected List<LayerItem> renderLayers = new ArrayList<>();

	@Override
	public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int brightness, int unknown) {
		if(!(itemStack.getItem() instanceof ItemEquipmentPart)) {
			return;
		}

		Hand hand = null;

		ItemEquipmentPart itemEquipmentPart = (ItemEquipmentPart)itemStack.getItem();
		ItemObjModel itemObjModel = ModelManager.getInstance().getEquipmentPartModel(itemEquipmentPart);
		if(itemObjModel == null) {
			return;
		}
		this.renderLayers.clear();
		itemObjModel.addCustomLayers(this);

		float loop = 0;
		if(Minecraft.getInstance().player != null) {
			loop = Minecraft.getInstance().player.tickCount;
		}

		matrixStack.translate(0.6F, 0.35F, 0.5F); // translate
		matrixStack.mulPose(new Vector3f(1.0F, 0.0F, 0.0F).rotationDegrees(190));
		matrixStack.mulPose(new Vector3f(0.0F, 1.0F, 0.0F).rotationDegrees(-45));
		matrixStack.mulPose(new Vector3f(0.0F, 0.0F, 1.0F).rotationDegrees(10));
		matrixStack.translate(0F, -1.7F, 0F);

		matrixStack.pushPose();
		if("head".equalsIgnoreCase(itemEquipmentPart.slotType)) {
			matrixStack.translate(0F, 0F, 0.5F);
		}
		else if("blade".equalsIgnoreCase(itemEquipmentPart.slotType) || "pike".equalsIgnoreCase(itemEquipmentPart.slotType) || "axe".equalsIgnoreCase(itemEquipmentPart.slotType) || "jewel".equalsIgnoreCase(itemEquipmentPart.slotType)) {
			matrixStack.translate(0F, 0F, 1F);
		}

		itemObjModel.generateAnimationFrames(itemStack, null, loop, null);
		this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, null,null, loop, brightness);
		for(LayerItem renderLayer : this.renderLayers) {
			this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, renderLayer, null, loop, brightness);
		}
		//this.renderModel(itemObjModel, itemStack, hand, matrixStack, renderTypeBuffer, new LayerItemDye(this, "dye"),null, loop, brightness);
		itemObjModel.clearAnimationFrames();
		matrixStack.popPose();

		VBOBatcher.getInstance().endBatches();
	}

	/**
	 * Renders the main model.
	 * @param itemStack The item stack to render.
	 * @param hand The hand that is holding the item or null if in the inventory instead.
	 * @param matrixStack The matrix stack for animation.
	 * @param renderTypeBuffer  The render type buffer for rendering with.
	 * @param layer The layer to render, the base layer is null.
	 * @param offsetObjPart A ModelObjPart, if not null this model is offset by it, used by assembled equipment pieces to create a full model.
	 * @param loop A constant tick for looping animations.
	 * @param brightness The brightness of the mob based on block location, etc.
	 */
	protected void renderModel(ItemObjModel model, ItemStack itemStack, Hand hand, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, LayerItem layer, AnimationPart offsetObjPart, float loop, int brightness) {
		ResourceLocation texture = model.getTexture(itemStack, layer);
		RenderType renderType = CustomRenderStates.getObjVBORenderType(model.getBlending(itemStack, layer), model.getGlow(itemStack, layer));
//		if(layer instanceof LayerItemDye) {
//			renderType = CustomRenderStates.getObjColorOnlyRenderType(texture, model.getBlending(itemStack, layer), model.getGlow(itemStack, layer));
//		}

		VBOObjModel.renderType = renderType;
		VBOObjModel.renderNormal = true;
		VBOObjModel.tex = texture;
		model.render(itemStack, hand, matrixStack, null, this, offsetObjPart, layer, loop, brightness);
		VBOObjModel.tex = null;
		VBOObjModel.renderNormal = false;
		VBOObjModel.renderType = null;
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
		if(!this.renderLayers.contains(renderLayer)) {
			this.renderLayers.add(renderLayer);
		}
		return this.renderLayers;
	}
}
