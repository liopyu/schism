package com.lycanitesmobs.core.block.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class BlockScorchfire extends BlockFireBase {
	
	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockScorchfire(Block.Properties properties) {
		super(properties, LycanitesMobs.modInfo, "scorchfire");

        // Stats:
        this.tickRate = 30;
        this.dieInRain = false;
        this.triggerTNT = false;
        this.agingRate = 6;
        this.spreadChance = 1;
		this.removeOnTick = false;
		this.removeOnNoFireTick = false;
	}


    // ==================================================
    //                       Break
    // ==================================================
    /*@Override
    public Item getItemDropped(BlockState state, Random random, int zero) {
        return ObjectManager.getItem("scorchfirecharge");
    }*/
    
    
	// ==================================================
	//                Collision Effects
	// ==================================================
    @Override
	public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
		super.entityInside(blockState, world, pos, entity);

		if(entity instanceof LivingEntity) {
			Effect penetration = ObjectManager.getEffect("penetration");
			if(penetration != null) {
				EffectInstance effect = new EffectInstance(penetration, 3 * 20, 0);
				LivingEntity entityLiving = (LivingEntity)entity;
				if(entityLiving.canBeAffected(effect))
					entityLiving.addEffect(effect);
			}
		}

		if(entity instanceof ItemEntity)
			if(((ItemEntity)entity).getItem().getItem() == ObjectManager.getItem("scorchfirecharge"))
				return;

		if(entity.isInvulnerableTo(DamageSource.IN_FIRE))
			return;

		entity.hurt(DamageSource.IN_FIRE, 1);
		entity.setSecondsOnFire(3);
	}


    // ==================================================
    //                      Particles
    // ==================================================
    @Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		if (random.nextInt(100) == 0) {
			x = pos.getX() + random.nextFloat();
			z = pos.getZ() + random.nextFloat();
			world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
			world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
		}
		super.animateTick(state, world, pos, random);
	}
}