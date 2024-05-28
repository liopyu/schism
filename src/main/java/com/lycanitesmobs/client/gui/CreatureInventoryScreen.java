package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.container.BaseContainer;
import com.lycanitesmobs.core.container.CreatureContainer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.inventory.CreatureInventory;
import com.lycanitesmobs.core.network.MessageEntityGUICommand;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class CreatureInventoryScreen extends BaseContainerScreen<CreatureContainer> {
	public BaseCreatureEntity creature;
	public CreatureInventory creatureInventory;

	/**
	 * Constructor
	 */
	public CreatureInventoryScreen(CreatureContainer container, PlayerInventory playerInventory, ITextComponent name) {
		super(container, playerInventory, name);
		this.creature = container.creature;
		this.creatureInventory = container.creature.inventory;
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

		if(!(this.creature instanceof TameableCreatureEntity))
			return;
		TameableCreatureEntity pet = (TameableCreatureEntity)this.creature;
		if(!pet.petControlsEnabled())
			return;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int buttonSpacing = 2;
		int buttonWidth = 128;
		int buttonHeight = 20;
		int buttonX = backX + this.imageWidth;
		int buttonY = backY;

		String buttonText = new TranslationTextComponent("gui.pet.follow").getString();
		buttonY += buttonSpacing;
		Button button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(pet.isFollowing() && !pet.isSitting()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.wander").getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.WANDER.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(!pet.isFollowing() && !pet.isSitting()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.sit").getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.SIT.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(!pet.isFollowing() && pet.isSitting()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.passive").getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(pet.isPassive()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.defensive").getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(!pet.isPassive() && !pet.isAssisting() && !pet.isAggressive()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.assist").getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(!pet.isPassive() && pet.isAssisting() && !pet.isAggressive()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.aggressive").getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this);
		if(!pet.isPassive() && pet.isAggressive()) {
			button.active = false;
		}
		this.addButton(button);

		buttonText = new TranslationTextComponent("gui.pet.pvp").append(": ").append(pet.isPVP() ? new TranslationTextComponent("common.yes") : new TranslationTextComponent("common.no")).getString();
		buttonY += buttonHeight + (buttonSpacing * 2);
		this.addButton(new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PVP.id, buttonX + buttonSpacing, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), this));
	}

	@Override
	protected void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.creature == null || this.creature.isDeadOrDying()) {
			this.onClose();
		}

        this.drawHelper.drawString(matrixStack, this.inventory.getName().getString(), this.leftPos + 8, this.topPos + this.imageHeight - 96 + 2, 4210752);
		int backX = (this.width - this.imageWidth) / 2;
		int backY = (this.height - this.imageHeight) / 2;
		this.drawBars(matrixStack, backX, backY);
    }

	protected void drawBars(MatrixStack matrixStack, int backX, int backY) {
		int barWidth = 100;
		int barHeight = 11;
		int barX = backX - barWidth;
		int barY = backY + 54 + 18;
		int barCenter = barX + (barWidth / 2);

		// Health Bar:
		this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
		float healthNormal = Math.min(1, this.creature.getHealth() / this.creature.getMaxHealth());
		this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarHealth"), barX, barY, 0, healthNormal, 1, barWidth * healthNormal, barHeight);
		String healthText = new TranslationTextComponent("entity.health").getString() + ": " + String.format("%.0f", this.creature.getHealth()) + "/" + String.format("%.0f", this.creature.getMaxHealth());
		this.drawHelper.getFontRenderer().draw(matrixStack, healthText, barCenter - ((float)this.drawHelper.getStringWidth(healthText) / 2), barY + 2, 0xFFFFFF);
		barY += barHeight + 1;

		// XP Bar:
		this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
		float experienceNormal = Math.min(1, (float)this.creature.getExperience() / this.creature.creatureStats.getExperienceForNextLevel());
		this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIBarExperience"), barX, barY, 0, experienceNormal, 1, barWidth * experienceNormal, barHeight);
		String experienceText = new TranslationTextComponent("entity.experience").getString() + ": " + this.creature.getExperience() + "/" + this.creature.creatureStats.getExperienceForNextLevel();
		this.drawHelper.getFontRenderer().draw(matrixStack, experienceText, barCenter - ((float)this.drawHelper.getStringWidth(experienceText) / 2), barY + 2, 0xFFFFFF);
		barY += barHeight + 1;

		// Level:
		String levelText = new TranslationTextComponent("entity.level").getString() + ": " + this.creature.getMobLevel();
		this.drawHelper.getFontRenderer().draw(matrixStack, levelText, barCenter - ((float)this.drawHelper.getStringWidth(levelText) / 2), barY + 2, 0xFFFFFF);
	}

	@Override
	protected void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bind(TextureManager.getTexture("GUIInventoryCreature"));
        this.imageWidth = 176;
        this.imageHeight = 166;
        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;
        this.drawHelper.drawTexturedModalRect(matrixStack, backX, backY, 0, 0, this.imageWidth, this.imageHeight);

		this.drawFrames(matrixStack, backX, backY, mouseX, mouseY);
		this.drawSlots(matrixStack, backX, backY);
	}

	protected void drawFrames(MatrixStack matrixStack, int backX, int backY, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bind(TextureManager.getTexture("GUIInventoryCreature"));
        
        // Status Frame:
        int statusWidth = 90;
        int statusHeight = 54;
        this.drawHelper.drawTexturedModalRect(matrixStack, backX + 79, backY + 17, 0, 256 - statusHeight, statusWidth, statusHeight);
        
        // Creature Frame:
        int creatureFrameWidth = 54;
        int creatureFrameHeight = 54;
        this.drawHelper.drawTexturedModalRect(matrixStack, backX - creatureFrameWidth + 1, backY + 17, statusWidth, 256 - creatureFrameHeight, creatureFrameWidth, creatureFrameHeight);
		double creatureWidth = creature.getBbWidth();
		double creatureHeight = creature.getBbHeight();
		int scale = (int)Math.round((2.5F / Math.max(creatureWidth, creatureHeight)) * 6);
		BaseGui.renderLivingEntity(matrixStack, backX + 26 - creatureFrameWidth + 1, backY + 60, scale, (float) backX - mouseX, (float) backY - mouseY, this.creature);
	}

	protected void drawSlots(MatrixStack matrixStack, int backX, int backY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bind(TextureManager.getTexture("GUIInventoryCreature"));
        
		BaseContainer container = this.getMenu();
		List<Slot> creatureSlots = container.slots.subList(container.specialStart, container.inventoryFinish + 1);
		int slotWidth = 18;
		int slotHeight = 18;
		int slotU = 238;
		int slotVBase = 0;
		for(Slot creatureSlot : creatureSlots) {
			int slotX = backX + creatureSlot.x - 1;
			int slotY = backY + creatureSlot.y - 1;
			int slotV = slotVBase;
			String slotType = creatureInventory.getTypeFromSlot(creatureSlot.getSlotIndex());
			if(slotType != null) {
				if(slotType.equals("saddle"))
					slotV += slotHeight;
				else if(slotType.equals("bag"))
					slotV += slotHeight * 2;
				else if(slotType.equals("chest"))
					slotV += slotHeight * 3;
			}
			this.drawHelper.drawTexturedModalRect(matrixStack, slotX, slotY, slotU, slotV, slotWidth, slotHeight);
		}
	}

	@Override
	public void actionPerformed(int buttonId) {
		MessageEntityGUICommand message = new MessageEntityGUICommand(buttonId, this.creature);
		LycanitesMobs.packetHandler.sendToServer(message);
	}
}
