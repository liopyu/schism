package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.beastiary.IndexBeastiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraft.client.gui.widget.button.Button.IPressable;

public class MainTab extends Tab {

	public MainTab(int id, IPressable pressable) {
        super(id, Tab.startX, Tab.startY, new ResourceLocation(LycanitesMobs.MODID, "textures/items/soulgazer.png"), new TranslationTextComponent("Index"), pressable);
    }

    public MainTab(int id, int x, int y, IPressable pressable) {
        super(id, x, y, new ResourceLocation(LycanitesMobs.MODID, "textures/items/soulgazer.png"), new TranslationTextComponent("Index"), pressable);
    }

    @Override
    public void onTabClicked () {
	    Minecraft.getInstance().setScreen(new IndexBeastiaryScreen(Minecraft.getInstance().player));
    }

    @Override
    public boolean shouldAddToList () {
        return true;
    }
}
