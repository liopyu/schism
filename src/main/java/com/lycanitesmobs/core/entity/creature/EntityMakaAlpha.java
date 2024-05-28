package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendEntitiesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityMakaAlpha extends AgeableCreatureEntity {
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityMakaAlpha(EntityType<? extends EntityMakaAlpha> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;
        this.attackCooldownMax = 10;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(this.getClass()));
		this.targetSelector.addGoal(this.nextSpecialTargetIndex++, new DefendEntitiesGoal(this, EntityMaka.class));

		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(PlayerEntity.class).setLongMemory(false));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }
	
	
	// ==================================================
  	//                      Update
  	// ==================================================
	@Override
	public void aiStep() {
		super.aiStep();
		
		// Alpha Sparring Cooldown:
		if(this.hasAttackTarget() && this.getTarget() instanceof EntityMakaAlpha) {
			if(this.getHealth() / this.getMaxHealth() <= 0.25F || this.getTarget().getHealth() / this.getTarget().getMaxHealth() <= 0.25F) {
				this.setTarget(null);
			}
		}
	}

	@Override
	public boolean isProtective(Entity entity) {
		if(entity instanceof EntityMaka) {
			return true;
		}
		return super.isProtective(entity);
	}
	
	
	// ==================================================
   	//                      Movement
   	// ==================================================
    // ========== Pathing Weight ==========
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(blockState.getMaterial() == Material.GRASS)
                return 10F;
            if(blockState.getMaterial() == Material.DIRT)
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed(PlayerEntity player) {
        return true;
    }

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
	// ==================================================
   	//                      Attacks
   	// ==================================================
	@Override
    public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityMaka)
			return false;
    	if(target instanceof EntityMakaAlpha && (this.getHealth() / this.getMaxHealth() <= 0.25F || target.getHealth() / target.getMaxHealth() <= 0.25F))
    		return false;
    	return super.canAttack(target);
    }

	@Override
	public boolean canAttackOwnSpecies() {
		return true;
	}

    @Override
    public void setTarget(LivingEntity entity) {
    	if(entity == null && this.getTarget() instanceof EntityMakaAlpha) {
    		this.heal((this.getMaxHealth() - this.getHealth()) / 2);
    		this.addEffect(new EffectInstance(Effects.REGENERATION, 20 * 20, 2, false, false));
			this.getTarget().heal((this.getMaxHealth() - this.getHealth()) / 2);
			this.getTarget().addEffect(new EffectInstance(Effects.REGENERATION, 20 * 20, 2, false, false));
    	}
    	super.setTarget(entity);
    }

	@Override
	public boolean rollAttackTargetChance(LivingEntity target) {
    	if(target instanceof PlayerEntity || target.getType() == this.getType())
    		return this.getRandom().nextDouble() <= 0.01D;
		return true;
	}
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        float damageMod = super.getDamageModifier(damageSrc);
        if(damageSrc.getEntity() instanceof EntityMakaAlpha)
            damageMod *= 2;
        return damageMod;
    }
    
    
    // ==================================================
    //                     Breeding
    // ==================================================
    // ========== Create Child ==========
	@Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		return (AgeableCreatureEntity) CreatureManager.getInstance().getCreature("maka").createEntity(this.getCommandSenderWorld());
	}
}
