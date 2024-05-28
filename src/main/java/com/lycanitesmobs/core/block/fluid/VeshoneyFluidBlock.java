package com.lycanitesmobs.core.block.fluid;

import com.lycanitesmobs.core.entity.creature.EntityVespid;
import com.lycanitesmobs.core.entity.creature.EntityVespidQueen;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.Supplier;

public class VeshoneyFluidBlock extends BaseFluidBlock {
    public VeshoneyFluidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier, properties, name, element, destroyItems);
    }

    public boolean shouldSpreadLiquid(World world, BlockPos neighborBlockPos, BlockState blockState) {
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);

        // Water Dirt:
        if (neighborBlockState.getMaterial() == Material.WATER) {
            world.setBlock(neighborBlockPos, Blocks.DIRT.defaultBlockState(), 4);
            return false;
        }

        // Lava Gravel:
        if (neighborBlockState.getMaterial() == Material.LAVA) {
            world.setBlock(neighborBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 4);
            return false;
        }

        return super.shouldSpreadLiquid(world, neighborBlockPos, blockState);
    }

    @Override
    public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
        // Extinguish:
        if(entity.isOnFire())
            entity.clearFire();

        // Effects:
        if(entity instanceof LivingEntity && !(entity instanceof EntityVespid) && !(entity instanceof EntityVespidQueen)) {
            if (!(entity instanceof PlayerEntity) || !((PlayerEntity)entity).isCreative() || !entity.isSpectator()) {
                entity.makeStuckInBlock(blockState, new Vector3d(0.3D, 0.6D, 0.3D));
                entity.setDeltaMovement(0, -0.02, 0);
            }
        }

        super.entityInside(blockState, world, pos, entity);
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        float f;
        float f1;
        float f2;

        if (random.nextInt(100) == 0) {
            f = (float)pos.getX() + random.nextFloat();
            f1 = (float)pos.getY() + random.nextFloat() * 0.5F;
            f2 = (float)pos.getZ() + random.nextFloat();
            world.addParticle(ParticleTypes.RAIN, (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
        }
        super.animateTick(state, world, pos, random);
    }
}
