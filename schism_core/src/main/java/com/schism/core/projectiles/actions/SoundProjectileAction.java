package com.schism.core.projectiles.actions;

import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import com.schism.core.resolvers.SoundEventResolver;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundProjectileAction extends AbstractProjectileAction
{
    protected final BlockRegistryObject<SoundEvent> cachedSoundEvent;
    protected final int tickInterval;
    protected final float chance;
    protected final float volumeMin;
    protected final float volumeMax;
    protected final float pitchMin;
    protected final float pitchMax;

    public SoundProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedSoundEvent = SoundEventResolver.get().fromId(dataStore.stringProp("sound"));
        this.tickInterval = dataStore.intProp("tick_interval");
        this.chance = dataStore.floatProp("chance");
        this.volumeMin = dataStore.floatProp("volume_min");
        this.volumeMax = dataStore.floatProp("volume_max");
        this.pitchMin = dataStore.floatProp("pitch_min");
        this.pitchMax = dataStore.floatProp("pitch_max");
    }

    @Override
    public void onTick(ProjectileEntity projectile)
    {
        if (!projectile.getLevel().isClientSide()) {
            return;
        }
        if (this.tickInterval > 0 && projectile.tickCount() - 1 % this.tickInterval != 0) {
            return;
        }
        if (!this.cachedSoundEvent.isPresent()) {
            return;
        }
        if (this.chance < 1 && projectile.getLevel().getRandom().nextFloat() > this.chance) {
            return;
        }

        float volume = this.volumeMax > this.volumeMin ? projectile.getLevel().getRandom().nextFloat(this.volumeMin, this.volumeMax) : this.volumeMin;
        float pitch = this.pitchMax > this.pitchMin ? projectile.getLevel().getRandom().nextFloat(this.pitchMin, this.pitchMax) : this.pitchMin;
        projectile.getLevel().playLocalSound(projectile.position().x(), projectile.position().y(), projectile.position().z(), this.cachedSoundEvent.get(), SoundSource.AMBIENT, volume, pitch, false);
    }
}
