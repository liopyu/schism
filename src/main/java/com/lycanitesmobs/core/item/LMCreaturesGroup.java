package com.lycanitesmobs.core.item;

import com.lycanitesmobs.ObjectManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMCreaturesGroup extends ItemGroup {
	private ItemStack iconStack = ItemStack.EMPTY;
	private boolean fallbackIcon = false;

	public LMCreaturesGroup(String modID) {
		super(modID);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack makeIcon() {
		this.fallbackIcon = false;
		if(ObjectManager.getItem("beastspawn") != null)
			return new ItemStack(ObjectManager.getItem("beastspawn"));
		if(ObjectManager.getItem("demonspawn") != null)
			return new ItemStack(ObjectManager.getItem("demonspawn"));
		if(ObjectManager.getItem("avianspawn") != null)
			return new ItemStack(ObjectManager.getItem("avianspawn"));
		if(ObjectManager.getItem("arthropodspawn") != null)
			return new ItemStack(ObjectManager.getItem("arthropodspawn"));

		this.fallbackIcon = true;
		return new ItemStack(Items.CREEPER_SPAWN_EGG);
	}

	@OnlyIn(Dist.CLIENT)
	public ItemStack getIconItem() {
		if (this.iconStack.isEmpty() || this.fallbackIcon) {
			this.iconStack = this.makeIcon();
		}
		return this.iconStack;
	}
}