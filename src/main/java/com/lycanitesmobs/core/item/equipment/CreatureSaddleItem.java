package com.lycanitesmobs.core.item.equipment;

import com.lycanitesmobs.core.info.CreatureType;
import com.lycanitesmobs.core.item.CreatureTypeItem;


public class CreatureSaddleItem extends CreatureTypeItem {

    public CreatureSaddleItem(Properties properties, CreatureType creatureType) {
		super(properties, creatureType.getSaddleName(), creatureType);
		this.setup();
    }
}
