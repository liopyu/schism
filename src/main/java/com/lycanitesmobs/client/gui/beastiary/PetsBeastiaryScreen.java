package com.lycanitesmobs.client.gui.beastiary;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureFilterList;
import com.lycanitesmobs.client.gui.beastiary.lists.CreatureList;
import com.lycanitesmobs.client.gui.beastiary.lists.PetTypeList;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.pets.PetEntry;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PetsBeastiaryScreen extends BeastiaryScreen {
	public CreatureFilterList petTypeList;
	public CreatureList petList;
	private int petCommandIdStart = 200;
	private int releaseConfirmId = 300;
	private int releaseCancelId = 301;

	public PetsBeastiaryScreen(PlayerEntity player) {
		super(player);
		this.playerExt.selectedSubspecies = 0;
		this.playerExt.selectedVariant = 0;
	}

	@Override
	public void initWidgets() {
		super.initWidgets();

		int petTypeListHeight = Math.round((float)this.colLeftHeight * 0.225F);
		int petTypeListY = this.colLeftY;
		this.petTypeList = new PetTypeList(this, this.colLeftWidth, petTypeListHeight, petTypeListY, petTypeListY + petTypeListHeight, this.colLeftX);
		this.children.add(this.petTypeList);

		int petListHeight = Math.round((float)this.colLeftHeight * 0.7F);
		int petListY = petTypeListY + petTypeListHeight + Math.round((float)this.colLeftHeight * 0.025F);
		this.petList = new CreatureList(CreatureList.Type.PET, this, this.petTypeList, this.colLeftWidth, petListHeight, petListY, petListY + petListHeight, this.colLeftX);
		this.children.add(this.petList);

		int buttonWidth = 80;
		int buttonHeight = 20;
		int buttonSpacing = 2;
		int buttonMarginX = 10 + Math.max(Math.max(this.drawHelper.getStringWidth(new TranslationTextComponent("gui.pet.actions").getString()), this.drawHelper.getStringWidth(new TranslationTextComponent("gui.pet.stance").getString())), this.drawHelper.getStringWidth(new TranslationTextComponent("gui.pet.movement").getString()));
		int buttonX = this.colRightX + buttonMarginX;
		int buttonY = this.colRightY + this.colRightHeight - ((buttonHeight + buttonSpacing) * 3);

		// Actions:
		ButtonBase button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.ACTIVE.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.active"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.TELEPORT.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.teleport"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.PVP.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.pvp"), this);
		this.addButton(button);

		buttonX += buttonWidth + buttonSpacing;
		button = new ButtonBase(BaseCreatureEntity.PET_COMMAND_ID.RELEASE.id + this.petCommandIdStart, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("gui.pet.release"), this);
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

		// Release Confirmation:
		buttonX = this.colRightX + Math.round((float)this.colRightWidth / 2) - (buttonWidth + buttonSpacing);
		buttonY = this.colRightY + Math.round((float)this.colRightHeight / 2) - Math.round((float)buttonHeight / 2);
		button = new ButtonBase(this.releaseConfirmId, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("common.yes"), this);
		button.visible = false;
		this.addButton(button);

		buttonX += buttonSpacing;
		button = new ButtonBase(this.releaseCancelId, buttonX + buttonWidth, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("common.no"), this);
		button.visible = false;
		this.addButton(button);
	}

	@Override
	public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderBackground(matrixStack, mouseX, mouseY, partialTicks);

		// Model:
		if(this.playerExt.selectedPet != null) {
			this.renderCreature(matrixStack, this.playerExt.selectedPet.getCreatureInfo(), this.colRightX + (this.colRightWidth / 2), this.colRightY + Math.round((float) this.colRightHeight / 2), mouseX, mouseY, partialTicks);
		}
	}

	@Override
	protected void renderWidgets(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderWidgets(matrixStack, mouseX, mouseY, partialTicks);

		boolean empty = this.playerExt.petManager.entries.isEmpty();

		if (!empty) {
			this.petTypeList.render(matrixStack, mouseX, mouseY, partialTicks);
			this.petList.render(matrixStack, mouseX, mouseY, partialTicks);
		}

		// Update Buttons:
		for (Widget buttonWidget : this.buttons) {
			if (!(buttonWidget instanceof ButtonBase))
				continue;
			ButtonBase button = (ButtonBase) buttonWidget;

			// Pet Controls:
			if (button.buttonId >= this.petCommandIdStart && button.buttonId < this.releaseConfirmId) {
				button.visible = !empty && this.playerExt.selectedPet != null;
				if (this.playerExt.selectedPet != null && !this.playerExt.selectedPet.releaseEntity) {

					// Actions:
					if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.ACTIVE.id + this.petCommandIdStart) {
						if (!this.playerExt.selectedPet.spawningActive) {
							button.setMessage(new TranslationTextComponent("gui.pet.summon"));
						} else {
							button.setMessage(new TranslationTextComponent("gui.pet.dismiss"));
						}
					} else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.PVP.id + this.petCommandIdStart) {
						if (this.playerExt.selectedPet.summonSet.getPVP()) {
							button.setMessage(new TranslationTextComponent(new TranslationTextComponent("gui.pet.pvp").getString() + ": " + new TranslationTextComponent("common.yes").getString()));
						} else {
							button.setMessage(new TranslationTextComponent(new TranslationTextComponent("gui.pet.pvp").getString() + ": " + new TranslationTextComponent("common.no").getString()));
						}
					}

					// Stance:
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id + this.petCommandIdStart) {
						button.active = !this.playerExt.selectedPet.summonSet.passive;
					} else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.selectedPet.summonSet.getPassive() && !this.playerExt.selectedPet.summonSet.getAssist() && !this.playerExt.selectedPet.summonSet.getAggressive());
					} else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.selectedPet.summonSet.getPassive() && this.playerExt.selectedPet.summonSet.getAssist() && !this.playerExt.selectedPet.summonSet.getAggressive());
					} else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.selectedPet.summonSet.getPassive() && this.playerExt.selectedPet.summonSet.getAggressive());
					}

					// Movement:
					else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.selectedPet.summonSet.getSitting() && this.playerExt.selectedPet.summonSet.getFollowing());
					} else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.WANDER.id + this.petCommandIdStart) {
						button.active = !(!this.playerExt.selectedPet.summonSet.getSitting() && !this.playerExt.selectedPet.summonSet.getFollowing());
					} else if (button.buttonId == BaseCreatureEntity.PET_COMMAND_ID.SIT.id + this.petCommandIdStart) {
						button.active = !(this.playerExt.selectedPet.summonSet.getSitting());
					}
				}

				// Invalid Button:
				else {
					button.visible = false;
				}
			}

			// Release Confirmation:
			else if (button.buttonId == this.releaseConfirmId || button.buttonId == this.releaseCancelId) {
				button.visible = this.playerExt.selectedPet != null && this.playerExt.selectedPet.releaseEntity && !empty;
			}
		}
	}

	@Override
	public void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);

		int marginX = 0;
		int nextX = this.colRightX + marginX;
		int nextY = this.colRightY + 20;
		int width = this.colRightWidth - marginX;

		// Empty:
		if(this.playerExt.petManager.entries.isEmpty()) {
			String text = new TranslationTextComponent("gui.beastiary.pets.empty.info").getString();
			this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);
			return;
		}

		// Player Spirit:
		String text = "\u00A7l" + new TranslationTextComponent("gui.beastiary.player.spirit").getString() + ": ";
		this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF);
		int barX = nextX + this.drawHelper.getStringWidth(text);
		int spiritMax = Math.round((float)this.playerExt.spiritMax / this.playerExt.spiritCharge);
		int spiritReserved = (int)Math.floor((double)this.playerExt.spiritReserved / this.playerExt.spiritCharge);
		int spiritAvailable = (int)Math.floor((double)this.playerExt.spirit / this.playerExt.spiritCharge);
		float spiritFilling = ((float)this.playerExt.spirit / this.playerExt.spiritCharge) - spiritAvailable;
		this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpiritEmpty"), barX, nextY, 0, 9, 9, spiritMax, 10);
		this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpirit"), barX, nextY, 0, 9, 9, spiritAvailable, 10);
		if(spiritFilling > 0) {
			this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetSpiritFilling"), barX + (9 * spiritAvailable), nextY, 0, spiritFilling, 1, spiritFilling * 9, 9);
		}
		this.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetSpiritUsed"), barX, nextY, 0, 9, 9, spiritReserved, -10);

		// Creature Display:
		if(this.playerExt.selectedPet != null) {
			// Spirit:
			nextY += 4 + this.drawHelper.getWordWrappedHeight(text, colRightWidth);
			text = "\u00A7l" + new TranslationTextComponent("creature.stat.spirit").getString() + ": ";
			this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF);
			this.drawLevel(matrixStack, this.playerExt.selectedPet.getCreatureInfo(), TextureManager.getTexture("GUIPetLevel"), nextX + this.drawHelper.getStringWidth(text), nextY);

			// Health:
			nextY += 4 + this.drawHelper.getWordWrappedHeight(text, colRightWidth);
			if(this.playerExt.selectedPet.respawnTime <= 0) {
				text = "\u00A7l" + new TranslationTextComponent("creature.stat.health").getString() + ": ";
			}
			else {
				text = "\u00A7l" + new TranslationTextComponent("creature.stat.respawning").getString() + ": ";
			}
			this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF);

			int barY = nextY - 1;
			int barWidth = (256 / 4) + 16;
			int barHeight = (32 / 4) + 2;
			barX = nextX + this.drawHelper.getStringWidth(text);
			int barCenter = barX + (barWidth / 2);
			this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
			if(this.playerExt.selectedPet.respawnTime <= 0) {
				float healthNormal = Math.min(this.playerExt.selectedPet.getHealth() / this.playerExt.selectedPet.getMaxHealth(), 1);
				this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarHealth"), barX, barY, 0, healthNormal, 1, barWidth * healthNormal, barHeight);
				String healthText = String.format("%.0f", this.playerExt.selectedPet.getHealth()) + "/" + String.format("%.0f", this.playerExt.selectedPet.getMaxHealth());
				this.drawHelper.drawString(matrixStack,healthText, barCenter - (this.drawHelper.getStringWidth(healthText) / 2), barY + 2, 0xFFFFFF);
			}
			else {
				float respawnNormal = 1.0F - ((float)this.playerExt.selectedPet.respawnTime / this.playerExt.selectedPet.respawnTimeMax);
				this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarRespawn"), barX, barY, 0, respawnNormal, 1, barWidth * respawnNormal, barHeight);
				this.drawHelper.drawString(matrixStack, "" + (this.playerExt.selectedPet.respawnTime / 20) + "s", barX + barWidth + 10, nextY, 0xFFFFFF);
			}

			// Experience:
			nextY += 4 + this.drawHelper.getWordWrappedHeight(text, colRightWidth);
			text = "\u00A7l" + new TranslationTextComponent("creature.stat.experience").getString() + ": ";
			this.drawHelper.drawString(matrixStack, text, nextX, nextY, 0xFFFFFF);

			barY = nextY - 1;
			barWidth = (256 / 4) + 16;
			barHeight = (32 / 4) + 2;
			barX = nextX + this.drawHelper.getStringWidth(text);
			barCenter = barX + (barWidth / 2);
			this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
			float experienceNormal = (float)this.playerExt.selectedPet.getExperience() / this.playerExt.selectedPet.getMaxExperience();
			this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIBarExperience"), barX, barY, 0, experienceNormal, 1, barWidth * experienceNormal, barHeight);
			String experienceText = this.playerExt.selectedPet.getExperience() + "/" + this.playerExt.selectedPet.getMaxExperience();
			this.drawHelper.getFontRenderer().draw(matrixStack, experienceText, barCenter - (this.drawHelper.getStringWidth(experienceText) / 2), barY + 2, 0xFFFFFF);
		}

		// Base Display:
		else {
			nextY += 4 + this.drawHelper.getWordWrappedHeight(text, colRightWidth);
			text = new TranslationTextComponent("gui.beastiary.pets.select").getString();
			this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, width, 0xFFFFFF, true);
		}

		// Button Titles:
		int buttonHeight = 20;
		int buttonSpacing = 2;
		int buttonY = this.colRightY + this.colRightHeight - ((buttonHeight + buttonSpacing) * 3);
		if(this.playerExt.selectedPet != null && !this.playerExt.selectedPet.releaseEntity) {
			this.drawHelper.drawString(matrixStack, "\u00A7l" + new TranslationTextComponent("gui.pet.actions").getString(), this.colRightX, buttonY + 6, 0xFFFFFF);
			buttonY += buttonHeight + buttonSpacing;
			this.drawHelper.drawString(matrixStack, "\u00A7l" + new TranslationTextComponent("gui.pet.stance").getString(), this.colRightX, buttonY + 6, 0xFFFFFF);
			buttonY += buttonHeight + buttonSpacing;
			this.drawHelper.drawString(matrixStack, "\u00A7l" + new TranslationTextComponent("gui.pet.movement").getString(), this.colRightX, buttonY + 6, 0xFFFFFF);
		}

		// Release Confirmation:
		if(this.playerExt.selectedPet != null && this.playerExt.selectedPet.releaseEntity) {
			text = new TranslationTextComponent("gui.pet.release.confirm").getString();
			nextX = this.colRightX;
			nextY = this.colRightY + Math.round((float) this.colRightHeight / 2) - Math.round((float) buttonHeight / 2) - this.drawHelper.getWordWrappedHeight(text, this.colRightWidth) - 2;
			this.drawHelper.drawStringWrapped(matrixStack, text, nextX, nextY, this.colRightWidth, 0xFFFFFF, true);
		}
	}

	@Override
	public void actionPerformed(int buttonId) {
		if(this.playerExt.selectedPet != null && this.playerExt.selectedPet.summonSet != null) {
			PetEntry petEntry = this.playerExt.selectedPet;

			// Pet Commands;
			if(buttonId >= this.petCommandIdStart && buttonId < this.releaseConfirmId) {
				int petCommandId = buttonId - this.petCommandIdStart;

				// Actions:
				if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.ACTIVE.id) {
					petEntry.spawningActive = !petEntry.spawningActive;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.TELEPORT.id) {
					petEntry.teleportEntity = true;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.PVP.id) {
					petEntry.summonSet.pvp = !petEntry.summonSet.pvp;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.RELEASE.id) {
					petEntry.releaseEntity = true;
				}

				// Stance:
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id) {
					petEntry.summonSet.passive = true;
					petEntry.summonSet.assist = false;
					petEntry.summonSet.aggressive = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id) {
					petEntry.summonSet.passive = false;
					petEntry.summonSet.assist = false;
					petEntry.summonSet.aggressive = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id) {
					petEntry.summonSet.passive = false;
					petEntry.summonSet.assist = true;
					petEntry.summonSet.aggressive = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id) {
					petEntry.summonSet.passive = false;
					petEntry.summonSet.assist = true;
					petEntry.summonSet.aggressive = true;
				}

				// Movement:
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id) {
					petEntry.summonSet.following = true;
					petEntry.summonSet.sitting = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.WANDER.id) {
					petEntry.summonSet.following = false;
					petEntry.summonSet.sitting = false;
				}
				else if (petCommandId == BaseCreatureEntity.PET_COMMAND_ID.SIT.id) {
					petEntry.summonSet.following = false;
					petEntry.summonSet.sitting = true;
				}

				this.playerExt.sendPetEntryToServer(petEntry);
				if (this.playerExt.selectedPet == null) {
					this.mc.setScreen(new PetsBeastiaryScreen(this.mc.player));
				}
				return;
			}

			// Release Confirmation:
			else if(buttonId == this.releaseCancelId) {
				petEntry.releaseEntity = false;
				return;
			}
			else if(buttonId == this.releaseConfirmId) {
				this.playerExt.selectedPet = null;
				this.playerExt.sendPetEntryRemoveRequest(petEntry);
				return;
			}
		}

		super.actionPerformed(buttonId);
	}

	@Override
	public ITextComponent getTitle() {
		if(this.playerExt.selectedPet != null) {
			ITextComponent title = ((TextComponent)this.playerExt.selectedPet.getDisplayName())
					.append(" ")
					.append(new TranslationTextComponent("creature.stat.level"))
					.append(" " + this.playerExt.selectedPet.getLevel());
			if(this.playerExt.selectedPet.releaseEntity) {
				title = new TranslationTextComponent("gui.pet.release").append(" ").append(title);
			}
			return title;
		}
		if(this.playerExt.petManager.entries.isEmpty()) {
			return new TranslationTextComponent(new TranslationTextComponent("gui.beastiary.pets.empty.title").getString());
		}
		return new TranslationTextComponent(new TranslationTextComponent("gui.beastiary.pets").getString());
	}

	@Override
	public int getDisplaySubspecies(CreatureInfo creatureInfo) {
		return this.playerExt.selectedPet.subspeciesIndex;
	}


	@Override
	public int getDisplayVariant(CreatureInfo creatureInfo) {
		return this.playerExt.selectedPet.variantIndex;
	}

	@Override
	public void playCreatureSelectSound(CreatureInfo creatureInfo) {
		String soundSuffix = "";
		if(this.playerExt.selectedPet != null && creatureInfo.getSubspecies(this.playerExt.selectedPet.subspeciesIndex).name != null) {
			soundSuffix += "." + creatureInfo.getSubspecies(this.playerExt.selectedPet.subspeciesIndex).name;
		}
		SoundEvent soundEvent = ObjectManager.getSound(creatureInfo.getName() + soundSuffix + "_tame");
		this.player.getCommandSenderWorld().playSound(this.player, this.player.position().x(), this.player.position().y(), this.player.position().z(), soundEvent, SoundCategory.NEUTRAL, 1, 1);
	}
}
