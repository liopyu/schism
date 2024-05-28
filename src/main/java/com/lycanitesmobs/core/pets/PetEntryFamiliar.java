package com.lycanitesmobs.core.pets;


import net.minecraft.entity.LivingEntity;

import java.util.UUID;

public class PetEntryFamiliar extends PetEntry {

    // ==================================================
    //                     Constructor
    // ==================================================
	public PetEntryFamiliar(UUID petEntryID, LivingEntity host, String summonType) {
        super(petEntryID, "familiar", host, summonType);
	}
}
