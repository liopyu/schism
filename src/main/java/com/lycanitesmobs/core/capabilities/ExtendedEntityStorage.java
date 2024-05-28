package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.core.entity.ExtendedEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class ExtendedEntityStorage implements Capability.IStorage<IExtendedEntity> {

    // ==================================================
    //                        NBT
    // ==================================================
    // ========== Read ===========
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    @Override
    public void readNBT(Capability<IExtendedEntity> capability, IExtendedEntity instance, Direction facing, INBT nbt) {
        if(!(instance instanceof ExtendedEntity) || !(nbt instanceof CompoundNBT))
            return;
        ExtendedEntity extendedEntity = (ExtendedEntity)instance;
        CompoundNBT extTagCompound = (CompoundNBT)nbt;
        extendedEntity.readNBT(extTagCompound);
    }

    // ========== Write ==========
    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    @Override
    public INBT writeNBT(Capability<IExtendedEntity> capability, IExtendedEntity instance, Direction facing) {
        if(!(instance instanceof ExtendedEntity))
            return null;
        ExtendedEntity extendedEntity = (ExtendedEntity)instance;
        CompoundNBT extTagCompound = new CompoundNBT();
        extendedEntity.writeNBT(extTagCompound);
        return extTagCompound;
    }
}