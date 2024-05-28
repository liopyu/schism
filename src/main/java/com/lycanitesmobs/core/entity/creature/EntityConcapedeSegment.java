package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.FollowParentGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EntityConcapedeSegment extends AgeableCreatureEntity {
    
	// Parent UUID:
	/** Used to identify the parent segment when loading this saved entity, set to null when found or lost for good. **/
	UUID parentUUID = null;

	public BaseCreatureEntity backSegment;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityConcapedeSegment(EntityType<? extends EntityConcapedeSegment> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.ARTHROPOD;
        this.hasAttackSound = true;
        this.hasStepSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0D;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
		this.goalSelector.addGoal(this.nextTravelGoalIndex++, new FollowParentGoal(this).setSpeed(1.0D).setStrayDistance(0));
        super.registerGoals();
    }

    // ==================================================
    //                      Spawning
    // ==================================================
    // ========== Natural Spawn Check ==========
    /** Second stage checks for spawning, this check is ignored if there is a valid monster spawner nearby. **/
    @Override
    public boolean environmentSpawnCheck(World world, BlockPos pos) {
    	if(this.getNearbyEntities(EntityConcapedeHead.class, null, CreatureManager.getInstance().spawnConfig.spawnLimitRange).size() <= 0)
    		return false;
    	return super.environmentSpawnCheck(world, pos);
    }
    
    // ========== Get Random Subspecies ==========
    @Override
    public void getRandomVariant() {
    	if(this.subspecies == null && !this.hasParent()) {
    		this.subspecies = this.creatureInfo.getRandomSubspecies(this);
    	}
    	
    	if(this.hasParent() && this.getParentTarget() instanceof BaseCreatureEntity) {
    		this.applyVariant(((BaseCreatureEntity)this.getParentTarget()).getSubspeciesIndex());
    	}
    }
    
    // ========== Despawning ==========
    /** Returns whether this mob should despawn overtime or not. Config defined forced despawns override everything except tamed creatures and tagged creatures. **/
    @Override
    protected boolean canDespawnNaturally() {
    	if(!super.canDespawnNaturally())
    		return false;
    	return !this.hasParent();
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        // Try to Load Parent from UUID:
        if(!this.getCommandSenderWorld().isClientSide && !this.hasParent() && this.parentUUID != null && this.updateTick > 0 && this.updateTick % 40 == 0) {
	        double range = 64D;
	        List connections = this.getCommandSenderWorld().getEntitiesOfClass(AgeableCreatureEntity.class, this.getBoundingBox().inflate(range, range, range));
	        Iterator possibleConnections = connections.iterator();
	        while(possibleConnections.hasNext()) {
	        	AgeableCreatureEntity possibleConnection = (AgeableCreatureEntity)possibleConnections.next();
	            if(possibleConnection != this && possibleConnection.getUUID().equals(this.parentUUID)) {
	            	this.setParentTarget(possibleConnection);
	            	break;
	            }
	        }
	        this.parentUUID = null;
        }
        
        super.aiStep();
        
        // Concapede Connections:
        if(!this.getCommandSenderWorld().isClientSide) {
        	// Check if back segment is alive:
			if(this.backSegment != null) {
				if(!this.backSegment.isAlive())
					this.backSegment = null;
			}

        	// Check if front segment is alive:
        	if(this.hasParent()) {
        		if(!this.getParentTarget().isAlive())
        			this.setParentTarget(null);
        	}

        	// Force position to front with offset:
        	if(this.hasParent()) {
				this.getLookControl().setLookAt(this.getParentTarget(), 360.0F, 360.0F);
				this.lookAt(this.getParentTarget(), 360, 360);

				Vector3d parentPos = this.getFacingPositionDouble(this.getParentTarget().getX(), this.getParentTarget().getY(), this.getParentTarget().getZ(), -0.65D, this.getParentTarget().yRot);
				double segmentPullThreshold = 0.15D;
				double segmentDistance = Math.sqrt(this.distanceToSqr(parentPos));
				if (segmentDistance > segmentPullThreshold) {
					double dragAmount = segmentPullThreshold / 2;
					Vector3d posVector = new Vector3d(this.getX(), this.getY(), this.getZ());
					Vector3d dragPos = this.getFacingPositionDouble(parentPos.x, parentPos.y, parentPos.z, dragAmount, posVector.dot(parentPos));
					double distY = (parentPos.y - this.getY());
					double dragY = this.getY() + (distY / 2);
					this.setPos(dragPos.x, dragY, dragPos.z);
				}
        	}

			// Growth Into Head:
			if(!this.getCommandSenderWorld().isClientSide && this.getGrowingAge() <= 0)
				this.setGrowingAge(-this.growthTime);
        }
    }

	@Override
	public boolean rollLookChance() {
		if(this.hasParent())
			return false;
		return super.rollLookChance();
	}

	@Override
	public boolean rollWanderChance() {
		if(this.hasParent())
			return false;
		return super.rollWanderChance();
	}
	
	
	// ==================================================
  	//                        Age
  	// ==================================================
	@Override
	public void setGrowingAge(int age) {
		if(this.hasParent())
			age = -this.growthTime;
        super.setGrowingAge(age);
		if(age == 0 && !this.getCommandSenderWorld().isClientSide) {
			EntityConcapedeHead concapedeHead = (EntityConcapedeHead)CreatureManager.getInstance().getCreature("concapede").createEntity(this.getCommandSenderWorld());
			concapedeHead.copyPosition(this);
			concapedeHead.firstSpawn = false;
			concapedeHead.setGrowingAge(-this.growthTime / 4);
			concapedeHead.setSizeScale(this.sizeScale);
			concapedeHead.applyVariant(this.getVariantIndex());
			this.getCommandSenderWorld().addFreshEntity(concapedeHead);
			if(this.backSegment != null)
				this.backSegment.setParentTarget(concapedeHead);
			this.remove();
		}
    }

    @Override
	public boolean shouldFollowParent() {
    	return true; // Follow as an adult.
	}

	
    // ==================================================
   	//                      Movement
   	// ==================================================
	// ========== Pathing Weight ==========
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(blockState.getMaterial() == Material.GRASS)
                return 10F;
            if(blockState.getMaterial() == Material.DIRT)
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }
    
	// ========== Can leash ==========
    @Override
    public boolean canBeLeashed(PlayerEntity player) {
	    return !this.hasParent();
    }
    
    // ========== Falling Speed Modifier ==========
    @Override
    public double getFallingMod() {
    	if(this.getCommandSenderWorld().isClientSide)
    		return 0.0D;
    	if(this.hasParent() && this.getParentTarget().position().y() > this.position().y())
    		return 0.0D;
    	return super.getFallingMod();
    }

    @Override
	public boolean useDirectNavigator() {
    	return this.hasParent();
	}

	@Override
	public boolean isPushable() {
		return false;
	}
    
    
    // ==================================================
   	//                     Targets
   	// ==================================================
	@Override
	public void setParentTarget(LivingEntity setTarget) {
		if(setTarget != this) {
			if (setTarget instanceof EntityConcapedeSegment)
				((EntityConcapedeSegment) setTarget).backSegment = this;
			if (setTarget instanceof EntityConcapedeHead)
				((EntityConcapedeHead) setTarget).backSegment = this;
		}
		super.setParentTarget(setTarget);
	}
    
	
	// ==================================================
   	//                     Interact
   	// ==================================================
    // ========== Render Subspecies Name Tag ==========
    /** Gets whether this mob should always display its nametag if it's a subspecies. **/
	@Override
    public boolean renderVariantNameTag() {
    	return !this.hasParent();
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean canClimb() { return true; }
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
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
    	if(type.equals("inWall"))
    		return false;
    	return super.isVulnerableTo(type, source, damage);
    }
    
    @Override
    public float getFallResistance() {
    	return 100;
    }
	
	
	// ==================================================
  	//                      Breeding
  	// ==================================================
    // ========== Create Child ==========
    @Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		return null;
	}
	
	// ========== Breed ==========
	public boolean breed() {
		if(!this.canBreed())
			return false;
        this.setGrowingAge(0);
        return true;
	}
	
	@Override
	public boolean canBreed() {
        return !this.hasParent();
    }

	@Override
	public boolean shouldFindParent() {
		return false;
	}
    
    
    // ==================================================
    //                        NBT
    // ==================================================
   	// ========== Read ===========
    /** Used when loading this mob from a saved chunk. **/
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
    	if(nbt.hasUUID("ParentUUID")) {
            this.parentUUID = nbt.getUUID("ParentUUID");
        }
        super.readAdditionalSaveData(nbt);
    }
    
    // ========== Write ==========
    /** Used when saving this mob to a chunk. **/
    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
		super.addAdditionalSaveData(nbt);
    	if(this.hasParent()) {
			nbt.putUUID("ParentUUID", this.getParentTarget().getUUID());
    	}
    }
}
