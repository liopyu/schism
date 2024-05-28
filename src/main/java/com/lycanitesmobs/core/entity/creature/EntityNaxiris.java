package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityNaxiris extends RideableCreatureEntity {
	public boolean griefing = true;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityNaxiris(EntityType<? extends EntityNaxiris> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = false;
        
        this.setAttackCooldownMax(20);
		this.solidCollision = true;
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.25D).setRange(40.0F).setMinChaseDistance(10.0F).setLongMemory(false));
    }

	@Override
	public void loadCreatureFlags() {
		this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
	}


    // ==================================================
    //                      Updates
    // ==================================================
    @Override
    public void riderEffects(LivingEntity rider) {
        if(rider.hasEffect(Effects.DIG_SLOWDOWN))
            rider.removeEffect(Effects.DIG_SLOWDOWN);
        if(rider.hasEffect(ObjectManager.getEffect("weight")))
            rider.removeEffect(ObjectManager.getEffect("weight"));
    }

    
    // ==================================================
   	//                    Taking Damage
   	// ==================================================
    // ========== On Damage ==========
    /** Called when this mob has received damage. **/
    public void onDamage(DamageSource damageSrc, float damage) {
    	super.onDamage(damageSrc, damage);
    	
    	Entity damageEntity = damageSrc.getEntity();
    	if(damageEntity != null && ("mob".equals(damageSrc.msgId) || "player".equals(damageSrc.msgId))) {
    		
    		// Eat Buffs:
        	if(damageEntity instanceof LivingEntity) {
        		LivingEntity targetLiving = (LivingEntity)damageEntity;
        		List<Effect> goodEffects = new ArrayList<>();
        		for(EffectInstance effect : targetLiving.getActiveEffects()) {
					if(ObjectLists.inEffectList("buffs", effect.getEffect()))
						goodEffects.add(effect.getEffect());
        		}
        		if(goodEffects.size() > 0 && this.getRandom().nextBoolean()) {
        			if(goodEffects.size() > 1)
        				targetLiving.removeEffect(goodEffects.get(this.getRandom().nextInt(goodEffects.size())));
        			else
        				targetLiving.removeEffect(goodEffects.get(0));
    		    	float leeching = damage * 1.1F;
    		    	this.heal(leeching);
        		}
        	}
    	}
    }
	
	
	// ==================================================
  	//                     Abilities
  	// ==================================================
    // ========== Movement ==========
    public boolean isFlying() { return true; }
    
    
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
   	//                      Attacks
   	// ==================================================
	// ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("arcanelaserstorm", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        super.attackRanged(target, range);
    }
    
    
    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isVulnerableTo(Entity entity) {
    	if(entity instanceof EntityNaxiris)
    		return false;
    	return super.isVulnerableTo(entity);
    }
    
    @Override
    public boolean canBurn() { return false; }


    // ==================================================
    //                      Movement
    // ==================================================
    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getDimensions(Pose.STANDING).height * 0.9D;
    }

	@Override
	public double getMountedZOffset() {
		return (double)this.getDimensions(Pose.STANDING).width * -0.2D;
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

        if(this.hasPickupEntity()) {
            this.dropPickupEntity();
            return;
        }

        if(this.getStamina() < this.getStaminaCost())
            return;

        if(rider instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)rider;
			ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("arcanelaserstorm");
			if(projectileInfo != null) {
				BaseProjectileEntity projectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), player);
				this.getCommandSenderWorld().addFreshEntity(projectile);
				this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
				this.triggerAttackCooldown();
			}
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 10;
    }

    public int getStaminaRecoveryWarmup() {
        return 2 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }
    
    
    // ==================================================
    //                   Brightness
    // ==================================================
    @Override
    public float getBrightness() {
        if(isAttackOnCooldown())
        	return 1.0F;
        else
        	return super.getBrightness();
    }
}
