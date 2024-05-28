package com.lycanitesmobs.core.block.fluid;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock.Properties;

public class PoisonFluidBlock extends BaseFluidBlock {
	public PoisonFluidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier, properties, name, element, destroyItems);
	}

    public boolean shouldSpreadLiquid(World world, BlockPos neighborBlockPos, BlockState blockState) {
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);

        // Freeze Water:
        if (neighborBlockState.getMaterial() == Material.WATER) {
            world.setBlock(neighborBlockPos, Blocks.ANDESITE.defaultBlockState(), 4);
            return false;
        }

        // Freeze Lava:
        if (neighborBlockState.getMaterial() == Material.LAVA) {
            world.setBlock(neighborBlockPos, Blocks.GRANITE.defaultBlockState(), 4);
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
        if(entity instanceof LivingEntity) {
            Effect effect = ObjectManager.getEffect("plague");
            if(effect != null) {
                ((LivingEntity) entity).addEffect(new EffectInstance(effect, 5 * 20, 0));
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
