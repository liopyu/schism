package com.schism.core.entities.capabilities;

import com.schism.core.creatures.CreatureDefinition;
import com.schism.core.util.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

public class HoldableBackend implements IHoldable, INBTSerializable<CompoundTag>
{
    protected final LivingEntity livingEntity;
    protected LivingEntity holderEntity;
    protected Vec3 holderOffset;
    protected LivingEntity holdingEntity;

    /**
     * Provides entity holding based logic for being picked up or picking up other entities.
     * @param livingEntity The living entity to attach to.
     */
    public HoldableBackend(LivingEntity livingEntity, CreatureDefinition creatureDefinition)
    {
        this.livingEntity = livingEntity;
    }

    @Override
    public void tick()
    {
        // TODO Update position relative to holder.
    }

    @Override
    public Optional<LivingEntity> holderEntity()
    {
        return Optional.ofNullable(this.holderEntity);
    }

    @Override
    public boolean setHolderEntity(LivingEntity holderEntity, Vec3 offset)
    {
        // Drop:
        if (holderEntity == null) {
            this.holderEntity = null;
            // TODO Set position.
            return true;
        }

        // Holder Validation:
        if (holderEntity == this.livingEntity) {
            return false;
        }

        // Pickup:
        this.holderEntity = holderEntity;
        this.holderOffset = offset;
        return true;
    }

    @Override
    public Optional<LivingEntity> holdingEntity()
    {
        return Optional.ofNullable(this.holdingEntity);
    }

    @Override
    public boolean pickup(LivingEntity pickupEntity, Vec3 offset)
    {
        // TODO Get pickupEntity IHoldable capability and call it.
        return false;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();
        if (this.holderEntity().isPresent()) {
            compoundTag.putUUID("holderUUID", this.holderEntity().get().getUUID());
            compoundTag.putDouble("holderOffsetX", this.holderOffset.x());
            compoundTag.putDouble("holderOffsetY", this.holderOffset.y());
            compoundTag.putDouble("holderOffsetZ", this.holderOffset.z());
        }
        if (this.holdingEntity().isPresent()) {
            compoundTag.putUUID("holdingUUID", this.holdingEntity().get().getUUID());
        }
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag)
    {
        if (compoundTag.contains("holderUUID")) {
            LivingEntity holderEntity = this.livingEntity.getLevel().getEntities(this.livingEntity, livingEntity.getBoundingBox().inflate(10), entity -> {
                return entity.getUUID() == compoundTag.getUUID("holderUUID");
            }).stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).findFirst().orElse(null);
            if (holderEntity != null) {
                this.setHolderEntity(holderEntity, new Vec3(compoundTag.getDouble("holderOffsetX"), compoundTag.getDouble("holderOffsetY"), compoundTag.getDouble("holderOffsetZ")));
            }
        }
        if (compoundTag.contains("holdingUUID")) {
            this.holdingEntity = this.livingEntity.getLevel().getEntities(this.livingEntity, livingEntity.getBoundingBox().inflate(10), entity -> {
                return entity.getUUID() == compoundTag.getUUID("holderUUID");
            }).stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).findFirst().orElse(null);
        }
    }
}
