package com.lycanitesmobs.core.block.fluid;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.Supplier;

public class BaseFluidBlock extends FlowingFluidBlock {
	public String blockName;
	protected ElementInfo element;
	protected boolean destroyItems = true;

	public BaseFluidBlock(Supplier<? extends FlowingFluid> fluidSupplier, Properties properties, String name, ElementInfo element, boolean destroyItems) {
        super(fluidSupplier, properties);
        this.setRegistryName(LycanitesMobs.MODID, name);
        this.blockName = name;
        this.element = element;
        this.destroyItems = destroyItems;
	}

	public ElementInfo getElement() {
		return this.element;
	}

	@Override
    public void neighborChanged(BlockState blockState, World world, BlockPos blockPos, Block neighborBlock, BlockPos neighborBlockPos, boolean someBoolean) {
	    super.neighborChanged(blockState, world, blockPos, neighborBlock, neighborBlockPos, someBoolean);
		if (neighborBlock == this) {
	        return;
        }
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);
		if (neighborBlockState.getBlock() == this) {
		    return;
        }
        if (this.shouldSpreadLiquid(world, neighborBlockPos, blockState)) {
            world.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.getFluid().getTickDelay(world));
        }
    }

    public boolean shouldSpreadLiquid(World world, BlockPos neighborBlockPos, BlockState blockState) {
        BlockState neighborBlockState = world.getBlockState(neighborBlockPos);
        if (neighborBlockState.getMaterial().isLiquid()) {
            return false;
        }
        return true;
    }

    @Override
    public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
		if (this.destroyItems && (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity)) {
			entity.kill();
		}
        super.entityInside(blockState, world, pos, entity);
    }

	/** Client side animation and sounds. **/
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		if(random.nextInt(52) == 0) {
			world.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, ObjectManager.getSound(this.blockName), SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
		}
		super.animateTick(state, world, pos, random);
    }
}
