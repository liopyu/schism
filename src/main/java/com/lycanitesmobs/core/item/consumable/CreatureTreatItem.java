package com.lycanitesmobs.core.item.consumable;

import com.lycanitesmobs.core.info.CreatureType;
import com.lycanitesmobs.core.item.CreatureTypeItem;


public class CreatureTreatItem extends CreatureTypeItem {

    public CreatureTreatItem(Properties properties, CreatureType creatureType) {
		super(properties, creatureType.getTreatName(), creatureType);
		this.setup();
    }
}
