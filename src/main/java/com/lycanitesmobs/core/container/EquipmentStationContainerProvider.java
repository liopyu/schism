package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.tileentity.EquipmentStationTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentStationContainerProvider implements INamedContainerProvider {
	public EquipmentStationTileEntity equipmentStation;

	public EquipmentStationContainerProvider(@Nonnull EquipmentStationTileEntity equipmentStation) {
		this.equipmentStation = equipmentStation;
	}

	@Nullable
	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new EquipmentStationContainer(windowId, playerInventory, this.equipmentStation);
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.equipmentStation.getName();
	}
}
