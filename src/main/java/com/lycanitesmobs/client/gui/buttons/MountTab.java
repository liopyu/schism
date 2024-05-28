package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.KeyHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraft.client.gui.widget.button.Button.IPressable;

public class MountTab extends Tab {
	
	public MountTab(int id, IPressable pressable) {
        super(id, Tab.startX, Tab.startY, new ResourceLocation("textures/items/saddle.png"), new TranslationTextComponent("Mounts"), pressable);
    }

    @Override
    public void onTabClicked () {
        KeyBinding.set(KeyHandler.instance.mountInventory.getKey(), true); //TODO Add a better way that works!
    }

    @Override
    public boolean shouldAddToList () {
    	return false;
        /*boolean ridingMount = Minecraft.getMinecraft().player.ridingEntity instanceof EntityCreatureRideable;
        if(ridingMount)
        	this.icon = ((EntityCreatureRideable)Minecraft.getMinecraft().player.ridingEntity).creatureInfo.getSprite();
        return ridingMount;*/
    }
}
