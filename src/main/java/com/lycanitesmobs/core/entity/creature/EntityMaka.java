package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityMaka extends AgeableCreatureEntity {
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityMaka(EntityType<? extends EntityMaka> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;

        this.babySpawnChance = 0.1D;
        this.attackCooldownMax = 10;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));

		this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindMasterGoal(this).setTargetClass(EntityMakaAlpha.class).setSightCheck(false));
    }


    // ==================================================
    //                      Spawn
    // ==================================================
    // ========== On Spawn ==========
    @Override
    public void onFirstSpawn() {
        // Random Alpha:
        CreatureInfo alphaInfo = CreatureManager.getInstance().getCreature("makaalpha");
        if(alphaInfo != null) {
            float alphaChance = (float)alphaInfo.creatureSpawn.spawnWeight / Math.max(this.creatureInfo.creatureSpawn.spawnWeight, 1);
            if (this.getRandom().nextFloat() <= alphaChance) {
				EntityMakaAlpha alpha = (EntityMakaAlpha)CreatureManager.getInstance().getCreature("makaalpha").createEntity(this.getCommandSenderWorld());
                alpha.copyPosition(this);
                this.getCommandSenderWorld().addFreshEntity(alpha);
                this.remove();
            }
        }
        super.onFirstSpawn();
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
	    return true;
    }

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
	// ==================================================
   	//                      Attacks
   	// ==================================================
    // ========== Attack Class ==========
	@Override
	public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityMaka || target instanceof EntityMakaAlpha)
			return false;
		return super.canAttack(target);
	}
    
    
    // ==================================================
    //                     Growing
    // ==================================================
	@Override
	public void setGrowingAge(int age) {
		if(age == 0 && this.getAge() < 0) {
            if (this.getRandom().nextFloat() >= 0.9F) {
				EntityMakaAlpha alpha = (EntityMakaAlpha)CreatureManager.getInstance().getCreature("makaalpha").createEntity(this.getCommandSenderWorld());
                alpha.copyPosition(this);
                this.getCommandSenderWorld().addFreshEntity(alpha);
                this.remove();
            }
        }
        super.setGrowingAge(age);
    }
}
