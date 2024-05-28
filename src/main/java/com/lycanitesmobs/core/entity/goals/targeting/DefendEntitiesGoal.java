package com.lycanitesmobs.core.entity.goals.targeting;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.TameableEntity;

public class DefendEntitiesGoal extends TargetingGoal {
	/** The entity class to defend. **/
	protected Class<? extends LivingEntity> defendClass;

    // ==================================================
  	//                    Constructor
  	// ==================================================
    public DefendEntitiesGoal(BaseCreatureEntity setHost, Class<? extends LivingEntity> defendClass) {
        super(setHost);
        this.defendClass = defendClass;
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public DefendEntitiesGoal setSightCheck(boolean setSightCheck) {
    	this.checkSight = setSightCheck;
    	return this;
    }
    
    public DefendEntitiesGoal setOnlyNearby(boolean setNearby) {
    	this.nearbyOnly = setNearby;
    	return this;
    }
    
    public DefendEntitiesGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
    	this.cantSeeTimeMax = setCantSeeTimeMax;
    	return this;
    }
    
    
    // ==================================================
 	//                    Host Target
 	// ==================================================
    @Override
    protected LivingEntity getTarget() {
    	return this.host.getTarget();
    }

    @Override
    protected void setTarget(LivingEntity newTarget) {
    	this.host.setTarget(newTarget);
    }
    
    
    // ==================================================
 	//                 Valid Target Check
 	// ==================================================
    @Override
    protected boolean isValidTarget(LivingEntity target) {

		// Owner Check:
		if(this.host.getOwner() != null) {
			return false;
		}

		// Has Target Check:
		LivingEntity targetTarget = target.getLastHurtByMob();
		if(target instanceof CreatureEntity) {
			targetTarget = ((CreatureEntity)target).getTarget();
		}
		if(targetTarget == null) {
			return false;
		}

		// Ownable Checks:
		if(this.host.getOwner() != null) {
			if(target instanceof TameableEntity && this.host.getOwner() == ((TameableEntity)target).getOwner()) {
				return false;
			}
			if(target instanceof TameableCreatureEntity && this.host.getOwner() == ((TameableCreatureEntity)target).getOwner()) {
				return false;
			}
			if(target == this.host.getOwner()) {
				return false;
			}
		}

		// Threat Check:
		if(this.defendClass.isAssignableFrom(targetTarget.getClass())) {
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
    	if(this.host.getOwner() != null)
    		return false;
        
        double distance = this.getTargetDistance() - this.host.getDimensions(Pose.STANDING).width;
        double heightDistance = 4.0D - this.host.getDimensions(Pose.STANDING).height;
        if(this.host.useDirectNavigator())
            heightDistance = this.getTargetDistance() - this.host.getDimensions(Pose.STANDING).height;
        if(this.host.useDirectNavigator())
            heightDistance = distance;
        this.target = this.getNewTarget(distance, heightDistance, distance);
        if(this.callForHelp)
            this.callNearbyForHelp();
        return this.target != null;
    }
}
