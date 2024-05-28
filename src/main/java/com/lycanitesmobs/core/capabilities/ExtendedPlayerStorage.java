package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class ExtendedPlayerStorage implements Capability.IStorage<IExtendedPlayer> {

    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    @Override
    public void readNBT(Capability<IExtendedPlayer> capability, IExtendedPlayer instance, Direction facing, INBT nbt) {
        if(!(instance instanceof ExtendedPlayer) || !(nbt instanceof CompoundNBT))
            return;
        ExtendedPlayer extendedPlayer = (ExtendedPlayer)instance;
        CompoundNBT extTagCompound = (CompoundNBT)nbt;
        extendedPlayer.readNBT(extTagCompound);
    }

    // ========== Write ==========
    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    @Override
    public INBT writeNBT(Capability<IExtendedPlayer> capability, IExtendedPlayer instance, Direction facing) {
        if(!(instance instanceof ExtendedPlayer))
            return null;
        ExtendedPlayer extendedPlayer = (ExtendedPlayer)instance;
        CompoundNBT extTagCompound = new CompoundNBT();
        extendedPlayer.writeNBT(extTagCompound);
        return extTagCompound;
    }
}
