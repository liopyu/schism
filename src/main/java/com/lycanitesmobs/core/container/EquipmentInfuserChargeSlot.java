package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.item.ChargeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class EquipmentInfuserChargeSlot extends BaseSlot {
	public EquipmentInfuserContainer container;

	/**
	 * Constructor
	 * @param container The Equipment Infuser Container using this slot.
	 * @param slotIndex THe index of this slot.
	 * @param x The x display position.
	 * @param y The y display position.
	 */
	public EquipmentInfuserChargeSlot(EquipmentInfuserContainer infuserContainer, int slotIndex, int x, int y) {
		super(infuserContainer.equipmentInfuser, slotIndex, x, y);
		this.container = infuserContainer;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return itemStack.getItem() instanceof ChargeItem || itemStack.getItem() instanceof DyeItem || itemStack.getItem() == Items.WATER_BUCKET || itemStack.getItem() == Items.BUCKET;
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
		this.container.attemptInfusion();
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
