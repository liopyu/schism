package com.lycanitesmobs.core.block.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.ElementManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class BlockShadowfire extends BlockFireBase {
    public boolean blindness;

	public BlockShadowfire(Block.Properties properties) {
		super(properties, LycanitesMobs.modInfo, "shadowfire");
		
		// Stats:
		this.tickRate = 30;
        this.dieInRain = false;
        this.triggerTNT = false;
        this.agingRate = 3;
        this.spreadChance = 0;
        this.removeOnTick = false;
        this.removeOnNoFireTick = false;
		this.blindness = true;
	}

    protected boolean canNeighborCatchFire(World worldIn, BlockPos pos) {
        return false;
    }

    protected int getNeighborEncouragement(World worldIn, BlockPos pos) {
        return 0;
    }

	public boolean canCatchFire(IBlockReader world, BlockPos pos, Direction face) {
        return false;
    }

	@Override
	public boolean isBlockFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		if(state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.CRYING_OBSIDIAN)
			return true;
		return true; // TODO Figure out why the PERMANENT property isn't working consistently.
	}

    protected boolean canDie(World world, BlockPos pos) {
        return false;
    }

    @Override
	public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
		super.entityInside(blockState, world, pos, entity);

		if(entity instanceof ItemEntity)
			return;

		if(entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)entity;

			Effect decay = ObjectManager.getEffect("decay");
			if(decay != null) {
				EffectInstance effect = new EffectInstance(decay, 5 * 20, 0);
				if(livingEntity.canBeAffected(effect))
					livingEntity.addEffect(effect);
			}

			EffectInstance blindness = new EffectInstance(Effects.BLINDNESS, 5 * 20, 0);
			if(this.blindness && livingEntity.canBeAffected(blindness)) {
				livingEntity.addEffect(blindness);
			}
		}

		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasElement(ElementManager.getInstance().getElement("shadow")))
			return;

		entity.hurt(DamageSource.WITHER, 1);
	}

    @Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		if (random.nextInt(100) == 0) {
			x = pos.getX() + random.nextFloat();
			z = pos.getZ() + random.nextFloat();
			world.addParticle(ParticleTypes.WITCH, x, y, z, 0.0D, 0.0D, 0.0D);
		}
		super.animateTick(state, world, pos, random);
	}
}