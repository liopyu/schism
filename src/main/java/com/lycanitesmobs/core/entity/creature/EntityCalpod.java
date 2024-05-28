package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.block.WoodType;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityCalpod extends BaseCreatureEntity implements IMob {
	private int swarmLimit = 5;
	private boolean griefing = true;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCalpod(EntityType<? extends EntityCalpod> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.ARTHROPOD;
        this.hasAttackSound = true;
        this.setupMob();
	}

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }

	@Override
	public void loadCreatureFlags() {
		this.swarmLimit = this.creatureInfo.getFlag("swarmLimit", this.swarmLimit);
		this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
		if(!this.getCommandSenderWorld().isClientSide && this.hasAttackTarget() && this.getTarget() instanceof PlayerEntity && this.updateTick % 60 == 0) {
			this.allyUpdate();
		}

		// Destroy Blocks:
		if(!this.getCommandSenderWorld().isClientSide)
			if(this.getTarget() != null && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing) {
				float distance = this.getTarget().distanceTo(this);
				if(distance <= this.getDimensions(Pose.STANDING).width + 1.0F)
					this.destroyAreaBlock((int)this.position().x(), (int)this.position().y(), (int)this.position().z(), WoodType.class, true, 0);
			}
        
        super.aiStep();
    }
    
    // ========== Spawn Minions ==========
	public void allyUpdate() {
		if(this.getCommandSenderWorld().isClientSide)
			return;
		
		// Spawn Minions:
		if(this.swarmLimit > 0 && this.countAllies(64D) < this.swarmLimit) {
			float random = this.random.nextFloat();
			if(random <= 0.125F)
				this.spawnAlly(this.position().x() - 2 + (random * 4), this.position().y(), this.position().z() - 2 + (random * 4));
		}
	}
	
    public void spawnAlly(double x, double y, double z) {
		BaseCreatureEntity minion = (BaseCreatureEntity) this.creatureInfo.createEntity(this.getCommandSenderWorld());
    	minion.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
		minion.setMinion(true);
		minion.applyVariant(this.getVariantIndex());
		this.getCommandSenderWorld().addFreshEntity(minion);
        if(this.getTarget() != null)
        	minion.setLastHurtByMob(this.getTarget());
    }
    
    
    // ==================================================
    //                       Death
    // ==================================================
    @Override
    public void die(DamageSource par1DamageSource) {
    	allyUpdate();
        super.die(par1DamageSource);
    }

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
	// ==================================================
	//                     Abilities
	// ==================================================
	// ========== Movement ==========
	@Override
	public boolean canClimb() { return true; }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
        return 100;
    }
}
