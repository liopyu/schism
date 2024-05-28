package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.CreatureType;
import com.lycanitesmobs.core.item.BaseItem;
import net.minecraft.item.Item;

import javax.annotation.Nullable;


public class CreatureTypeItem extends BaseItem {
	protected CreatureType creatureType;

	/**
	 * Constructor
	 * @param creatureType The creature type this item is used for.
	 */
    public CreatureTypeItem(Item.Properties properties, String itemName, @Nullable CreatureType creatureType) {
		super(properties);
		this.itemName = itemName;
		this.creatureType = creatureType;
		if (creatureType != null) {
			this.modInfo = creatureType.modInfo;
		}
		else {
			this.modInfo = LycanitesMobs.modInfo;
		}
    }

	/**
	 * Gets the creature type that this item is for.
	 * @return The creature type of this item.
	 */
	public CreatureType getCreatureType() {
    	return this.creatureType;
	}
}
