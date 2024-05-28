package com.lycanitesmobs.core.item;

import com.lycanitesmobs.ObjectManager;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMBlocksGroup extends ItemGroup {

	// ========== Constructor ==========
	public LMBlocksGroup(String modID) {
		super(modID);
	}
	
	// ========== Tab Icon ==========
	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack makeIcon() {
		if(ObjectManager.getBlock("shadowcrystal") != null)
			return new ItemStack(Item.byBlock(ObjectManager.getBlock("shadowcrystal")));
		else if(ObjectManager.getBlock("summoningpedestal") != null)
			return new ItemStack(Item.byBlock(ObjectManager.getBlock("summoningpedestal")));
		else if(ObjectManager.getBlock("demoncrystal") != null)
			return new ItemStack(Item.byBlock(ObjectManager.getBlock("demoncrystal")));
		else
			return new ItemStack(Item.byBlock(Blocks.OBSIDIAN));
	}
}