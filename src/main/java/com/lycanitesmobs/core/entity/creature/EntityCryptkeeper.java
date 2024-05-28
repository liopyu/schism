package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveVillageGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class EntityCryptkeeper extends AgeableCreatureEntity implements IMob {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCryptkeeper(EntityType<? extends EntityCryptkeeper> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEAD;
        this.hasAttackSound = false;
        this.spreadFire = true;
        this.canGrow = false;
        this.babySpawnChance = 0.1D;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new MoveVillageGoal(this));

        super.registerGoals();

        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.HUSK));

        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(PlayerEntity.class).setLongMemory(false));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));

        if(this.getNavigation() instanceof GroundPathNavigator) {
            GroundPathNavigator pathNavigateGround = (GroundPathNavigator)this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
            pathNavigateGround.setAvoidSun(true);
        }
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
    // ========== On Kill ==========
    @Override
    public void onKillEntity(LivingEntity entityLivingBase) {
        super.onKillEntity(entityLivingBase);

        if(this.getCommandSenderWorld().getDifficulty().getId() >= 2 && entityLivingBase instanceof VillagerEntity) {
            if (this.getCommandSenderWorld().getDifficulty().getId() == 2 && this.random.nextBoolean()) return;

            VillagerEntity villagerentity = (VillagerEntity)entityLivingBase;
            ZombieVillagerEntity zombievillagerentity = EntityType.ZOMBIE_VILLAGER.create(this.level);
            zombievillagerentity.copyPosition(villagerentity);
            villagerentity.remove();
            zombievillagerentity.finalizeSpawn((IServerWorld) this.getCommandSenderWorld(), this.getCommandSenderWorld().getCurrentDifficultyAt(zombievillagerentity.blockPosition()),SpawnReason.CONVERSION, null, null);
            zombievillagerentity.setVillagerData(villagerentity.getVillagerData());
            zombievillagerentity.setTradeOffers(villagerentity.getOffers().createTag());
            zombievillagerentity.setVillagerXp(villagerentity.getVillagerXp());
            zombievillagerentity.setBaby(villagerentity.isBaby());
            zombievillagerentity.setNoAi(villagerentity.isNoAi());

            if (villagerentity.hasCustomName()) {
                zombievillagerentity.setCustomName(villagerentity.getCustomName());
                zombievillagerentity.setCustomNameVisible(villagerentity.isCustomNameVisible());
            }

            this.getCommandSenderWorld().addFreshEntity(zombievillagerentity);
            this.getCommandSenderWorld().levelEvent(null, 1016, zombievillagerentity.blockPosition(), 0);
        }
    }
}
