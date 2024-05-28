package com.lycanitesmobs.core.item.equipment.features;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SlotEquipmentFeature extends EquipmentFeature {
	/** The type of slot that this adds to the part. Can be: head, blade, axe, pike or jewel. Shouldn't be base else you can have infinitely large weapons! **/
	public String slotType;

	/** Loads this slot from a JSON object. **/
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
		this.slotType = json.get("slotType").getAsString();
	}

	@Override
	public ITextComponent getDescription(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		return new TranslationTextComponent("equipment.feature." + this.featureType).append(" ").append(new TranslationTextComponent("equipment.slot." + this.slotType));
	}

	@Override
	public ITextComponent getSummary(ItemStack itemStack, int level) {
		return this.getDescription(itemStack, level);
	}
}
