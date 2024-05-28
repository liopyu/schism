package com.lycanitesmobs.core.dispenser;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.Random;

public class BaseProjectileDispenseBehaviour extends ProjectileDispenseBehavior {
	protected ProjectileInfo projectileInfo;

	protected String oldProjectileName;
	protected Class<? extends BaseProjectileEntity> oldProjectileClass;

	// ==================================================
	//                      Constructor
	// ==================================================
	public BaseProjectileDispenseBehaviour(ProjectileInfo projectileInfo) {
		super();
		this.projectileInfo = projectileInfo;
	}

	public BaseProjectileDispenseBehaviour(Class<? extends BaseProjectileEntity> oldProjectileClass, String oldProjectileName) {
		super();
		this.oldProjectileClass = oldProjectileClass;
		this.oldProjectileName = oldProjectileName;
	}
	
	// ==================================================
	//                      Dispense
	// ==================================================
	@Override
    public ItemStack execute(IBlockSource blockSource, ItemStack stack) {
        World world = blockSource.getLevel();
        IPosition position = DispenserBlock.getDispensePosition(blockSource);
        Direction facing = blockSource.getBlockState().getValue(DispenserBlock.FACING);

		ProjectileEntity projectile = this.getProjectile(world, position, stack);
        if(projectile == null)
        	return stack;
        
        projectile.shoot((double)facing.getStepX(), (double)facing.getStepY(), (double)facing.getStepZ(), this.getPower(), this.getUncertainty());
        world.addFreshEntity(projectile);
        stack.split(1);
        
        return stack;
    }
    
	@Override
    protected ProjectileEntity getProjectile(World world, IPosition pos, ItemStack stack) {
		if(this.projectileInfo != null) {
			return this.projectileInfo.createProjectile(world, pos.x(), pos.y(), pos.z());
		}
		if(this.oldProjectileClass != null) {
			return ProjectileManager.getInstance().createOldProjectile(this.oldProjectileClass, world, pos.x(), pos.y(), pos.z());
		}
		return null;
	}

	@Override
	protected float getUncertainty()
	{
		return 0F;
	}

	@Override
	protected float getPower()
	{
		if(this.projectileInfo != null) {
			return (float)this.projectileInfo.velocity;
		}
		return 1.1F;
	}
    
    
	// ==================================================
	//                        Sound
	// ==================================================
	@Override
    protected void playSound(IBlockSource blockSource) {
        SoundEvent soundEvent = this.getDispenseSound();
        if(soundEvent == null || blockSource == null)
            return;
        blockSource.getLevel().playSound(null, blockSource.getPos(), soundEvent, SoundCategory.AMBIENT, 1.0F, 1.0F / (new Random().nextFloat() * 0.4F + 0.8F));
    }

    protected SoundEvent getDispenseSound() {
		if(this.projectileInfo != null) {
			return this.projectileInfo.getLaunchSound();
		}
		return ObjectManager.getSound(this.oldProjectileName);
    }
}