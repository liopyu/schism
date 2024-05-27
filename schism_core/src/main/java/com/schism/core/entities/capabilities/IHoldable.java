package com.schism.core.entities.capabilities;

import com.schism.core.util.Vec3;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public interface IHoldable
{
    /**
     * Called when the host entity does an update tick.
     */
    void tick();

    /**
     * Gets the entity holding this holdable, if any.
     * @return An optional entity that is holding this holdable.
     */
    Optional<LivingEntity> holderEntity();

    /**
     * Sets the entity that is holding this holdable, or clears it if null.
     * @param holderEntity The entity to hold this holdable or null to drop.
     * @param offset The holding offset to use, this is relative to the holding entity's position. Also acts as the dropping location.
     * @return True if the holdable was picked up successfully, false otherwise.
     */
    boolean setHolderEntity(LivingEntity holderEntity, Vec3 offset);

    /**
     * Gets the entity this holdable is holding, if any.
     * @return An optional entity that is this holdable is holding.
     */
    Optional<LivingEntity> holdingEntity();

    /**
     * Attempts to pick up the target entity.
     * @param pickupEntity The entity to pickup or null to drop.
     * @param offset The holding offset to use, this is relative to the holding entity's position. Also acts as the dropping location.
     * @return True if the entity was picked up successfully, false otherwise.
     */
    boolean pickup(LivingEntity pickupEntity, Vec3 offset);
}
