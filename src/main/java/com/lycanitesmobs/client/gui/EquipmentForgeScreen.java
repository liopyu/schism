package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.EquipmentForgeSlot;
import com.lycanitesmobs.core.network.MessageTileEntityButton;
import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class EquipmentForgeScreen extends BaseContainerScreen<EquipmentForgeContainer> {
	public TileEntityEquipmentForge equipmentForge;
	public String currentMode = "empty";
	public boolean confirmation = false;

	public EquipmentForgeScreen(EquipmentForgeContainer container, PlayerInventory playerInventory, ITextComponent name) {
		super(container, playerInventory, name);
		this.equipmentForge = container.equipmentForge;
	}

	@Override
	public void init() {
		super.init();
		this.imageWidth = 176;
        this.imageHeight = 166;
	}

	@Override
	protected void initWidgets() {
		int backX = (this.width - this.imageWidth) / 2;
		int backY = (this.height - this.imageHeight) / 2;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int buttonSpacing = 2;
		int buttonWidth = 128;
		int buttonHeight = 20;
		int buttonX = backX + this.imageWidth;
		int buttonY = backY;

		String buttonText = "";
		if("construct".equals(this.currentMode)) {
			buttonText = new TranslationTextComponent("gui.equipmentforge.forge").getString();
		}
		else if("deconstruct".equals(this.currentMode)) {
			buttonText = new TranslationTextComponent("gui.equipmentforge.deconstruct").getString();
		}
		buttonY += buttonSpacing;
		//this.addButton(new ButtonBase(1, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, buttonText, this));
	}

	@Override
	protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawHelper.getMinecraft().getTextureManager().bind(TextureManager.getTexture("GUIEquipmentForge"));
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

			if(forgeSlot instanceof EquipmentForgeSlot) {
				EquipmentForgeSlot equipmentSlot =(EquipmentForgeSlot)forgeSlot;
				if("base".equals(equipmentSlot.type)) {
					slotV += slotHeight;
				}
				else if("head".equals(equipmentSlot.type)) {
					slotV += slotHeight * 2;
				}
				else if("blade".equals(equipmentSlot.type)) {
					slotV += slotHeight * 3;
				}
				else if("axe".equals(equipmentSlot.type)) {
					slotV += slotHeight * 4;
				}
				else if("pike".equals(equipmentSlot.type)) {
					slotV += slotHeight * 5;
				}
				else if("pommel".equals(equipmentSlot.type)) {
					slotV += slotHeight * 6;
				}
				else if("jewel".equals(equipmentSlot.type)) {
					slotV += slotHeight * 7;
				}
				else if("aura".equals(equipmentSlot.type)) {
					slotV += slotHeight * 8;
				}
			}

			this.drawHelper.drawTexturedModalRect(matrixStack, slotX, slotY, slotU, slotV, slotWidth, slotHeight);
		}
	}

	@Override
	protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.drawHelper.drawString(matrixStack, this.inventory.getName().getString(), this.leftPos + 8, this.topPos + this.imageHeight - 96 + 2, 4210752);
    }
    
	@Override
	public void actionPerformed(int buttonid) {
		MessageTileEntityButton message = new MessageTileEntityButton(buttonid, this.equipmentForge.getBlockPos());
		LycanitesMobs.packetHandler.sendToServer(message);
	}
}
