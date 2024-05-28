package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.WanderGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class EntityCockatrice extends RideableCreatureEntity implements IMob {

    protected WanderGoal wanderAI;
    protected AttackMeleeGoal attackAI;
    protected boolean wantsToLand;
    protected boolean  isLanded;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCockatrice(EntityType<? extends EntityCockatrice> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;
        this.flySoundSpeed = 20;
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
        EntityType vespidType = CreatureManager.getInstance().getEntityType("vespid");
        if(vespidType != null)
			this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(vespidType));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        // Land/Fly:
        if(!this.getCommandSenderWorld().isClientSide && !this.isNoAi()) {
            if(this.isLanded) {
                this.wantsToLand = false;
                if(this.hasPickupEntity() || this.getControllingPassenger() != null || this.isLeashed() || this.isInWater() || (!this.isTamed() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean())) {
                    this.leap(1.0D, 1.0D);
                    this.isLanded = false;
                }
            }
            else {
                if(this.wantsToLand) {
                    if(!this.isLanded && this.isSafeToLand()) {
                        this.isLanded = true;
                    }
                }
                else {
                    if (!this.hasPickupEntity() && !this.hasAttackTarget() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                        this.wantsToLand = true;
                    }
                }
            }
            if(this.hasPickupEntity() || this.getControllingPassenger() != null || this.hasAttackTarget() || this.isInWater()) {
                this.wantsToLand = false;
            }
            else if(this.isTamed() && !this.isLeashed()) {
                this.wantsToLand = true;
            }
        }

        // Random Leaping:
        if(!this.isTamed() && !this.getCommandSenderWorld().isClientSide) {
            if(this.hasAttackTarget()) {
                if(this.random.nextInt(10) == 0)
                    this.leap(2.0F, 1D, this.getTarget());
            }
            else {
                if(this.random.nextInt(50) == 0 && this.isMoving())
                    this.leap(1.0D, 1D);
            }
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Get Wander Position ==========
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        if(this.wantsToLand || !this.isLanded) {
            BlockPos groundPos;
            for(groundPos = wanderPosition.below(); groundPos.getY() > 0 && this.getCommandSenderWorld().getBlockState(groundPos).getBlock() == Blocks.AIR; groundPos = groundPos.below()) {}
            if(this.getCommandSenderWorld().getBlockState(groundPos).getMaterial().isSolid()) {
                return groundPos.above();
            }
        }
        if(this.hasPickupEntity() && this.getPickupEntity() instanceof PlayerEntity)
            wanderPosition = new BlockPos(wanderPosition.getX(), this.restrictYHeightFromGround(wanderPosition, 6, 14), wanderPosition.getZ());
        return wanderPosition;
    }

    // ========== Get Flight Offset ==========
    public double getFlightOffset() {
        if(!this.wantsToLand) {
            super.getFlightOffset();
        }
        return 0;
    }


	// ==================================================
	//                      Attacks
	// ==================================================
	// ========== Melee Attack ==========
	@Override
	public boolean attackMelee(Entity target, double damageScale) {
		if (!super.attackMelee(target, damageScale))
			return false;

		// Vespid Extermination:
		if (target instanceof EntityVespid) {
			target.remove();
		}

		return true;
	}

	// ========== Special Attack ==========
	public void specialAttack() {
		// Petrifying Caw:
		double distance = 5.0D;
		List<LivingEntity> possibleTargets = this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(distance, distance, distance), possibleTarget -> {
				if(!possibleTarget.isAlive()
						|| possibleTarget == EntityCockatrice.this
						|| EntityCockatrice.this.isEntityPassenger(possibleTarget, EntityCockatrice.this)
						|| EntityCockatrice.this.isAlliedTo(possibleTarget)
						|| !EntityCockatrice.this.canAttackType(possibleTarget.getType())
						|| !EntityCockatrice.this.canAttack(possibleTarget))
					return false;
			return true;
		});
		if(!possibleTargets.isEmpty()) {
			for(LivingEntity possibleTarget : possibleTargets) {
				boolean doDamage = true;
				if(this.getRider() instanceof PlayerEntity) {
					if(MinecraftForge.EVENT_BUS.post(new AttackEntityEvent((PlayerEntity)this.getRider(), possibleTarget))) {
						doDamage = false;
					}
				}
				if(doDamage) {
					if (ObjectManager.getEffect("paralysis") != null)
						possibleTarget.addEffect(new EffectInstance(ObjectManager.getEffect("paralysis"), this.getEffectDuration(5), 1));

					if (ObjectManager.getEffect("aphagia") != null)
						possibleTarget.addEffect(new EffectInstance(ObjectManager.getEffect("aphagia"), this.getEffectDuration(5), 1));
					else
						possibleTarget.addEffect(new EffectInstance(Effects.WEAKNESS, 10 * 20, 0));
				}
			}
		}
		this.playAttackSound();
		this.triggerAttackCooldown();
	}
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() {
        return !this.isLanded || this.hasPickupEntity();
    }
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
        return 100;
    }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }


    // ==================================================
    //                      Movement
    // ==================================================
    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getDimensions(Pose.STANDING).height * 0.7D;
    }

	@Override
	public double getMountedZOffset() {
		return -(double)this.getDimensions(Pose.STANDING).width * 0.01D;
	}


    // ==================================================
    //                   Mount Ability
    // ==================================================
	@Override
	public void mountAbility(Entity rider) {
		if(this.getCommandSenderWorld().isClientSide)
			return;

		if(this.abilityToggled)
			return;
		if(this.getStamina() < this.getStaminaCost())
			return;

		this.specialAttack();
		this.applyStaminaCost();
	}

	public float getStaminaCost() {
		return 100;
	}

	public int getStaminaRecoveryWarmup() {
		return 5 * 20;
	}

	public float getStaminaRecoveryMax() {
		return 1.0F;
	}
}
