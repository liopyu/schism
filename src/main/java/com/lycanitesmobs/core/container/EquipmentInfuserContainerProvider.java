package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentInfuserContainerProvider implements INamedContainerProvider {
	public EquipmentInfuserTileEntity equipmentInfuser;

	public EquipmentInfuserContainerProvider(@Nonnull EquipmentInfuserTileEntity equipmentInfuser) {
		this.equipmentInfuser = equipmentInfuser;
	}

	@Nullable
	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new EquipmentInfuserContainer(windowId, playerInventory, this.equipmentInfuser);
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.equipmentInfuser.getName();
	}
}
