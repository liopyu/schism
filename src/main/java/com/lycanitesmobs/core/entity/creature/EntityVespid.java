package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureBuildTask;
import com.lycanitesmobs.core.entity.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.PlaceBlockGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class EntityVespid extends AgeableCreatureEntity implements IMob {
    public PlaceBlockGoal aiPlaceBlock;
    public CreatureBuildTask creatureBuildTask;

	private boolean hiveBuilding = true;

    public EntityVespid(EntityType<? extends EntityVespid> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.ARTHROPOD;
        this.hasAttackSound = true;
        this.setupMob();

        this.canGrow = true;
        this.babySpawnChance = 0.1D;

        this.maxUpStep = 1.0F;
        this.setAttackCooldownMax(10);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
		this.aiPlaceBlock = new PlaceBlockGoal(this).setMaxDistance(128D).setSpeed(3D).setReplaceLiquid(true).setReplaceSolid(true);
		this.goalSelector.addGoal(this.nextIdleGoalIndex++, this.aiPlaceBlock);

		this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindMasterGoal(this).setTargetClass(EntityVespidQueen.class).setRange(64.0D));
    }

	@Override
	public void loadCreatureFlags() {
		this.hiveBuilding = this.creatureInfo.getFlag("hiveBuilding", this.hiveBuilding);
	}

    @Override
    public boolean isPersistant() {
    	if(this.getMasterTarget() instanceof BaseCreatureEntity)
    		return ((BaseCreatureEntity)this.getMasterTarget()).isPersistant();
    	return super.isPersistant();
    }

	@Override
    public void aiStep() {
        super.aiStep();
        
        // Building AI:
        if(!this.getCommandSenderWorld().isClientSide && this.hiveBuilding && this.getMasterTarget() instanceof EntityVespidQueen && this.aiPlaceBlock.blockState == null) {
        	EntityVespidQueen queen = (EntityVespidQueen)this.getMasterTarget();
        	this.creatureBuildTask = queen.creatureStructure.getBuildTask(this);
        	if (this.creatureBuildTask != null) {
				this.aiPlaceBlock.setBlockPlacement(this.creatureBuildTask.blockState, this.creatureBuildTask.pos);
			}
        }
        
        // Don't Keep Infected Conbas Targeted:
        if(!this.getCommandSenderWorld().isClientSide && this.getTarget() instanceof EntityConba) {
        	if(((EntityConba)this.getTarget()).vespidInfection) {
        		this.setTarget(null);
        	}
        }
    }

    @Override
	public void onBlockPlaced(BlockPos blockPos, BlockState blockState) {
    	if (this.getMasterTarget() instanceof EntityVespidQueen) {
			EntityVespidQueen queen = (EntityVespidQueen)this.getMasterTarget();
			if (this.creatureBuildTask != null) {
				queen.creatureStructure.completeBuildTask(this.creatureBuildTask);
				this.creatureBuildTask = null;
			}
		}
	}

    @Override
    public boolean canAttack(LivingEntity targetEntity) {
    	if(targetEntity == this.getMasterTarget())
    		return false;
		if(targetEntity instanceof EntityConba)
        	return false;
    	if(targetEntity instanceof EntityVespid) {
    		if(!((EntityVespid)targetEntity).hasMaster() || ((EntityVespid)targetEntity).getMasterTarget() == this.getMasterTarget())
    			return false;
    	}
    	if(targetEntity instanceof EntityVespidQueen) {
    		if(!this.hasMaster() || this.getMasterTarget() == targetEntity)
    			return false;
    	}
    	if (this.hasMaster() && this.getMasterTarget() instanceof EntityVespidQueen) {
			EntityVespidQueen entityVespidQueen = (EntityVespidQueen)this.getMasterTarget();
			CreatureRelationshipEntry creatureRelationshipEntry = entityVespidQueen.relationships.getEntry(targetEntity);
			if (creatureRelationshipEntry != null && !creatureRelationshipEntry.canAttack()) {
				return false;
			}
		}
    	return super.canAttack(targetEntity);
    }

    @Override
    public boolean isFlying() { return true; }

	@Override
    public float getDamageModifier(DamageSource damageSrc) {
    	if(damageSrc.isFire())
    		return 2.0F;
    	return super.getDamageModifier(damageSrc);
    }

    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
    	if(type.equals("inWall")) return false;
    	return super.isVulnerableTo(type, source, damage);
    }

    @Override
	public boolean canSee(Entity entity) {
    	if (entity instanceof EntityVespidQueen) {
    		return true;
		}
    	return super.canSee(entity);
	}
}
