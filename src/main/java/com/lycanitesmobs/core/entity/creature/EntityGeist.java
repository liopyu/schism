package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveVillageGoal;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class EntityGeist extends AgeableCreatureEntity implements IMob {

    public boolean shadowfireDeath = true;

    public EntityGeist(EntityType<? extends EntityGeist> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEAD;
        this.hasAttackSound = true;
        this.spreadFire = true;

        this.canGrow = false;
        this.babySpawnChance = 0.01D;

        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(this.nextTravelGoalIndex++, new MoveVillageGoal(this));

        super.registerGoals();

        this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(PlayerEntity.class).setLongMemory(false));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));

        if(this.getNavigation() instanceof GroundPathNavigator) {
            GroundPathNavigator pathNavigateGround = (GroundPathNavigator)this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
            pathNavigateGround.setAvoidSun(true);
        }
    }

    @Override
    public void loadCreatureFlags() {
        this.shadowfireDeath = this.creatureInfo.getFlag("shadowfireDeath", this.shadowfireDeath);
    }

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

    @Override
    public void die(DamageSource damageSource) {
        try {
            int shadowfireWidth = (int)Math.floor(this.getDimensions(this.getPose()).width) + 1;
            int shadowfireHeight = (int)Math.floor(this.getDimensions(this.getPose()).height) + 1;
            boolean permanent = false;
            if(damageSource.getEntity() == this) {
                permanent = true;
                shadowfireWidth *= 5;
            }

            if(!this.getCommandSenderWorld().isClientSide && (permanent || (this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.shadowfireDeath))) {
                for(int x = (int)this.position().x() - shadowfireWidth; x <= (int)this.position().x() + shadowfireWidth; x++) {
                    for(int y = (int)this.position().y() - shadowfireHeight; y <= (int)this.position().y() + shadowfireHeight; y++) {
                        for(int z = (int)this.position().z() - shadowfireWidth; z <= (int)this.position().z() + shadowfireWidth; z++) {
                            Block block = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
                            if(block != Blocks.AIR && block != ObjectManager.getBlock("shadowfire")) {
                                BlockPos placePos = new BlockPos(x, y + 1, z);
                                Block upperBlock = this.getCommandSenderWorld().getBlockState(placePos).getBlock();
                                if(upperBlock == Blocks.AIR) {
                                    this.getCommandSenderWorld().setBlockAndUpdate(placePos, ObjectManager.getBlock("shadowfire").defaultBlockState().setValue(BlockFireBase.PERMANENT, permanent));
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {}
        super.die(damageSource);
    }

    @Override
    public boolean daylightBurns() {
        return !this.isBaby() && !this.isMinion();
    }
}
