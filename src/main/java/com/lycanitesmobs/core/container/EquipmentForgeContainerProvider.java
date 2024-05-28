package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentForgeContainerProvider implements INamedContainerProvider {
	public TileEntityEquipmentForge equipmentForge;

	public EquipmentForgeContainerProvider(@Nonnull TileEntityEquipmentForge equipmentForge) {
		this.equipmentForge = equipmentForge;
	}

	@Nullable
	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new EquipmentForgeContainer(windowId, playerInventory, this.equipmentForge);
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.equipmentForge.getName();
	}
}
