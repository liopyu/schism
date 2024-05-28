package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.EffectBase;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

public class EntityEechetik extends TameableCreatureEntity implements IMob {

	public int myceliumRadius = 2;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityEechetik(EntityType<? extends EntityEechetik> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.ARTHROPOD;
        this.hasAttackSound = true;
        
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }

	@Override
	public void loadCreatureFlags() {
		this.myceliumRadius = this.creatureInfo.getFlag("myceliumRadius", this.myceliumRadius);
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

		// Plague Aura Attack:
		if(!this.getCommandSenderWorld().isClientSide && this.updateTick % 40 == 0 && this.hasAttackTarget()) {
			EffectBase plague = ObjectManager.getEffect("plague");
			if(plague != null) {
				EffectInstance potionEffect = new EffectInstance(plague, this.getEffectDuration(2), 1);
				List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 2);
				for(Object entityObj : aoeTargets) {
					LivingEntity target = (LivingEntity) entityObj;
					if (target != this && this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().canSee(target) && target.canBeAffected(potionEffect)) {
						target.addEffect(potionEffect);
					}
				}
			}
		}

		// Grow Mycelium:
		if(!this.getCommandSenderWorld().isClientSide && this.updateTick % 100 == 0 && this.myceliumRadius > 0 && !this.isTamed() && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			int range = this.myceliumRadius;
			for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w++) {
				for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d++) {
					for (int h = -((int) Math.ceil(this.getDimensions(Pose.STANDING).height) + range); h <= Math.ceil(this.getDimensions(Pose.STANDING).height); h++) {
						BlockPos blockPos = this.blockPosition().offset(w, h, d);
						BlockState blockState = this.getCommandSenderWorld().getBlockState(blockPos);
						BlockState upperBlockState = this.getCommandSenderWorld().getBlockState(blockPos.above());
						if (upperBlockState.getBlock() == Blocks.AIR && blockState.getBlock() == Blocks.DIRT) {
							this.getCommandSenderWorld().setBlockAndUpdate(blockPos, Blocks.MYCELIUM.defaultBlockState());
						}
					}
				}
			}
		}

		// Particles:
		if(this.getCommandSenderWorld().isClientSide) {
			for(int i = 0; i < 2; ++i) {
				this.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width * 2, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width * 2, 0.0D, 0.0D, 0.0D);
				this.getCommandSenderWorld().addParticle(ParticleTypes.MYCELIUM, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width * 2, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width * 2, 0.0D, 0.0D, 0.0D);
			}
		}
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;
        return true;
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
	@Override
	public boolean isFlying() { return true; }

	@Override
	public boolean isStrongSwimmer() { return true; }
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
    // ==================================================
   	//                    Taking Damage
   	// ==================================================
	// ========== Damage Modifier ==========
	public float getDamageModifier(DamageSource damageSrc) {
		if(damageSrc.isFire())
			return 0F;
		else return super.getDamageModifier(damageSrc);
	}
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
    	if(type.equals("inWall")) return false;
    	return super.isVulnerableTo(type, source, damage);
    }

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}
}
