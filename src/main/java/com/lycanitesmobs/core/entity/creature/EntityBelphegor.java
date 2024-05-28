package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireOrb;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityBelphegor extends TameableCreatureEntity implements IMob {

    // Data Manager:
    protected static final DataParameter<Integer> HELLFIRE_ENERGY = EntityDataManager.defineId(EntityBelphegor.class, DataSerializers.INT);

    public int hellfireEnergy = 0;
    public List<EntityHellfireOrb> hellfireOrbs = new ArrayList<>();
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityBelphegor(EntityType<? extends EntityBelphegor> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEAD;
        this.hasAttackSound = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(this.nextTravelGoalIndex++, new MoveRestrictionGoal(this));

        super.registerGoals();

        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0D).setRange(16.0F).setMinChaseDistance(8.0F).setChaseTime(-1));

        if(this.getNavigation() instanceof GroundPathNavigator) {
            GroundPathNavigator pathNavigateGround = (GroundPathNavigator)this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
        }
    }

    // ========== Init ==========
    /** Initiates the entity setting all the values to be watched by the datawatcher. **/
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELLFIRE_ENERGY, this.hellfireEnergy);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Sync Hellfire Energy:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(HELLFIRE_ENERGY, this.hellfireEnergy);
        }
        else {
            try {
                this.hellfireEnergy = this.entityData.get(HELLFIRE_ENERGY);
            }
            catch(Exception e) {}
        }

        // Hellfire Update:
        if(this.getCommandSenderWorld().isClientSide && this.hellfireEnergy > 0)
            EntityRahovart.updateHellfireOrbs(this, this.updateTick, 3, this.hellfireEnergy, 0.5F, this.hellfireOrbs);
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttack(LivingEntity target) {
    	if(this.isTamed())
    		return super.canAttack(target);
    	if(target instanceof EntityBehemophet)
    		return false;
        return super.canAttack(target);
    }
    
    // ========== Ranged Attack ==========
	@Override
	public void attackRanged(Entity target, float range) {
		this.fireProjectile("doomfireball", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
		super.attackRanged(target, range);
	}
    
    
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
    public boolean canBurn() { return false; }
}
