package com.lycanitesmobs.core.item;

import com.lycanitesmobs.ObjectManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMItemsGroup extends ItemGroup {
	
	// ========== Constructor ==========
	public LMItemsGroup(String modID) {
		super(modID);
	}


	// ========== Tab Icon ==========
	@OnlyIn(Dist.CLIENT)
	@Override
	public ItemStack makeIcon() {
		if(ObjectManager.getItem("soulgazer") != null)
			return new ItemStack(ObjectManager.getItem("soulgazer"));
		else if(ObjectManager.getItem("poisongland") != null)
			return new ItemStack(ObjectManager.getItem("poisongland"));
		else
			return new ItemStack(Items.EMERALD);
	}
}