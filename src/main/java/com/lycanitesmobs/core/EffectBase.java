package com.lycanitesmobs.core;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.TextureManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EffectBase extends Effect {
	public String name;
	
	// ==================================================
	//                    Constructor
	// ==================================================
	public EffectBase(String name, boolean badEffect, int color) {
		super(badEffect ? EffectType.HARMFUL : EffectType.BENEFICIAL, color);
		this.name = name;
		this.setRegistryName(LycanitesMobs.MODID, name);
		TextureManager.addTexture("effect." + name, LycanitesMobs.modInfo, "textures/mob_effect/" + name + ".png");
	}
	
	
	// ==================================================
	//                    Effects
	// ==================================================
	@Override
	public boolean isInstantenous() {
        return false;
    }
	
	
	// ==================================================
	//                    Visuals
	// ==================================================
	@OnlyIn(Dist.CLIENT)
	@Override
	public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
		ResourceLocation texture = TextureManager.getTexture("effect." + this.name);
		if(texture == null) {
			return;
		}

		Minecraft.getInstance().getTextureManager().bind(texture);
		gui.blit(mStack, x + 6, y + 7, 0, 0, 18, 18, 18, 18);
		super.renderInventoryEffect(effect, gui, mStack, x, y, z);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha) {
		ResourceLocation texture = TextureManager.getTexture("effect." + this.name);
		if(texture == null) {
			return;
		}

		Minecraft.getInstance().getTextureManager().bind(texture);
		gui.blit(mStack, x + 3, y + 3, 0, 0, 18, 18, 18, 18);
	}
}
