package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.EquipmentInfuserChargeSlot;
import com.lycanitesmobs.core.container.EquipmentInfuserContainer;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.network.MessageTileEntityButton;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class EquipmentInfuserScreen extends BaseContainerScreen<EquipmentInfuserContainer> {
	public EquipmentInfuserTileEntity equipmentInfuser;

	public EquipmentInfuserScreen(EquipmentInfuserContainer container, PlayerInventory playerInventory, ITextComponent name) {
		super(container, playerInventory, name);
		this.equipmentInfuser = container.equipmentInfuser;
	}

	@Override
	public void init() {
		super.init();
		this.imageWidth = 176;
        this.imageHeight = 166;
	}

	@Override
	protected void initWidgets() {

	}

	@Override
	protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.getMinecraft().getTextureManager().bind(TextureManager.getTexture("GUIEquipmentForge"));
		this.imageWidth = 176;
		this.imageHeight = 166;
		int backX = (this.width - this.imageWidth) / 2;
		int backY = (this.height - this.imageHeight) / 2;
		this.drawHelper.drawTexturedModalRect(matrixStack, backX, backY, 0, 0, this.imageWidth, this.imageHeight);

		this.drawSlots(matrixStack, backX, backY);
	}

	/**
	 * Draws each Equipment Slot.
	 * @param backX
	 * @param backY
	 */
	protected void drawSlots(MatrixStack matrixStack, int backX, int backY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.getMinecraft().getTextureManager().bind(TextureManager.getTexture("GUIEquipmentForge"));

		BaseContainer container = this.getMenu();
		List<Slot> forgeSlots = container.slots.subList(container.inventoryStart, container.inventoryFinish);
		int slotWidth = 18;
		int slotHeight = 18;
		int slotU = 238;
		int slotVBase = 0;
		for(Slot forgeSlot : forgeSlots) {
			int slotX = backX + forgeSlot.x - 1;
			int slotY = backY + forgeSlot.y - 1;
			int slotV = slotVBase;

			if(forgeSlot instanceof EquipmentInfuserChargeSlot) {
				slotV += slotHeight * 9;
			}

			this.drawHelper.drawTexturedModalRect(matrixStack, slotX, slotY, slotU, slotV, slotWidth, slotHeight);
		}
	}

	@Override
	protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.drawHelper.drawString(matrixStack, this.inventory.getName().getString(), this.leftPos + 8, this.topPos + this.imageHeight - 96 + 2, 4210752);
		int backX = (this.width - this.imageWidth) / 2;
		int backY = (this.height - this.imageHeight) / 2;
		this.drawBars(matrixStack, backX, backY);
    }

	protected void drawBars(MatrixStack matrixStack, int backX, int backY) {
		int barWidth = 100;
		int barHeight = 11;
		int barX = (this.width / 2) - (barWidth / 2);
		int barY = backY + 58;
		int barCenter = barX + (barWidth / 2);
		this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
		ItemStack partStack = this.equipmentInfuser.getItem(1);
		if(!(partStack.getItem() instanceof ItemEquipmentPart)) {
			return;
		}
		ItemEquipmentPart partItem = (ItemEquipmentPart)partStack.getItem();
		if(partItem.getPartLevel(partStack) >= partItem.levelMax) {
			return;
		}
		int experience = partItem.getExperience(partStack);
		int experienceMax = partItem.getExperienceForNextLevel(partStack);
		float experienceNormal = (float)experience / experienceMax;
		this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIBarExperience"), barX, barY, 0, experienceNormal, 1, barWidth * experienceNormal, barHeight);
		String experienceText = new TranslationTextComponent("entity.experience").getString() + ": " + experience + "/" + experienceMax;
		this.drawHelper.drawString(matrixStack, experienceText, barCenter - (this.drawHelper.getStringWidth(experienceText) / 2), barY + 2, 0xFFFFFF);
	}
    
	@Override
	public void actionPerformed(int buttonid) {
		MessageTileEntityButton message = new MessageTileEntityButton(buttonid, this.equipmentInfuser.getBlockPos());
		LycanitesMobs.packetHandler.sendToServer(message);
	}
}
