package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.IMob;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityEttin extends AgeableCreatureEntity implements IMob {
	public boolean griefing = true;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityEttin(EntityType<? extends EntityEttin> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        
        this.solidCollision = true;
        this.setupMob();
        
        // Stats:
        this.attackPhaseMax = 2;
    }

    @Override
    protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));

		if(this.getNavigation() instanceof GroundPathNavigator) {
			GroundPathNavigator pathNavigateGround = (GroundPathNavigator)this.getNavigation();
			pathNavigateGround.setCanOpenDoors(true);
		}
    }

	@Override
	public void loadCreatureFlags() {
		this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
	}

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
    	// Destroy Blocks:
		if(!this.getCommandSenderWorld().isClientSide)
	        if(this.getTarget() != null && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing) {
		    	float distance = this.getTarget().distanceTo(this);
		    		if(distance <= this.getDimensions(Pose.STANDING).width + 4.0F)
		    			this.destroyArea((int)this.position().x(), (int)this.position().y(), (int)this.position().z(), 0.5F, true);
	        }
        
        super.aiStep();
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	boolean success = super.attackMelee(target, damageScale);
    	if(success)
    		this.nextAttackPhase();
    	return success;
    }
}
