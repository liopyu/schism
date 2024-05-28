package com.lycanitesmobs.core.container;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.tileentity.EquipmentStationTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;

public class EquipmentStationContainer extends BaseContainer {
	public static final ContainerType<EquipmentStationContainer> TYPE = (ContainerType<EquipmentStationContainer>)IForgeContainerType.create(EquipmentStationContainer::new).setRegistryName(LycanitesMobs.MODID, "equipment_station");
	public EquipmentStationTileEntity equipmentStation;
	EquipmentStationEquipmentSlot equipmentSlot;
	EquipmentStationRepairSlot repairSlot;

	/**
	 * Client Constructor
	 * @param windowId The window id for the gui screen to use.
	 * @param playerInventory The accessing player's inventory.
	 * @param extraData A packet sent from the server to create the Container from.
	 */
	public EquipmentStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
		this(windowId, playerInventory, (EquipmentStationTileEntity) playerInventory.player.getCommandSenderWorld().getBlockEntity(BlockPos.of(extraData.readLong())));
	}

	/**
	 * Main Constructor
	 * @param equipmentStation The Equipment Forge Tile Entity.
	 * @param playerInventory The Inventory of the accessing player.
	 */
	public EquipmentStationContainer(int windowId, PlayerInventory playerInventory, EquipmentStationTileEntity equipmentStation) {
		super(TYPE, windowId);
		this.equipmentStation = equipmentStation;

		// Player Inventory
		this.addPlayerSlots(playerInventory, 0, 0);

		// Station Inventory
		this.inventoryStart = this.slots.size();
		int slots = 0;
		if(equipmentStation.getContainerSize() > 0) {
			int y = 28;

			this.repairSlot = new EquipmentStationRepairSlot(this, slots++, 50, y);
			this.addSlot(this.repairSlot);

			this.equipmentSlot = new EquipmentStationEquipmentSlot(this, slots++, 110, y);
			this.addSlot(this.equipmentSlot);
		}
		this.inventoryFinish = this.inventoryStart + slots;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		if(this.equipmentStation == null || !this.equipmentStation.stillValid(player)) {
			return false;
		}
		return true;
	}

	/**
	 * Called by either the Repair slot or the Equipment slot, performs a repair if possible.
	 */
	public void attemptRepair() {
		if(this.equipmentSlot.getItem().isEmpty() || !(this.equipmentSlot.getItem().getItem() instanceof ItemEquipment)) {
			return;
		}

		int sharpness = ItemManager.getInstance().getEquipmentSharpnessRepair(this.repairSlot.getItem());
		int mana = ItemManager.getInstance().getEquipmentManaRepair(this.repairSlot.getItem());
		if (sharpness <= 0 && mana <= 0) {
			return;
		}

		ItemEquipment equipment = (ItemEquipment) this.equipmentSlot.getItem().getItem();
		boolean repaired = false;
		if (sharpness > 0) {
			repaired = equipment.addSharpness(this.equipmentSlot.getItem(), sharpness);
		}
		if (mana > 0) {
			repaired = equipment.addMana(this.equipmentSlot.getItem(), mana) || repaired;
		}

		if (repaired) {
			this.repairSlot.remove(1);
			this.attemptRepair();
		}
	}

	/**
	 * Disabled until fixed later.
	 */
	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int slotID) {
		return ItemStack.EMPTY;
	}
}
