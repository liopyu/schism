package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.navigate.CreaturePathNavigator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.GroundPathNavigator;

public class PaddleGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private boolean sink = false;
    
    // ==================================================
   	//                    Constructor
   	// ==================================================
    public PaddleGoal(BaseCreatureEntity setEntity) {
        this.host = setEntity;
        if(setEntity.getNavigation() instanceof GroundPathNavigator || setEntity.getNavigation() instanceof CreaturePathNavigator)
            setEntity.getNavigation().setCanFloat(true);
		this.sink = this.host.canBreatheUnderwater() || (this.host.canBreatheUnderlava() && this.host.isLavaCreature);
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public PaddleGoal setSink(boolean setSink) {
    	this.sink = setSink;
    	return this;
    }
    
    
    // ==================================================
   	//                  Should Execute
   	// ==================================================
	@Override
    public boolean canUse() {
    	if(this.host.isStrongSwimmer())
    		return false;
        if(this.host.getControllingPassenger() != null && this.host.getControllingPassenger() instanceof PlayerEntity && this.host.canBeControlledByRider())
            return false;
        return this.host.isInWater() || this.host.isInLava();
    }
    
    
    // ==================================================
   	//                      Update
   	// ==================================================
	@Override
    public void tick() {
    	if(this.sink) {
	    	double targetY = this.host.position().y();
	    	if(!this.host.useDirectNavigator()) {
	    		if(!this.host.getNavigation().isDone()) {
                    targetY = this.host.getNavigation().getPath().getEndNode().y;
                    if(this.host.hasAttackTarget())
                        targetY = this.host.getTarget().position().y();
                    else if(this.host.hasParent())
                        targetY = this.host.getParentTarget().position().y();
                    else if(this.host.hasMaster())
                        targetY = this.host.getMasterTarget().position().y();
                }
	    	}
	    	else {
	    		if(!this.host.directNavigator.atTargetPosition()) {
                    targetY = this.host.directNavigator.targetPosition.getY();
                }
	    	}

			if (this.host.position().y() < targetY) {
				this.host.getJumpControl().jump();
			}
			else {
				this.host.push(0, -(0.01F + this.host.getSpeed() * 0.25F), 0);
			}
    	}
    	else if(this.host.getRandom().nextFloat() < 0.8F) {
			this.host.getJumpControl().jump();
		}
    }
}
