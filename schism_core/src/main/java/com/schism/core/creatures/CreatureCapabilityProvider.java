package com.schism.core.creatures;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreatureCapabilityProvider implements ICapabilitySerializable<CompoundTag>
{
    public static final Capability<ICreature> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    protected final CreatureBackend capabilityBackend;
    protected final LazyOptional<ICreature> lazyCapability;

    /**
     * Provides a Capability with the Creature Capability Backend to a Living Entity with a Creature Definition.
     * @param livingEntity The living entity to provide a creature capability to.
     * @param creatureDefinition The creature definition to use.
     */
    public CreatureCapabilityProvider(LivingEntity livingEntity, CreatureDefinition creatureDefinition)
    {
        this.capabilityBackend = new CreatureBackend(livingEntity, creatureDefinition);
        this.lazyCapability = LazyOptional.of(() -> this.capabilityBackend);
    }

    /**
     * Gets the capability backend created by this provider.
     * @return The capability backend.
     */
    public CreatureBackend backend()
    {
        return this.capabilityBackend;
    }

    /**
     * Gets the lazy optional capability created by this provider.
     * @return The lazy optional capability.
     */
    public LazyOptional<ICreature> lazyCapability()
    {
        return this.lazyCapability;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction)
    {
        if (capability == CAPABILITY) {
            return this.lazyCapability.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return this.capabilityBackend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.capabilityBackend.deserializeNBT(nbt);
    }
}
