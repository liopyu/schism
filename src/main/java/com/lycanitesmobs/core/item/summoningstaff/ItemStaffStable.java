package com.lycanitesmobs.core.item.summoningstaff;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemStaffStable extends ItemStaffSummoning {
	
	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemStaffStable(Item.Properties properties, String itemName, String textureName) {
        super(properties, itemName, textureName);
    }
	
    
	// ==================================================
	//                       Use
	// ==================================================
    // ========== Rapid Time ==========
    @Override
    public int getRapidTime(ItemStack itemStack) {
        return 20;
    }
    
    // ========== Summon Cost ==========
    public int getSummonCostBoost() {
    	return 2;
    }
    public float getSummonCostMod() {
    	return 1.0F;
    }
    
    // ========== Summon Duration ==========
    public int getSummonDuration() {
    	return 180 * 20;
    }

    @Override
	protected void damageStaff(ItemStack itemStack, int amountToDamage, ServerPlayerEntity entity) {
		amountToDamage = Math.max(1, (int)Math.floor((double)amountToDamage / 2));
		super.damageStaff(itemStack, amountToDamage, entity);
	}
    
	
	// ==================================================
	//                     Repairs
	// ==================================================
    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairStack) {
    	if(repairStack.getItem() == Items.GOLD_INGOT) return true;
        return super.isValidRepairItem(itemStack, repairStack);
    }
}
