package com.lycanitesmobs.core.item;

import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.item.Item;


public class ItemMobToken extends BaseItem {

	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemMobToken(Item.Properties properties, ModInfo group) {
        super(properties);
		this.itemName = "mobtoken";
		this.modInfo = group;
        this.setup();
    }

    @Override
    public void setup() {
        this.setRegistryName(this.modInfo.modid, this.itemName);
    }
}
