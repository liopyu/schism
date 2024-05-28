package com.lycanitesmobs.client.gui.overlays;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.client.KeyHandler;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.gui.BaseGui;
import com.lycanitesmobs.client.gui.DrawHelper;
import com.lycanitesmobs.core.config.ConfigDebug;
import com.lycanitesmobs.core.entity.*;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.item.summoningstaff.ItemStaffSummoning;
import com.lycanitesmobs.client.mobevent.MobEventPlayerClient;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class BaseOverlay extends BaseGui {
	public DrawHelper drawHelper;
	public Minecraft minecraft;
	
	private int mountMessageTimeMax = 10 * 20;
	private int mountMessageTime = 0;
	
    // ==================================================
    //                     Constructor
    // ==================================================
	public BaseOverlay(Minecraft minecraft) {
		super(new TranslationTextComponent("gui.overlay"));
		this.minecraft = Minecraft.getInstance();
		this.drawHelper = new DrawHelper(minecraft, minecraft.font);
	}
	
	
    // ==================================================
    //                  Draw Game Overlay
    // ==================================================
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRenderExperienceBar(RenderGameOverlayEvent event) {
        if(ClientManager.getInstance().getClientPlayer() == null)
            return;
        PlayerEntity player = ClientManager.getInstance().getClientPlayer();

		if(event.isCancelable() || event.getType() != ElementType.EXPERIENCE) {
			return;
		}
		MatrixStack matrixStack = event.getMatrixStack();

		matrixStack.pushPose();
		RenderSystem.disableLighting();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		int sWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth(); // getMainWindow()
        int sHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // ========== Mob/World Events Title ==========
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(player.getCommandSenderWorld());
        if(worldExt != null) {
            for(MobEventPlayerClient mobEventPlayerClient : worldExt.clientMobEventPlayers.values()) {
				matrixStack.pushPose();
				mobEventPlayerClient.onGUIUpdate(matrixStack, this, sWidth, sHeight);
				matrixStack.popPose();
			}
            if(worldExt.clientWorldEventPlayer != null) {
				matrixStack.pushPose();
				worldExt.clientWorldEventPlayer.onGUIUpdate(matrixStack, this, sWidth, sHeight);
				matrixStack.popPose();
			}
        }
		
		// ========== Summoning Focus Bar ==========
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
		if(playerExt != null && !this.minecraft.player.abilities.instabuild && (
                (this.minecraft.player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof ItemStaffSummoning)
                || (this.minecraft.player.getItemInHand(Hand.OFF_HAND).getItem() instanceof ItemStaffSummoning)
                )) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(TextureManager.getTexture("GUIInventoryCreature"));
			
			int barYSpace = 10;
			int barXSpace = -1;
			
            int summonBarWidth = 9;
            int summonBarHeight = 9;
            int summonBarX = (sWidth / 2) + 10;
            int summonBarY = sHeight - 30 - summonBarHeight;
            int summonBarU = 256 - summonBarWidth;
            int summonBarV = 256 - summonBarHeight;
            
            summonBarY -= barYSpace;
            if(this.minecraft.player.isEyeInFluid(FluidTags.WATER))
            	summonBarY -= barYSpace;
            
            for(int summonBarEnergyN = 0; summonBarEnergyN < 10; summonBarEnergyN++) {
            	this.drawHelper.drawTexturedModalRect(matrixStack, summonBarX + ((summonBarWidth + barXSpace) * summonBarEnergyN), summonBarY, summonBarU, summonBarV, summonBarWidth, summonBarHeight);
            	if(playerExt.summonFocus >= playerExt.summonFocusMax - (summonBarEnergyN * playerExt.summonFocusCharge)) {
                	this.drawHelper.drawTexturedModalRect(matrixStack, summonBarX + ((summonBarWidth + barXSpace) * summonBarEnergyN), summonBarY, summonBarU - summonBarWidth, summonBarV, summonBarWidth, summonBarHeight);
            	}
                else if(playerExt.summonFocus + playerExt.summonFocusCharge > playerExt.summonFocusMax - (summonBarEnergyN * playerExt.summonFocusCharge)) {
            		float summonChargeScale = (float)(playerExt.summonFocus % playerExt.summonFocusCharge) / (float)playerExt.summonFocusCharge;
            		this.drawHelper.drawTexturedModalRect(matrixStack, (summonBarX + ((summonBarWidth + barXSpace) * summonBarEnergyN)) + (summonBarWidth - Math.round((float)summonBarWidth * summonChargeScale)), summonBarY, summonBarU - Math.round((float)summonBarWidth * summonChargeScale), summonBarV, Math.round((float)summonBarWidth * summonChargeScale), summonBarHeight);
            	}
            }
		}
		
		// ========== Mount Stamina Bar ==========
		if(this.minecraft.player.getVehicle() != null && this.minecraft.player.getVehicle() instanceof RideableCreatureEntity) {
			RideableCreatureEntity mount = (RideableCreatureEntity)this.minecraft.player.getVehicle();
            float mountStamina = mount.getStaminaPercent();
            
            // Mount Controls Message:
            if(this.mountMessageTime > 0) {
            	ITextComponent mountMessage = new TranslationTextComponent("gui.mount.controls.prefix")
						.append(" ").append(KeyHandler.instance.mountAbility.getTranslatedKeyMessage())
						.append(" ").append(new TranslationTextComponent("gui.mount.controls.ability"));
//						.append(" ").append(KeyHandler.instance.dismount.getTranslatedKeyMessage())
//						.append(" ").append(new TranslationTextComponent("gui.mount.controls.dismount"));
				this.minecraft.gui.setOverlayMessage(mountMessage, false);
            }
            
            // Mount Ability Stamina Bar:
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
            int staminaBarWidth = 182;
            int staminaBarHeight = 5;
            int staminaEnergyWidth = (int)((float)(staminaBarWidth + 1) * mountStamina);
            int staminaBarX = (sWidth / 2) - (staminaBarWidth / 2);
            int staminaBarY = sHeight - 32 + 3;
            int staminaTextureY = 84;
            if("toggle".equals(mount.getStaminaType()))
            	staminaTextureY -= staminaBarHeight * 2;
            int staminaEnergyY = staminaTextureY + staminaBarHeight;
            
            this.drawHelper.drawTexturedModalRect(matrixStack, staminaBarX, staminaBarY, 0, staminaTextureY, staminaBarWidth, staminaBarHeight);
            if(staminaEnergyWidth > 0)
                this.drawHelper.drawTexturedModalRect(matrixStack, staminaBarX, staminaBarY, 0, staminaEnergyY, staminaEnergyWidth, staminaBarHeight);
            
            if(this.mountMessageTime > 0)
            	this.mountMessageTime--;
		}
		else
			this.mountMessageTime = this.mountMessageTimeMax;

		// ========== Taming Reputation Bar ==========
		RayTraceResult mouseOver = Minecraft.getInstance().hitResult;
		if(mouseOver instanceof EntityRayTraceResult) {
			Entity mouseOverEntity = ((EntityRayTraceResult) mouseOver).getEntity();
			if (mouseOverEntity instanceof BaseCreatureEntity) {
				BaseCreatureEntity creatureEntity = (BaseCreatureEntity)mouseOverEntity;
				CreatureInfo creatureInfo = creatureEntity.creatureInfo;
				CreatureRelationshipEntry relationshipEntry = creatureEntity.relationships.getEntry(player);
				if (relationshipEntry != null && relationshipEntry.getReputation() > 0 && !creatureEntity.isTamed()) {
					float barWidth = 100;
					float barHeight = 11;
					float barX = ((float) this.minecraft.getWindow().getGuiScaledWidth() / 2) - (barWidth / 2);
					float barY = (float) this.minecraft.getWindow().getGuiScaledHeight() * 0.75F;
					float barCenter = barX + (barWidth / 2);

					this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
					float reputationNormal = Math.min(1, (float)relationshipEntry.getReputation() / creatureInfo.getTamingReputation());
					String barFillTexture = "GUIPetBarRespawn";
					if (relationshipEntry.getReputation() >= creatureInfo.getFriendlyReputation()) {
						barFillTexture = "GUIPetBarHealth";
					}
					this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture(barFillTexture), barX, barY, 0, reputationNormal, 1, barWidth * reputationNormal, barHeight);
					String reputationText = new TranslationTextComponent("entity.reputation").getString() + ": " + relationshipEntry.getReputation() + "/" + creatureInfo.getTamingReputation();
					this.drawHelper.getFontRenderer().draw(matrixStack, reputationText, barCenter - ((float) this.drawHelper.getStringWidth(reputationText) / 2), barY + 2, 0xFFFFFF);
				}
			}
		}

		matrixStack.popPose();
		this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
	}


	// ==================================================
	//                 Debug Overlay
	// ==================================================
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onGameOverlay(RenderGameOverlayEvent.Text event) {
		if(!ConfigDebug.INSTANCE.creatureOverlay.get()) {
			return;
		}

		// Entity:
		RayTraceResult mouseOver = Minecraft.getInstance().hitResult;
		if(mouseOver instanceof EntityRayTraceResult) {
			Entity mouseOverEntity = ((EntityRayTraceResult)mouseOver).getEntity();
			if(mouseOverEntity instanceof BaseCreatureEntity) {
				BaseCreatureEntity mouseOverCreature = (BaseCreatureEntity)mouseOverEntity;
				event.getLeft().add("");
				event.getLeft().add("Target Creature: " + mouseOverCreature.getName().getString());
				event.getLeft().add("Distance To player: " + mouseOverCreature.distanceTo(Minecraft.getInstance().player));
				event.getLeft().add("Elements: " + mouseOverCreature.creatureInfo.getElementNames(mouseOverCreature.getSubspecies()).getString());
				event.getLeft().add("Subspecies: " + mouseOverCreature.getSubspeciesIndex());
				event.getLeft().add("Variant: " + mouseOverCreature.getVariantIndex());
				event.getLeft().add("Level: " + mouseOverCreature.getMobLevel());
				event.getLeft().add("Experience: " + mouseOverCreature.getExperience() + "/" + mouseOverCreature.creatureStats.getExperienceForNextLevel());
				event.getLeft().add("Size: " + mouseOverCreature.sizeScale);
				event.getLeft().add("");
				event.getLeft().add("Health: " + mouseOverCreature.getHealth() + "/" + mouseOverCreature.getMaxHealth() + " Fresh: " + mouseOverCreature.creatureStats.getHealth());
				event.getLeft().add("Speed: " + mouseOverCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue() + "/" + mouseOverCreature.creatureStats.getSpeed());
				event.getLeft().add("");
				event.getLeft().add("Defense: " + mouseOverCreature.creatureStats.getDefense());
				event.getLeft().add("Armor: " + mouseOverCreature.getArmorValue());
				event.getLeft().add("");
				event.getLeft().add("Damage: " + mouseOverCreature.creatureStats.getDamage());
				event.getLeft().add("Melee Speed: " + mouseOverCreature.creatureStats.getAttackSpeed());
				event.getLeft().add("Melee Range: " + mouseOverCreature.getPhysicalRange());
				event.getLeft().add("Ranged Speed: " + mouseOverCreature.creatureStats.getRangedSpeed());
				event.getLeft().add("Pierce: " + mouseOverCreature.creatureStats.getPierce());
				event.getLeft().add("");
				event.getLeft().add("Effect Duration: " + mouseOverCreature.creatureStats.getEffect() + " Base Seconds");
				event.getLeft().add("Effect Amplifier: x" + mouseOverCreature.creatureStats.getAmplifier());
				event.getLeft().add("");
				event.getLeft().add("Has Attack Target: " + mouseOverCreature.hasAttackTarget());
				event.getLeft().add("Has Avoid Target: " + mouseOverCreature.hasAvoidTarget());
				event.getLeft().add("Has Master Target: " + mouseOverCreature.hasMaster());
				event.getLeft().add("Has Parent Target: " + mouseOverCreature.hasParent());

				event.getLeft().add("");
				CreatureRelationshipEntry relationshipEntry = mouseOverCreature.relationships.getEntry(this.minecraft.player);
				event.getLeft().add("Reputation with Player: " + (relationshipEntry != null ? relationshipEntry.getReputation() : 0) + "/" + mouseOverCreature.creatureInfo.getTamingReputation());

				if(mouseOverEntity instanceof TameableCreatureEntity) {
					TameableCreatureEntity mouseOverTameable = (TameableCreatureEntity)mouseOverCreature;
					event.getLeft().add("Owner ID: " + (mouseOverTameable.getOwnerId() != null ? mouseOverTameable.getOwnerId().toString() : "None"));
					event.getLeft().add("Owner Name: " + mouseOverTameable.getOwnerName().getString());
				}
			}
		}
	}
}
