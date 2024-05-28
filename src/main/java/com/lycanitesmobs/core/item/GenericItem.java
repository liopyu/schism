package com.lycanitesmobs.core.item;

import net.minecraft.item.Item.Properties;

public class GenericItem extends BaseItem {
	public String modelName;

	public GenericItem(Properties properties, String itemName) {
		super(properties);
		this.itemName = itemName;
		super.setup();
	}
}
