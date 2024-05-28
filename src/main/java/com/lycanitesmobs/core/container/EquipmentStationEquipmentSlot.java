package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class EquipmentStationEquipmentSlot extends BaseSlot {
	public EquipmentStationContainer container;

	/**
	 * Constructor
	 * @param stationContainer The Equipment Station Container using this slot.
	 * @param slotIndex THe index of this slot.
	 * @param x The x display position.
	 * @param y The y display position.
	 */
	public EquipmentStationEquipmentSlot(EquipmentStationContainer stationContainer, int slotIndex, int x, int y) {
		super(stationContainer.equipmentStation, slotIndex, x, y);
		this.container = stationContainer;
	}


	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return itemStack.getItem() instanceof ItemEquipment;
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
		return 1;
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


	@Override
	public ItemStack onTake(PlayerEntity player, ItemStack itemStack) {
		return super.onTake(player, itemStack);
	}


	@Override
	public boolean mayPickup(PlayerEntity player) {
		return true;
	}
}
