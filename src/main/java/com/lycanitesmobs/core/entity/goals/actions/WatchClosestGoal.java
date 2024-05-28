package com.lycanitesmobs.core.entity.goals.actions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class WatchClosestGoal extends Goal {
    // Targets:
    private MobEntity host;
    protected Entity closestEntity;
    private final EntityPredicate searchPredicate = (new EntityPredicate()).range(64.0D);

    // Properties
    private Class watchedClass = LivingEntity.class;
    private float maxDistance = 4.0F;
    private int lookTime;
    private int lookTimeMin = 40;
    private int lookTimeRange = 40;
    private float lookChance = 0.02F;
    
    // ==================================================
   	//                     Constructor
   	// ==================================================
    public WatchClosestGoal(MobEntity setHost) {
    	this.host = setHost;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public WatchClosestGoal setTargetClass(Class setTarget) {
    	this.watchedClass = setTarget;
    	return this;
    }
    public WatchClosestGoal setMaxDistance(float setDist) {
    	this.maxDistance = setDist;
    	return this;
    }
    public WatchClosestGoal setlookChance(float setChance) {
    	this.lookChance = setChance;
    	return this;
    }
    
    
    // ==================================================
   	//                   Should Execute
   	// ==================================================
	@Override
    public boolean canUse() {
        if(this.host.getRandom().nextFloat() >= this.lookChance)
            return false;
        else {
            if(this.host.getTarget() != null)
                this.closestEntity = this.host.getTarget();
            if(this.watchedClass == PlayerEntity.class)
                this.closestEntity = this.host.getCommandSenderWorld().getNearestPlayer(this.host.position().x(), this.host.position().y(), this.host.position().z(), this.maxDistance, entity -> true);
            else
                this.host.level.getNearbyEntities(LivingEntity.class, this.searchPredicate, this.host, this.host.getBoundingBox().inflate((double)this.maxDistance));

            return this.closestEntity != null;
        }
    }
    
    
    // ==================================================
   	//                 Continue Executing
   	// ==================================================
	@Override
    public boolean canContinueToUse() {
    	if(!this.closestEntity.isAlive())
    		return false;
    	if(this.host.distanceTo(this.closestEntity) > (double)(this.maxDistance * this.maxDistance))
    		return false;
        return this.lookTime > 0;
    }
    
    
    // ==================================================
   	//                  Start Executing
   	// ==================================================
	@Override
    public void start() {
        this.lookTime = lookTimeMin + this.host.getRandom().nextInt(lookTimeRange);
    }
    
    
    // ==================================================
   	//                      Reset
   	// ==================================================
	@Override
    public void stop() {
        this.closestEntity = null;
    }
    
    
    // ==================================================
   	//                      Update
   	// ==================================================
	@Override
    public void tick() {
        this.host.getLookControl().setLookAt(this.closestEntity.position().x(), this.closestEntity.position().y() + (double)this.closestEntity.getEyeHeight(), this.closestEntity.position().z(), 10.0F, (float)this.host.getMaxHeadXRot());
        this.lookTime--;
    }
}