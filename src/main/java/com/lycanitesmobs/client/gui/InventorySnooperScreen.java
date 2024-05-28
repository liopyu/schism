package com.lycanitesmobs.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A sneaky snooping class that taps into GuiInventory to try and grab stuff from those stupid protected fields!
 *
 */
public class InventorySnooperScreen extends InventoryScreen {

	public InventorySnooperScreen(PlayerEntity entityPlayer) {
		super(entityPlayer);
	}
	
	/**
	 * This agent's missing is to infiltrate the buttonList field by comparing it to it's own.
	 * @return The name of the buttonList field before or after obfuscation.
	 */
	public String getButtonListFieldName() {
		try {
			for(Field field : Screen.class.getDeclaredFields()) {
				field.setAccessible(true);
				Object object = field.get(this);
				if(object instanceof List)
					if(object == this.buttons)
						return field.getName();
			}
		} catch(Exception e) {}
		return "";
	}
	
	public int getGUIXSize() {
		return this.imageWidth;
	}
	
	public int getGUIYSize() {
		return this.imageHeight;
	}

}
