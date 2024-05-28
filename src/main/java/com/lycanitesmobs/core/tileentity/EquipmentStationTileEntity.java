package com.lycanitesmobs.core.tileentity;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.CustomItemEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class EquipmentStationTileEntity extends TileEntityBase {
	/** A list of item stacks in the station. **/
	protected NonNullList<ItemStack> itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);

	public ITextComponent getName() {
		return new TranslationTextComponent("block.lycanitesmobs.equipment_station");
	}

	@Override
	public TileEntityType<?> getType() {
		return ObjectManager.tileEntityTypes.get(this.getClass());
	}

	@Override
	public void setRemoved() {
		for (ItemStack itemStack : this.itemStacks) {
			CustomItemEntity entityItem = new CustomItemEntity(this.getLevel(), this.getBlockPos().getX(), this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ(), itemStack);
			this.getLevel().addFreshEntity(entityItem);
		}
		this.clearContent();
		super.setRemoved();
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.itemStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getItem(int index) {
		return this.itemStacks.get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 */
	@Override
	public ItemStack removeItem(int index, int count) {
		return ItemStackHelper.removeItem(this.itemStacks, index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ItemStackHelper.takeItem(this.itemStacks, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setItem(int index, ItemStack stack) {
		this.itemStacks.set(index, stack);
		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}
	}

	@Override
	public int getContainerSize() {
		return this.itemStacks.size();
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void startOpen(PlayerEntity player) {

	}

	@Override
	public void stopOpen(PlayerEntity player) {

	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
	 * guis use Slot.isItemValid
	 */
	@Override
	public boolean canPlaceItem(int index, ItemStack itemStack) {
		ItemStack existingStack = this.getItem(index);
		return existingStack.isEmpty();
	}

	@Override
	public void clearContent() {
		this.itemStacks.clear();
	}

	@Override
	public boolean triggerEvent(int eventID, int eventArg) {
		return false;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT syncData = new CompoundNBT();
		return new SUpdateTileEntityPacket(this.getBlockPos(), 1, syncData);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		super.onDataPacket(net, packet);
	}

	@Override
	public void onGuiButton(int buttonId) {

	}

	@Override
	public void load(BlockState blockState, CompoundNBT nbtTagCompound) {
		super.load(blockState, nbtTagCompound);
		if(nbtTagCompound.contains("Items")) {
			ItemStackHelper.loadAllItems(nbtTagCompound, this.itemStacks);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbtTagCompound) {
		ItemStackHelper.saveAllItems(nbtTagCompound, this.itemStacks);
		return super.save(nbtTagCompound);
	}
}
