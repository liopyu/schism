package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.item.ChargeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class EquipmentStationRepairSlot extends BaseSlot {
	public EquipmentStationContainer container;

	/**
	 * Constructor
	 * @param container The Equipment Infuser Container using this slot.
	 * @param slotIndex THe index of this slot.
	 * @param x The x display position.
	 * @param y The y display position.
	 */
	public EquipmentStationRepairSlot(EquipmentStationContainer container, int slotIndex, int x, int y) {
		super(container.equipmentStation, slotIndex, x, y);
		this.container = container;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return ItemManager.getInstance().getEquipmentSharpnessRepair(itemStack) > 0 || ItemManager.getInstance().getEquipmentManaRepair(itemStack) > 0;
	}

	/**
	 * Returns true if this slot has an item stack.
	 * @return True if this slot has a valid item stack.
	 */
	@Override
	public boolean hasItem() {
		return super.hasItem();
	}

	@Override
	public int getMaxStackSize() {
		return 64;
    }

	/**
	 * Called when an ItemStack is inserted into this slot.
	 * @param itemStack The ItemStack being inserted.
	 */
	@Override
	public void set(ItemStack itemStack) {
		super.set(itemStack);
		this.container.attemptRepair();
	}

	/**
	 * Called when an ItemStack is removed into this slot.
	 * @param itemStack The ItemStack being inserted.
	 */
	@Override
	public ItemStack onTake(PlayerEntity player, ItemStack itemStack) {
		return super.onTake(player, itemStack);
	}

	@Override
	public boolean mayPickup(PlayerEntity player) {
		return true;
	}
}
