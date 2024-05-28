package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.CustomItemEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.IMob;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityKhalk extends TameableCreatureEntity implements IMob, IGroupHeavy {

    public boolean lavaDeath = true;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityKhalk(EntityType<? extends EntityKhalk> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.isLavaCreature = true;
        this.hasAttackSound = true;

        this.canGrow = true;
        this.babySpawnChance = 0.01D;

        this.solidCollision = true;
        this.setupMob();

        this.setPathfindingMalus(PathNodeType.LAVA, 0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }

	@Override
	public void loadCreatureFlags() {
		this.lavaDeath = this.creatureInfo.getFlag("lavaDeath", this.lavaDeath);
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Random Lunging:
        if(this.onGround && !this.getCommandSenderWorld().isClientSide) {
        	if(this.hasAttackTarget()) {
        		if(this.random.nextInt(10) == 0)
        			this.leap(6.0F, 0.1D, this.getTarget());
        	}
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
        if(this.lavaContact())
            return 2.0F;
        return 1.0F;
    }

    // Pathing Weight:
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        int waterWeight = 10;
        BlockPos pos = new BlockPos(x, y, z);
        if(this.getCommandSenderWorld().getBlockState(pos).getBlock() == Blocks.LAVA)
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);

        if(this.getTarget() != null)
            return super.getBlockPathWeight(x, y, z);
        if(this.lavaContact())
            return -999999.0F;

        return super.getBlockPathWeight(x, y, z);
    }

    // Pushed By Water:
    @Override
    public boolean isPushedByFluid() {
        return false;
    }
    
    
    // ==================================================
   	//                      Death
   	// ==================================================
    @Override
    public void die(DamageSource damageSource) {
		if(!this.getCommandSenderWorld().isClientSide && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.lavaDeath && !this.isTamed()) {
			int lavaWidth = (int)Math.floor(this.getDimensions(Pose.STANDING).width) - 1;
			int lavaHeight = (int)Math.floor(this.getDimensions(Pose.STANDING).height) - 1;
			for(int x = (int)this.position().x() - lavaWidth; x <= (int)this.position().x() + lavaWidth; x++) {
				for(int y = (int)this.position().y(); y <= (int)this.position().y() + lavaHeight; y++) {
					for(int z = (int)this.position().z() - lavaWidth; z <= (int)this.position().z() + lavaWidth; z++) {
						Block block = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
						if(block == Blocks.AIR) {
							BlockState blockState = Blocks.LAVA.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, 4);
							if(x == (int)this.position().x() && y == (int)this.position().y() && z == (int)this.position().z())
								blockState = blockState = Blocks.LAVA.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, 5);
							this.getCommandSenderWorld().setBlock(new BlockPos(x, y, z), blockState, 3);
						}
					}
				}
			}
		}
        super.die(damageSource);
    }

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
    
    @Override
    public boolean waterDamage() { return true; }
    
    @Override
    public boolean canBreatheUnderlava() {
        return true;
    }
    
    @Override
    public boolean canBreatheAir() {
        return true;
    }
    
    
    // ==================================================
   	//                    Taking Damage
   	// ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
    	if(damageSrc.isFire())
    		return 0F;
    	else return super.getDamageModifier(damageSrc);
    }
    
    
    // ==================================================
   	//                       Drops
   	// ==================================================
    // ========== Apply Drop Effects ==========
    /** Used to add effects or alter the dropped entity item. **/
    @Override
    public void applyDropEffects(CustomItemEntity entityitem) {
    	entityitem.setCanBurn(false);
    }
    
    
    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }
    
    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
	

    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }
}
