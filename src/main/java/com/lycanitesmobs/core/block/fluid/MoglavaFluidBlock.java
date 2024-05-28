package com.lycanitesmobs.core.block.fluid;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Supplier;

public class MoglavaFluidBlock extends BaseFluidBlock {
	public MoglavaFluidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Block.Properties properties, String name, ElementInfo element, boolean destroyItems) {
		super(fluidSupplier, properties, name, element, destroyItems);
	}

	@Override
	public boolean shouldSpreadLiquid(World world, BlockPos neighborBlockPos, BlockState blockState) {
		BlockState neighborBlockState = world.getBlockState(neighborBlockPos);

        // Water Cobblestone:
		if (neighborBlockState.getMaterial() == Material.WATER) {
			world.setBlock(neighborBlockPos, Blocks.STONE.defaultBlockState(), 2);
			return false;
        }

		return super.shouldSpreadLiquid(world, neighborBlockPos, blockState);
	}

	@Override
	public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
		if(entity instanceof ItemEntity)
			entity.hurt(DamageSource.LAVA, 10F);
		super.entityInside(blockState, world, pos, entity);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		float f;
		float f1;
		float f2;

		if (random.nextInt(100) == 0) {
			f = (float)pos.getX() + random.nextFloat();
			f1 = (float)pos.getY() + random.nextFloat() * 0.5F;
			f2 = (float)pos.getZ() + random.nextFloat();
			world.addParticle(ParticleTypes.LAVA, (double)f, (double)f1, (double)f2, 0.0D, 0.0D, 0.0D);
		}
		super.animateTick(state, world, pos, random);
    }
}
