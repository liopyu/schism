package com.lycanitesmobs.core.item;

import com.lycanitesmobs.ObjectManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMEquipmentPartsGroup extends ItemGroup {
	private ItemStack iconStack = ItemStack.EMPTY;
	private boolean fallbackIcon = false;

	public LMEquipmentPartsGroup(String modID) {
		super(modID);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack makeIcon() {
		this.fallbackIcon = false;
		if(ObjectManager.getItem("equipmentpart_eechetikarm") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_eechetikarm"));
		if(ObjectManager.getItem("equipmentpart_darklingskull") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_darklingskull"));
		if(ObjectManager.getItem("equipmentpart_grueclaw") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_grueclaw"));
		if(ObjectManager.getItem("equipmentpart_xaphanspine") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_xaphanspine"));
		if(ObjectManager.getItem("equipmentpart_geonachfist") != null)
			return new ItemStack(ObjectManager.getItem("equipmentpart_geonachfist"));

		this.fallbackIcon = true;
		if(ObjectManager.getItem("geistliver") != null)
			return new ItemStack(ObjectManager.getItem("geistliver"));
		return new ItemStack(Items.BONE);
	}

	@OnlyIn(Dist.CLIENT)
	public ItemStack getIconItem() {
		if (this.iconStack.isEmpty() || this.fallbackIcon) {
			this.iconStack = this.makeIcon();
		}
		return this.iconStack;
	}
}