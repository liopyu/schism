package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.api.IFusable;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityArgus extends TameableCreatureEntity implements IMob, IFusable {

	private int teleportTime = 60;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityArgus(EntityType<? extends EntityArgus> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;
        this.spawnsInWater = true;
        this.setupMob();

        this.maxUpStep = 1.0F;

        this.setAttackCooldownMax(40);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true).setMaxChaseDistanceSq(5.0F));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(16.0F).setMinChaseDistance(14.0F));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        /*/ Random Target Teleporting:
		if(!this.getEntityWorld().isRemote && this.hasAttackTarget()) {
			if(this.teleportTime-- <= 0) {
				this.teleportTime = 20 + this.getRNG().nextInt(20);
				BlockPos teleportPosition = this.getFacingPosition(this.getAttackTarget(), -this.getAttackTarget().getSize(Pose.STANDING).width - 1D, 0);
				if(this.canTeleportTo(teleportPosition)) {
					this.playJumpSound();
					this.setPosition(teleportPosition.getX(), teleportPosition.getY(), teleportPosition.getZ());
				}
			}
		}*/
        
        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
	        for(int i = 0; i < 2; ++i) {
	            this.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
	        }
    }


	// ==================================================
	//                      Attacks
	// ==================================================
	// ========== Ranged Attack ==========
	@Override
	public void attackRanged(Entity target, float range) {
		this.fireProjectile("chaosorb", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 1f, 1F);
		super.attackRanged(target, range);
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
			return CreatureManager.getInstance().getEntityType("grue");
		}
		if(fusable instanceof EntityJengu) {
			return CreatureManager.getInstance().getEntityType("eechetik");
		}
		if(fusable instanceof EntityGeonach) {
			return CreatureManager.getInstance().getEntityType("tremor");
		}
		if(fusable instanceof EntityZephyr) {
			return CreatureManager.getInstance().getEntityType("wraith");
		}
		if(fusable instanceof EntityAegis) {
			return CreatureManager.getInstance().getEntityType("spectre");
		}
		return null;
	}
}
