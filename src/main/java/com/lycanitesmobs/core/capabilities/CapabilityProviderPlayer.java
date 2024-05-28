package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityProviderPlayer implements ICapabilitySerializable<CompoundNBT> {
	public LazyOptional<IExtendedPlayer> instance;

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if(capability != LycanitesMobs.EXTENDED_PLAYER)
			return LazyOptional.empty();
		if(this.instance == null) {
			this.instance = LazyOptional.of(LycanitesMobs.EXTENDED_PLAYER::getDefaultInstance);
		}
		return this.instance.cast();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) LycanitesMobs.EXTENDED_PLAYER.getStorage().writeNBT(LycanitesMobs.EXTENDED_PLAYER, this.getCapability(LycanitesMobs.EXTENDED_PLAYER, null).orElse(null), null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		LycanitesMobs.EXTENDED_PLAYER.getStorage().readNBT(LycanitesMobs.EXTENDED_PLAYER, this.getCapability(LycanitesMobs.EXTENDED_PLAYER, null).orElse(null), null, nbt);
	}
}
