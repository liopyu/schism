package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SummoningPedestalContainerProvider implements INamedContainerProvider {
	public TileEntitySummoningPedestal summoningPedestal;

	public SummoningPedestalContainerProvider(@Nonnull TileEntitySummoningPedestal summoningPedestal) {
		this.summoningPedestal = summoningPedestal;
	}

	@Nullable
	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new SummoningPedestalContainer(windowId, playerInventory, this.summoningPedestal);
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.summoningPedestal.getName();
	}
}
