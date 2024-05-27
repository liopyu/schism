package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.particles.DefinedParticleType;
import com.schism.core.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class ParticlesBlockAction extends AbstractBlockAction
{
    protected CachedRegistryObject<ParticleType<?>> cachedParticleType;
    protected CachedRegistryObject<Block> cachedParticleBlock;
    protected final int intervalTicks;
    protected final float chance;
    protected final float offsetMin;
    protected final float offsetMax;
    protected final float forceMin;
    protected final float forceMax;
    protected final int countMin;
    protected final int countMax;

    public ParticlesBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedParticleType = new CachedRegistryObject<>(dataStore.stringProp("type_id"), () -> ForgeRegistries.PARTICLE_TYPES);
        this.cachedParticleBlock = new CachedRegistryObject<>(dataStore.stringProp("block_id"), () -> ForgeRegistries.BLOCKS);
        this.intervalTicks = dataStore.intProp("interval_ticks");
        this.chance = dataStore.floatProp("chance");
        this.offsetMin = dataStore.floatProp("offset_min");
        this.offsetMax = dataStore.floatProp("offset_max");
        this.forceMin = dataStore.floatProp("force_min");
        this.forceMax = dataStore.floatProp("force_max");
        this.countMin = dataStore.intProp("count_min");
        this.countMax = dataStore.intProp("count_max");
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random)
    {
        if (this.intervalTicks > 0 && level.getGameTime() % this.intervalTicks != 0) {
            return;
        }

        // Get Particle Type:
        ParticleType<?> particleType = this.cachedParticleType.get();
        if (particleType == null) {
            return;
        }
    
        // Chance:
        if (this.chance < 1 && random.nextFloat() > this.chance) {
            return;
        }

        // Get Particle Options:
        ParticleOptions particleOptions;
        if (particleType instanceof SimpleParticleType) {
            particleOptions = (SimpleParticleType)particleType;
        } else if (particleType instanceof DefinedParticleType) {
            particleOptions = (DefinedParticleType)particleType;
        } else if (particleType == ParticleTypes.BLOCK && this.cachedParticleBlock.isPresent()) {
            particleOptions = new BlockParticleOption((ParticleType<BlockParticleOption>) particleType, this.cachedParticleBlock.get().defaultBlockState());
        } else {
            return;
        }

        // Create Particles:
        int count = this.countMax > this.countMin ? random.nextInt(this.countMin, this.countMax + 1) : this.countMin;
        float offset = this.offsetMax > this.offsetMin ? random.nextFloat(this.offsetMin, this.offsetMax) : this.offsetMin;
        float force = this.forceMax > this.forceMin ? random.nextFloat(this.forceMin, this.forceMax) : this.forceMin;
        for (int i = 0; i < count; i++) {
            Vec3 particlePos = new Vec3(blockPos).add(0.5F);
            if (offset > 0) {
                particlePos = particlePos.add(random.nextFloat(-offset, offset), random.nextFloat(0, offset), random.nextFloat(-offset, offset));
            }
            Vec3 particleForce = Vec3.ZERO;
            if (force > 0) {
                particleForce = particleForce.add(random.nextFloat(-force, force), random.nextFloat(-force, force), random.nextFloat(-force, force));
            }
            level.addParticle(particleOptions, particlePos.x(), particlePos.y(), particlePos.z(), particleForce.x(), particleForce.y(), particleForce.z());
        }
    }
}
