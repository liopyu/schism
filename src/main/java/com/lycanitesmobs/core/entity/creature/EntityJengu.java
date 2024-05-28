package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.api.IFusable;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.HashMap;

public class EntityJengu extends TameableCreatureEntity implements IMob, IFusable {

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityJengu(EntityType<? extends EntityJengu> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.spawnsInWater = true;
        this.hasAttackSound = false;
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(5.0F));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
	        for(int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.CURRENT_DOWN, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
                this.getCommandSenderWorld().addParticle(ParticleTypes.DRIPPING_WATER, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("aquapulse", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        super.attackRanged(target, range);
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================

    @Override
    public float getAISpeedModifier() {
        if(this.isInWater()) // Checks specifically just for water.
            return 1F;
        if(this.waterContact()) // Checks for water, rain, etc.
            return 0.75F;
        return 0.5F;
    }

    @Override
    public boolean isFlying() { return true; }

    @Override
    public boolean isStrongSwimmer() { return true; }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }
    
    @Override
    public boolean canBurn() { return false; }


    // ==================================================
    //                     Interact
    // ==================================================
    // ========== Get Interact Commands ==========
    @Override
    public HashMap<Integer, String> getInteractCommands(PlayerEntity player, ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<>();
        commands.putAll(super.getInteractCommands(player, itemStack));

        if(itemStack != null) {
            // Water:
            if(itemStack.getItem() == Items.BUCKET && this.isTamed())
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Water");
        }

        return commands;
    }

    // ========== Perform Command ==========
    @Override
    public boolean performCommand(String command, PlayerEntity player, ItemStack itemStack) {

        // Water:
        if(command.equals("Water")) {
            this.replacePlayersItem(player, itemStack, new ItemStack(Items.WATER_BUCKET));
            return true;
        }

        return super.performCommand(command, player, itemStack);
    }


    // ==================================================
    //                      Fusion
    // ==================================================
    protected IFusable fusionTarget;

    @Override
    public IFusable getFusionTarget() {
        return this.fusionTarget;
    }

    @Override
    public void setFusionTarget(IFusable fusionTarget) {
        this.fusionTarget = fusionTarget;
    }

    @Override
    public EntityType<? extends LivingEntity> getFusionType(IFusable fusable) {
        if(fusable instanceof EntityCinder) {
            return CreatureManager.getInstance().getEntityType("xaphan");
        }
        if(fusable instanceof EntityGeonach) {
            return CreatureManager.getInstance().getEntityType("spriggan");
        }
        if(fusable instanceof EntityZephyr) {
            return CreatureManager.getInstance().getEntityType("reiver");
        }
        if(fusable instanceof EntityAegis) {
            return CreatureManager.getInstance().getEntityType("nymph");
        }
        if(fusable instanceof EntityArgus) {
            return CreatureManager.getInstance().getEntityType("eechetik");
        }
        return null;
    }
}
