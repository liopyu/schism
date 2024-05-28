package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.api.Targeting;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.TameableEntity;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class DefendOwnerGoal extends TargetingGoal {
	// Properties:
	private TameableCreatureEntity tamedHost;
    
    // ==================================================
  	//                    Constructor
  	// ==================================================
    public DefendOwnerGoal(TameableCreatureEntity setHost) {
        super(setHost);
    	this.tamedHost = setHost;
		this.setFlags(EnumSet.of(Flag.TARGET));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public DefendOwnerGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }
    
    public DefendOwnerGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }
    
    public DefendOwnerGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }
    
    
    // ==================================================
 	//                    Host Target
 	// ==================================================
    @Override
    protected LivingEntity getTarget() { return this.host.getTarget(); }
    @Override
    protected void setTarget(LivingEntity newTarget) { this.host.setTarget(newTarget); }
    protected Entity getOwner() { return this.tamedHost.getPlayerOwner(); }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {
		if(target == null) {
			return false;
		}

    	// Owner Check:
    	if(!this.tamedHost.isTamed() || this.getOwner() == null)
    		return false;
    	
    	// Passive Check:
    	if(this.tamedHost.isPassive())
			return false;
    	
    	// Aggressive Check:
    	if(!this.host.isAggressive())
            return false;
    	
    	// Team Checks:
        if(this.host.isAlliedTo(target)) {
            return false;
        }

        // LivingEntity Check:
		if(target instanceof MobEntity) {
			MobEntity mobEntity = (MobEntity)target;
			if(!mobEntity.canAttackType(EntityType.PLAYER)) {
				return false;
			}
		}

		// Mod Interaction Check:
		if(!Targeting.isValidTarget(this.host, target)) {
        	return false;
		}

        // Threat Check:
        if(target instanceof IMob && !(target instanceof TameableEntity) && !(target instanceof BaseCreatureEntity)) {
            return true;
        }
        else if(target instanceof BaseCreatureEntity && ((BaseCreatureEntity)target).isHostileTo(this.getOwner())) {
            return true;
        }
        else if(target instanceof MobEntity && ((MobEntity)target).getTarget() == this.getOwner()) {
            return true;
        }
		else if(target.getLastHurtByMob() == this.getOwner()) {
			return true;
		}
        
    	return false;
    }
    
    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
    @Override
    public boolean canUse() {
    	this.target = null;
    	
    	// Owner Check:
    	if(!this.tamedHost.isTamed())
    		return false;
    	
    	// Passive Check:
    	if(this.tamedHost.isPassive())
			return false;
    	
    	// Aggressive Check:
    	if(!this.host.isAggressive())
            return false;
        
        double distance = this.getTargetDistance() - this.host.getDimensions(Pose.STANDING).width;
        double heightDistance = 4.0D - this.host.getDimensions(Pose.STANDING).height;
        if(this.host.useDirectNavigator())
            heightDistance = distance;
        this.target = this.getNewTarget(distance, heightDistance, distance);
        if(this.callForHelp)
            this.callNearbyForHelp();
        return this.target != null;
    }


	// ==================================================
	//                  Continue Executing
	// ==================================================
	@Override
	public boolean canContinueToUse() {
		if(!this.isValidTarget(this.getTarget()))
			return false;
		return super.canContinueToUse();
	}
}
