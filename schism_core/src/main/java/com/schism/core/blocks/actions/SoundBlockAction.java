package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.database.registryobjects.SoundRegistryObject;
import com.schism.core.resolvers.SoundEventResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class SoundBlockAction extends AbstractBlockAction
{
    protected final SoundRegistryObject<SoundEvent> cachedSoundEvent;
    protected final int tickInterval;
    protected final float chance;
    protected final float volumeMin;
    protected final float volumeMax;
    protected final float pitchMin;
    protected final float pitchMax;

    public SoundBlockAction(BlockDefinition definition, DataStore dataStore)
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

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random)
    {
        if (this.tickInterval > 0 && level.getGameTime() % this.tickInterval != 0) {
            return;
        }
        if (!this.cachedSoundEvent.isPresent()) {
            return;
        }
        if (this.chance < 1 && random.nextFloat() > this.chance) {
            return;
        }

        double x = blockPos.getX() + 0.5D;
        double y = blockPos.getY() + 0.5D;
        double z = blockPos.getZ() + 0.5D;
        float volume = this.volumeMax > this.volumeMin ? random.nextFloat(this.volumeMin, this.volumeMax) : this.volumeMin;
        float pitch = this.pitchMax > this.pitchMin ? random.nextFloat(this.pitchMin, this.pitchMax) : this.pitchMin;
        level.playLocalSound(x, y, z, this.cachedSoundEvent.get(), SoundSource.BLOCKS, volume, pitch, false);
    }
}
