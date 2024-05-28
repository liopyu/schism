package com.lycanitesmobs.client.gui.beastiary;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureList;
import com.lycanitesmobs.client.gui.beastiary.lists.SubspeciesList;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.client.gui.buttons.CreatureButton;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.network.MessageSummonSetSelection;
import com.lycanitesmobs.core.pets.SummonSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SummoningBeastiaryScreen extends BeastiaryScreen {
	public CreatureList petList;
	public SubspeciesList subspeciesList;

	private int summoningSlotIdStart = 200;
	private int petCommandIdStart = 300;

	public SummoningBeastiaryScreen(PlayerEntity player) {
		super(player);
	}

	@Override
	public void initWidgets() {
		super.initWidgets();

		int petListHeight = this.colLeftHeight;
		int petListY = this.colLeftY;
		this.petList = new CreatureList(CreatureList.Type.SUMMONABLE, this, null, this.colLeftWidth, petListHeight, petListY, petListY + petListHeight, this.colLeftX);
		this.children.add(this.petList);

		int subspeciesListHeight = 80;
		int subspeciesListY = this.colRightY + 70;
		this.subspeciesList = new SubspeciesList(this, true, 90, subspeciesListHeight, subspeciesListY, subspeciesListY + subspeciesListHeight, this.colRightX);
		this.children.add(this.subspeciesList);

		int summoningSlots = this.playerExt.summonSetMax;
		int buttonSpacing = 2;
		int buttonWidth = 32;
		int buttonHeight = 32;
		int buttonX = this.colRightX + Math.round((float)this.colRightWidth / 2) - Math.round((buttonWidth + buttonSpacing) * ((float)summoningSlots / 2));
		int buttonY = this.colRightY + 10;

		// Summoning Slots:
		int tabSpacing = buttonSpacing;
		for(int i = 1; i <= summoningSlots; i++) {
			String buttonText = String.valueOf(i);
			CreatureInfo creatureInfo = this.playerExt.getSummonSet(i).getCreatureInfo();
			buttonX += tabSpacing;
			ButtonBase tabButton = new CreatureButton(this.summoningSlotIdStart + i, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent(buttonText), i, creatureInfo, this);
			this.addButton(tabButton);
			if(i == this.playerExt.selectedSummonSet) {
				tabButton.active = false;
			}
			tabSpacing = buttonWidth + buttonSpacing;
		}

		int buttonMarginX = 10 + Math.max(Math.max(this.drawHelper.getStringWidth(new TranslationTextComponent("gui.pet.actions").getString()), this.drawHelper.getStringWidth(new TranslationTextComponent("gui.pet.stance").getString())), this.drawHelper.getStringWidth(new TranslationTextComponent("gui.pet.movement").getString()));
		buttonWidth = 80;
		buttonHeight = 20;
		buttonX = this.colRightX + buttonMarginX;
		buttonY = this.colRightY + this.colRightHeight - ((buttonHeight + buttonSpacing) * 3);

		// Actions:
		ButtonBase button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PVP.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.pvp"), this);
		this.addButton(button);

		// Stance:
		buttonX = this.colRightX + buttonMarginX;
		buttonY += buttonHeight + 2;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.passive"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.defensive"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.assist"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.aggressive"), this);
		this.addButton(button);

		// Movement:
		buttonX = this.colRightX + buttonMarginX;
		buttonY += buttonHeight + 2;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.follow"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.WANDER.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.wander"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.SIT.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.sit"), this);
		this.addButton(button);
	}

	@Override
	public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderWidgets(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderWidgets(matrixStack, mouseX, mouseY, partialTicks);

		if(this.playerExt.beastiary.getSummonableList().isEmpty()) {
			return;
		}

		this.petList.render(matrixStack, mouseX, mouseY, partialTicks);
		this.subspeciesList.render(matrixStack, mouseX, mouseY, partialTicks);

		// Update Buttons:
		for(Widget buttonWidget : this.buttons) {
			if(!(buttonWidget instanceof ButtonBase))
				continue;
			ButtonBase button = (ButtonBase)buttonWidget;

			// Summoning Slots:
			if(button.buttonId >= this.summoningSlotIdStart && button.buttonId < this.petCommandIdStart) {
				button.active = button.buttonId - this.summoningSlotIdStart != this.playerExt.selectedSummonSet;
				if(button instanceof CreatureButton) {
					CreatureButton buttonCreature = (CreatureButton)button;
					buttonCreature.creatureInfo = this.playerExt.getSummonSet(button.buttonId - this.summoningSlotIdStart).getCreatureInfo();
				}
			}

			// Pet Commands:
			else if(button.buttonId >= this.petCommandIdStart) {
				if (this.playerExt.getSelectedSummonSet() != null) {
					button.visible = true;

					// Actions:
					if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.PVP.id + this.petCommandIdStart) {
						if (this.playerExt.getSelectedSummonSet().getPVP()) {
							button.setMessage(new TranslationTextComponent(new TranslationTextComponent("gui.pet.pvp").getString() + ": " + new TranslationTextComponent("common.yes").getString()));
						}
						else {
							button.setMessage(new TranslationTextComponent(new TranslationTextComponent("gui.pet.pvp").getString() + ": " + new TranslationTextComponent("common.no").getString()));
						}
					}

					// Stance:
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id + this.petCommandIdStart) {
						button.active = !this.playerExt.getSelectedSummonSet().passive;
					}
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.getSelectedSummonSet().getPassive() && !this.playerExt.getSelectedSummonSet().getAssist() && !this.playerExt.getSelectedSummonSet().getAggressive());
					}
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.getSelectedSummonSet().getPassive() && this.playerExt.getSelectedSummonSet().getAssist() && !this.playerExt.getSelectedSummonSet().getAggressive());
					}
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.getSelectedSummonSet().getPassive() && this.playerExt.getSelectedSummonSet().getAggressive());
					}

					// Movement:
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.getSelectedSummonSet().getSitting() && this.playerExt.getSelectedSummonSet().getFollowing());
					}
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.WANDER.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.getSelectedSummonSet().getSitting() && !this.playerExt.getSelectedSummonSet().getFollowing());
					}
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.SIT.id + this.petCommandIdStart) {
						button.active = !(this.playerExt.getSelectedSummonSet().getSitting());
					}
				}
				else {
					button.visible = false;
				}
			}
		}
	}

	@Override
	public void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);

		int marginX = 0;
		int nextX = this.colRightX + marginX;
		int nextY = this.colRightY + 44;
		int width = this.colRightWidth - marginX;

		// Empty:
		if(this.playerExt.beastiary.getSummonableList().isEmpty()) {
			String text = new TranslationTextComponent("gui.beastiary.summoning.empty.info").getString();
			this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);
			return;
		}

		CreatureInfo selectedCreature = this.playerExt.getSelectedSummonSet().getCreatureInfo();

		// Model:
		if(selectedCreature != null) {
			this.renderCreature(matrixStack, selectedCreature, this.colRightX + (marginX / 2) + (this.colRightWidth / 2), this.colRightY + Math.round((float) this.colRightHeight / 2), mouseX, mouseY, partialTicks);
		}

		// Player Summoning Focus:
		String text = "\u00A7l" + new TranslationTextComponent("gui.beastiary.player.focus").getString() + ": ";
		this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF);
		int barX = nextX + this.drawHelper.getStringWidth(text);
		int focusMax = Math.round((float)this.playerExt.summonFocusMax / this.playerExt.summonFocusCharge);
		int focusAvailable = (int)Math.floor((double)this.playerExt.summonFocus / this.playerExt.summonFocusCharge);
		float focusFilling = ((float)this.playerExt.summonFocus / this.playerExt.summonFocusCharge) - focusAvailable;
		this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpiritEmpty"), barX, nextY, 0, 9, 9, focusMax, 10);
		this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpiritUsed"), barX, nextY, 0, 9, 9, focusAvailable, 10);
		if(focusFilling > 0) {
			this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetSpiritFilling"), barX + (9 * focusAvailable), nextY, 0, focusFilling, 1, focusFilling * 9, 9);
		}

		// Creature Display:
		if(selectedCreature != null) {
			// Focus Cost:
			nextY += 4 + this.drawHelper.getWordWrappedHeight(text, colRightWidth);
			text = "\u00A7l" + new TranslationTextComponent("creature.stat.focus").getString() + ": ";
			this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF);
			this.drawLevel(matrixStack, selectedCreature, TextureManager.getTexture("GUIPetLevel"), nextX + this.drawHelper.getStringWidth(text), nextY);
		}

		// Base Display:
		else {
			nextY += 4 + this.drawHelper.getWordWrappedHeight(text, colRightWidth);
			text = new TranslationTextComponent("gui.beastiary.summoning.select").getString();
			this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);
		}

		// Button Titles:
		int buttonHeight = 20;
		int buttonSpacing = 2;
		int buttonY = this.colRightY + this.colRightHeight - ((buttonHeight + buttonSpacing) * 3);
		this.drawHelper.drawString(matrixStack, "\u00A7l" + new TranslationTextComponent("gui.pet.actions").getString(), this.colRightX, buttonY + 6, 0xFFFFFF);
		buttonY += buttonHeight + buttonSpacing;
		this.drawHelper.drawString(matrixStack, "\u00A7l" + new TranslationTextComponent("gui.pet.stance").getString(), this.colRightX, buttonY + 6, 0xFFFFFF);
		buttonY += buttonHeight + buttonSpacing;
		this.drawHelper.drawString(matrixStack, "\u00A7l" + new TranslationTextComponent("gui.pet.movement").getString(), this.colRightX, buttonY + 6, 0xFFFFFF);
	}

	@Override
	public void actionPerformed(int buttonId) {
		// Summoning Slots:
		if(buttonId >= this.summoningSlotIdStart && buttonId < this.petCommandIdStart) {
			this.playerExt.setSelectedSummonSet(buttonId - this.summoningSlotIdStart);
			MessageSummonSetSelection message = new MessageSummonSetSelection(this.playerExt);
			LycanitesMobs.packetHandler.sendToServer(message);
		}

		SummonSet summonSet = this.playerExt.getSelectedSummonSet();
		if(summonSet != null) {

			// Pet Commands:
			if (buttonId >= this.petCommandIdStart) {
				int petCommandId = buttonId - this.petCommandIdStart;

				// Actions:
				if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.PVP.id) {
					summonSet.pvp = !summonSet.pvp;
				}

				// Stance:
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id) {
					summonSet.passive = true;
					summonSet.assist = false;
					summonSet.aggressive = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id) {
					summonSet.passive = false;
					summonSet.assist = false;
					summonSet.aggressive = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id) {
					summonSet.passive = false;
					summonSet.assist = true;
					summonSet.aggressive = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id) {
					summonSet.passive = false;
					summonSet.assist = true;
					summonSet.aggressive = true;
				}

				// Movement:
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id) {
					summonSet.following = true;
					summonSet.sitting = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.WANDER.id) {
					summonSet.following = false;
					summonSet.sitting = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.SIT.id) {
					summonSet.following = false;
					summonSet.sitting = true;
				}

				this.playerExt.sendSummonSetToServer((byte) this.playerExt.selectedSummonSet);
				if (this.playerExt.selectedPet == null) {
					this.mc.setScreen(new SummoningBeastiaryScreen(this.mc.player));
				}
				return;
			}
		}

		super.actionPerformed(buttonId);
	}

	@Override
	public ITextComponent getTitle() {
		if(this.playerExt.beastiary.getSummonableList().isEmpty()) {
			return new TranslationTextComponent("gui.beastiary.summoning.empty.title");
		}
		return new TranslationTextComponent("gui.beastiary.summoning");
	}

	@Override
	public int getDisplaySubspecies(CreatureInfo creatureInfo) {
		return this.playerExt.getSelectedSummonSet().subspecies;
	}


	@Override
	public int getDisplayVariant(CreatureInfo creatureInfo) {
		return this.playerExt.getSelectedSummonSet().variant;
	}

	@Override
	public void playCreatureSelectSound(CreatureInfo creatureInfo) {
		this.player.getCommandSenderWorld().playSound(this.player, this.player.position().x(), this.player.position().y(), this.player.position().z(), ObjectManager.getSound(creatureInfo.getName() + "_tame"), SoundCategory.NEUTRAL, 1, 1);
	}
}
