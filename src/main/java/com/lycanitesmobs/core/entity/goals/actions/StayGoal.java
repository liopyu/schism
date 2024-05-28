package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class StayGoal extends Goal {
	// Targets:
    private TameableCreatureEntity host;
    
    // Properties:
    private boolean enabled = true;
    private double speed = 1.0D;
    private double farSpeed = 1.5D;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public StayGoal(TameableCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE));
    }
    
    
	// ==================================================
 	//                  Set Properties
 	// ==================================================
    public StayGoal setEnabled(boolean flag) {
        this.enabled = flag;
        return this;
    }
    
    public StayGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    
    public StayGoal setFarSpeed(double setSpeed) {
    	this.farSpeed = setSpeed;
    	return this;
    }
    
    
    // ==================================================
  	//                   Should Execute
  	// ==================================================
	@Override
    public boolean canUse() {
    	if(!this.enabled)
    		return false;
        if(!this.host.isTamed())
            return false;
        if(this.host.isInWater()) {
        	if(!this.host.canBreatheUnderwater())
        		return false;
		}
        else if(!this.host.isOnGround() && !this.host.isFlying()) {
			return false;
		}

        if (!(this.host.getOwner() instanceof LivingEntity)) {
			return false;
		}
        LivingEntity owner = (LivingEntity)this.host.getOwner();
        if(owner != null && this.host.distanceTo(owner) < 144.0D && owner.getLastHurtByMob() != null && !this.host.isPassive()) {
			return false;
		}
        
        return this.host.isSitting();
    }
    
    
	// ==================================================
 	//                      Start
 	// ==================================================
	@Override
    public void start() {
        this.host.clearMovement();
        if(this.host.hasHome() && this.host.getDistanceFromHome() > 1.0F) {
        	BlockPos homePos = this.host.getRestrictCenter();
        	double speed = this.speed;
        	if(this.host.getDistanceFromHome() > this.host.getHomeDistanceMax())
        		speed = this.farSpeed;
	    	if(!host.useDirectNavigator())
	    		this.host.getNavigation().moveTo(homePos.getX(), homePos.getY(), homePos.getZ(), this.speed);
	    	else
	    		host.directNavigator.setTargetPosition(new BlockPos(homePos), speed);
        }
    }
}
