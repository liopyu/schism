package com.schism.core.projectiles.actions;

import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.particles.DefinedParticleType;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import com.schism.core.util.Vec3;
import net.minecraft.core.particles.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class ParticlesProjectileAction extends AbstractProjectileAction
{
    protected BlockRegistryObject<ParticleType<?>> cachedParticleType;
    protected BlockRegistryObject<Block> cachedParticleBlock;
    protected BlockRegistryObject<ParticleType<?>> cachedFluidParticleType;
    protected BlockRegistryObject<Block> cachedFluidParticleBlock;
    protected final int intervalTicks;
    protected final float chance;
    protected final float offsetMin;
    protected final float offsetMax;
    protected final float forceMin;
    protected final float forceMax;
    protected final int countMin;
    protected final int countMax;

    public ParticlesProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedParticleType = new BlockRegistryObject<>(dataStore.stringProp("type_id"), () -> ForgeRegistries.PARTICLE_TYPES);
        this.cachedParticleBlock = new BlockRegistryObject<>(dataStore.stringProp("block_id"), () -> ForgeRegistries.BLOCKS);
        this.cachedFluidParticleType = new BlockRegistryObject<>(dataStore.stringProp("fluid_type_id"), () -> ForgeRegistries.PARTICLE_TYPES);
        this.cachedFluidParticleBlock = new BlockRegistryObject<>(dataStore.stringProp("fluid_block_id"), () -> ForgeRegistries.BLOCKS);
        this.intervalTicks = dataStore.intProp("interval_ticks");
        this.chance = dataStore.floatProp("chance");
        this.offsetMin = dataStore.floatProp("offset_min");
        this.offsetMax = dataStore.floatProp("offset_max");
        this.forceMin = dataStore.floatProp("force_min");
        this.forceMax = dataStore.floatProp("force_max");
        this.countMin = dataStore.intProp("count_min");
        this.countMax = dataStore.intProp("count_max");
    }

    @Override
    public void onTick(ProjectileEntity projectile)
    {
        if (!projectile.getLevel().isClientSide()) {
            return;
        }
        if (this.intervalTicks > 0 && projectile.tickCount() - 1 % this.intervalTicks != 0) {
            return;
        }

        // Get Particle Type:
        ParticleType<?> particleType = projectile.isInWater() ? this.cachedFluidParticleType.get() : this.cachedParticleType.get();
        if (particleType == null) {
            return;
        }

        // Chance:
        if (this.chance < 1 && projectile.getLevel().getRandom().nextFloat() > this.chance) {
            return;
        }

        // Get Particle Options:
        ParticleOptions particleOptions;
        if (particleType instanceof SimpleParticleType) {
            particleOptions = (SimpleParticleType)particleType;
        } else if (particleType instanceof DefinedParticleType) {
            particleOptions = (DefinedParticleType)particleType;
        } else if (particleType == ParticleTypes.BLOCK && this.cachedParticleBlock.isPresent()) {
            BlockRegistryObject<Block> cachedBlock = projectile.isInWater() ? this.cachedFluidParticleBlock : this.cachedParticleBlock;
            if (cachedBlock.isEmpty()) {
                return;
            }
            particleOptions = new BlockParticleOption((ParticleType<BlockParticleOption>) particleType, cachedBlock.get().defaultBlockState());
        } else {
            return;
        }

        // Create Particles:
        Random random = projectile.getLevel().getRandom();
        int count = this.countMax > this.countMin ? random.nextInt(this.countMin, this.countMax + 1) : this.countMin;
        float offset = this.offsetMax > this.offsetMin ? random.nextFloat(this.offsetMin, this.offsetMax) : this.offsetMin;
        float force = this.forceMax > this.forceMin ? random.nextFloat(this.forceMin, this.forceMax) : this.forceMin;
        for (int i = 0; i < count; i++) {
            Vec3 particlePos = new Vec3(projectile.position());
            if (offset > 0) {
                particlePos = particlePos.add(random.nextFloat(-offset, offset), random.nextFloat(0, offset), random.nextFloat(-offset, offset));
            }
            Vec3 particleForce = Vec3.ZERO;
            if (force > 0) {
                particleForce = particleForce.add(random.nextFloat(-force, force), random.nextFloat(-force, force), random.nextFloat(-force, force));
            }
            projectile.getLevel().addParticle(particleOptions, particlePos.x(), particlePos.y(), particlePos.z(), particleForce.x(), particleForce.y(), particleForce.z());
        }
    }
}
