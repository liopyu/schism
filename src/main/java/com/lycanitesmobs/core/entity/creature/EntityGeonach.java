package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.api.IFusable;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ObjectLists;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityGeonach extends TameableCreatureEntity implements IMob, IFusable {
	
	public int blockBreakRadius = 0;

	public float fireDamageAbsorbed = 0;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityGeonach(EntityType<? extends EntityGeonach> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;
        
        this.setupMob();

        this.maxUpStep = 1.0F;
        this.attackPhaseMax = 3;
        this.setAttackCooldownMax(10);
    }

    @Override
    protected void registerGoals() {
		this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.SILVERFISH));
		super.registerGoals();
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }

	@Override
	public void loadCreatureFlags() {
		this.blockBreakRadius = this.creatureInfo.getFlag("blockBreakRadius", this.blockBreakRadius);
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        if(!this.getCommandSenderWorld().isClientSide) {
			if (this.isRareVariant() && !this.isPetType("familiar")){
				// Random Charging:
				if (this.hasAttackTarget() && this.distanceTo(this.getTarget()) > 1 && this.getRandom().nextInt(20) == 0) {
					if (this.position().y() - 1 > this.getTarget().position().y())
						this.leap(6.0F, -1.0D, this.getTarget());
					else if (this.position().y() + 1 < this.getTarget().position().y())
						this.leap(6.0F, 1.0D, this.getTarget());
					else
						this.leap(6.0F, 0D, this.getTarget());
					if (this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.blockBreakRadius > -1 && !this.isTamed()) {
						this.destroyArea((int) this.position().x(), (int) this.position().y(), (int) this.position().z(), 10, true, this.blockBreakRadius);
					}
				}
			}

			// Environmental Transformation:
			if(!this.isTamed() && !this.isRareVariant()) {
				if (this.updateTick % 40 == 0 && this.isInLava()) {
					this.transform(CreatureManager.getInstance().getEntityType("volcan"), null, false);
				}
				if (this.fireDamageAbsorbed >= 10) {
					this.transform(CreatureManager.getInstance().getEntityType("volcan"), null, false);
				}
			}
		}

        // Particles:
        if(this.getCommandSenderWorld().isClientSide && !CreatureManager.getInstance().config.disableBlockParticles)
            for(int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        this.position().x() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width,
                        this.position().y() + this.random.nextDouble() * (double) this.getDimensions(Pose.STANDING).height,
                        this.position().z() + (this.random.nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width,
                        0.0D, 0.0D, 0.0D);
            }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
        // Silverfish Extermination:
        if(this.hasAttackTarget() && this.getTarget() instanceof SilverfishEntity)
            return 4.0F;
        return super.getAISpeedModifier();
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;

        // Silverfish Extermination:
        if(target instanceof SilverfishEntity) {
            target.remove();
        }

		this.nextAttackPhase();
        return true;
    }

	@Override
	public int getMeleeCooldown() {
		if(this.getAttackPhase() == 2)
			return super.getMeleeCooldown() * 3;
		return super.getMeleeCooldown();
	}

	@Override
	public int getRangedCooldown() {
		if(this.getAttackPhase() == 2)
			return super.getRangedCooldown() * 3;
		return super.getRangedCooldown();
	}
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() { return true; }
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }
    
    
    // ==================================================
   	//                    Taking Damage
   	// ==================================================
    // ========== Damage Modifier ==========
    @Override
    public float getDamageModifier(DamageSource damageSrc) {
		if(damageSrc.getEntity() != null) {
			ItemStack heldItem = ItemStack.EMPTY;
			if(damageSrc.getEntity() instanceof LivingEntity) {
				LivingEntity entityLiving = (LivingEntity)damageSrc.getEntity();
				if(!entityLiving.getItemInHand(Hand.MAIN_HAND).isEmpty()) {
					heldItem = entityLiving.getItemInHand(Hand.MAIN_HAND);
				}
			}
			if(ObjectLists.isPickaxe(heldItem)) {
				return 3.0F;
			}
		}
		return super.getDamageModifier(damageSrc);
    }

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
    	if(type.equals("cactus") || type.equals("inWall"))
    		return false;
    	if(source.isFire()) {
    		this.fireDamageAbsorbed += damage;
    		return false;
		}
		return super.isVulnerableTo(type, source, damage);
    }
    
    @Override
    public boolean canBurn() {
    	// Geonach can now burn in order to heat up and transform into Volcans.
    	return true;
    }

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}


	// ==================================================
	//                      Fusion
	// ==================================================
	protected IFusable fusionTarget;

	@Override
	public IFusable getFusionTarget() {
		return this.fusionTarget;
	}

	@Override
	public void setFusionTarget(IFusable fusionTarget) {
		this.fusionTarget = fusionTarget;
	}

	@Override
	public EntityType<? extends LivingEntity> getFusionType(IFusable fusable) {
		if(fusable instanceof EntityCinder) {
			return CreatureManager.getInstance().getEntityType("volcan");
		}
		if(fusable instanceof EntityJengu) {
			return CreatureManager.getInstance().getEntityType("spriggan");
		}
		if(fusable instanceof EntityZephyr) {
			return CreatureManager.getInstance().getEntityType("banshee");
		}
		if(fusable instanceof EntityAegis) {
			return CreatureManager.getInstance().getEntityType("vapula");
		}
		if(fusable instanceof EntityArgus) {
			return CreatureManager.getInstance().getEntityType("tremor");
		}
		return null;
	}
}
