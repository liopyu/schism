package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityKrake extends AgeableCreatureEntity implements IMob {

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityKrake(EntityType<? extends EntityKrake> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.hasAttackSound = true;

        this.babySpawnChance = 0.1D;
        this.canGrow = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setRange(1));
    }

    @Override
    public boolean canSee(Entity target) {
        if(target == this.getLastHurtByMob() && this.getLastHurtByMobTimestamp() + (3 * 20) > this.tickCount) {
            return true;
        }
        if(target instanceof PlayerEntity || target instanceof VillagerEntity) {
            return target.isSprinting();
        }
        return true;
    }

	
    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
    	if(this.isInWater()) // Checks specifically just for water.
    		return 1.5F;
    	else if(this.waterContact()) // Checks for water, rain, etc.
    		return 1.25F;
    	return super.getAISpeedModifier();
    }
    
	// Pathing Weight:
	@Override
    public float getBlockPathWeight(int x, int y, int z) {
        int waterWeight = 10;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState blockState = this.getCommandSenderWorld().getBlockState(pos);
        if(blockState.getBlock() == Blocks.WATER)
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);
        if(this.getCommandSenderWorld().isRaining() && this.getCommandSenderWorld().canSeeSkyFromBelowWater(pos))
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);

        if(this.getTarget() != null)
            return super.getBlockPathWeight(x, y, z);

        return super.getBlockPathWeight(x, y, z);
    }
	
	// Pushed By Water:
	@Override
	public boolean isPushedByFluid() {
        return false;
    }

    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed(PlayerEntity player) {
        if(!this.hasAttackTarget())
            return true;
        return super.canBeLeashed(player);
    }

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
    // ========== Get Wander Position ==========
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        BlockPos groundPos;
        for(groundPos = wanderPosition.below(); groundPos.getY() > 0 && !this.getCommandSenderWorld().getBlockState(groundPos).getMaterial().isSolid(); groundPos = groundPos.below()) {}
        return groundPos.above();
    }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
}
