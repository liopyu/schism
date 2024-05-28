package com.lycanitesmobs.client;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventListener {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        int sharpness = ItemManager.getInstance().getEquipmentSharpnessRepair(event.getItemStack());
        int mana = ItemManager.getInstance().getEquipmentManaRepair(event.getItemStack());
        if (sharpness > 0 || mana > 0) {
            event.getToolTip().add(new TranslationTextComponent("equipment.item.repair").withStyle(TextFormatting.BLUE));
            if (sharpness > 0) {
                event.getToolTip().add(new TranslationTextComponent("equipment.sharpness").append(" " + sharpness).withStyle(TextFormatting.BLUE));
            }
            if (mana > 0) {
                event.getToolTip().add(new TranslationTextComponent("equipment.mana").append(" " + mana).withStyle(TextFormatting.BLUE));
            }
        }

        if (event.getItemStack().getItem() instanceof ItemSoulgazer) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getPlayer());
            if (extendedPlayer != null && extendedPlayer.creatureStudyCooldown > 0) {
                event.getToolTip().add(new TranslationTextComponent("message.beastiary.study.cooldown").append(" " + String.format("%.0f", (float)extendedPlayer.creatureStudyCooldown / 20) + "s").withStyle(TextFormatting.BLUE));
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        GameRenderer fogRenderer = event.getRenderer();
        LivingEntity entityLiving = Minecraft.getInstance().player;
        if (entityLiving == null) {
            return;
        }
        if(entityLiving.isInLava() && (!entityLiving.isOnFire() || entityLiving.hasEffect(Effects.FIRE_RESISTANCE))) {
            event.setDensity(0.5F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if(event.getBlockForOverlay().getMaterial() == Material.FIRE && (!event.getPlayer().isOnFire() || event.getPlayer().hasEffect(Effects.FIRE_RESISTANCE))) {
            event.setCanceled(true);
        }
    }
}
