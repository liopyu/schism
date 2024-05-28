package com.lycanitesmobs.client;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;

public class ClientEventListener {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        int sharpness = ItemManager.getInstance().getEquipmentSharpnessRepair(event.getItemStack());
        int mana = ItemManager.getInstance().getEquipmentManaRepair(event.getItemStack());
        if (sharpness > 0 || mana > 0) {
            event.getToolTip().add(Component.translatable("equipment.item.repair").withStyle(ChatFormatting.BLUE));
            if (sharpness > 0) {
                event.getToolTip().add(Component.translatable("equipment.sharpness").append(" " + sharpness).withStyle(ChatFormatting.BLUE));
            }
            if (mana > 0) {
                event.getToolTip().add(Component.translatable("equipment.mana").append(" " + mana).withStyle(ChatFormatting.BLUE));
            }
        }

        if (event.getItemStack().getItem() instanceof ItemSoulgazer) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getPlayer());
            if (extendedPlayer != null && extendedPlayer.creatureStudyCooldown > 0) {
                event.getToolTip().add(Component.translatable("message.beastiary.study.cooldown").append(" " + String.format("%.0f", (float)extendedPlayer.creatureStudyCooldown / 20) + "s").withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFogDensity(ViewportEvent.RenderFog event) {
        GameRenderer fogRenderer = event.getRenderer();
        LivingEntity entityLiving = Minecraft.getInstance().player;
        if (entityLiving == null) {
            return;
        }
        if(entityLiving.isInLava() && (!entityLiving.isOnFire() || entityLiving.hasEffect(MobEffects.FIRE_RESISTANCE))) {
            event.scaleNearPlaneDistance(0.5F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onBlockOverlay(RenderBlockScreenEffectEvent event) {
        if(event.getBlockState().getBlock() == Blocks.FIRE && (!event.getPlayer().isOnFire() || event.getPlayer().hasEffect(MobEffects.FIRE_RESISTANCE))) {
            event.setCanceled(true);
        }
    }
}
