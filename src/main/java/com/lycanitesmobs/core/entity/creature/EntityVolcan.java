package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;

public class EntityVolcan extends TameableCreatureEntity implements IMob {

	public int blockMeltingRadius = 2;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityVolcan(EntityType<? extends EntityVolcan> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;
        
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AttackMeleeGoal(this).setLongMemory(true));
    }

	@Override
	public void loadCreatureFlags() {
		this.blockMeltingRadius = this.creatureInfo.getFlag("blockMeltingRadius", this.blockMeltingRadius);
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

		// Burning Aura Attack:
		if(!this.getCommandSenderWorld().isClientSide && this.updateTick % 40 == 0) {
			List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 4);
			for(Object entityObj : aoeTargets) {
				LivingEntity target = (LivingEntity)entityObj;
				if(target != this && this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().canSee(target)) {
					target.setSecondsOnFire(2);
				}
			}
		}

		// Melt Blocks:
		if(!this.getCommandSenderWorld().isClientSide && this.updateTick % 40 == 0 && this.blockMeltingRadius > 0 && !this.isTamed() && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			int range = this.blockMeltingRadius;
			for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); w++) {
				for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width) + range); d++) {
					for (int h = -((int) Math.ceil(this.getDimensions(Pose.STANDING).height) + range); h <= Math.ceil(this.getDimensions(Pose.STANDING).height); h++) {
						Block block = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(w, h, d)).getBlock();
						if (block == Blocks.COBBLESTONE || block == Blocks.GRAVEL) {
							BlockState blockState = Blocks.LAVA.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, 5);
							this.getCommandSenderWorld().setBlockAndUpdate(this.blockPosition().offset(w, h, d), blockState);
						}
						/*else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.ICE || block == Blocks.SNOW) {
							this.getEntityWorld().setBlockState(this.getPosition().add(w, h, d), Blocks.AIR.getDefaultState(), 3);
						}*/
					}
				}
			}
		}

		// Particles:
		if(this.getCommandSenderWorld().isClientSide) {
			for(int i = 0; i < 2; ++i) {
				this.getCommandSenderWorld().addParticle(ParticleTypes.FLAME, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
				this.getCommandSenderWorld().addParticle(ParticleTypes.DRIPPING_LAVA, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
			}
			if(this.tickCount % 10 == 0)
				for(int i = 0; i < 2; ++i) {
					this.getCommandSenderWorld().addParticle(ParticleTypes.FLAME, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
				}
		}
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;

        // Silverfish Extermination:
        if(target instanceof SilverfishEntity) {
            target.remove();
        }
        
        return true;
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() { return true; }

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
			this.replacePlayersItem(player, itemStack, new ItemStack(Items.LAVA_BUCKET));
			return true;
		}

		return super.performCommand(command, player, itemStack);
	}
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }


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
    	if(type.equals("cactus") || type.equals("inWall")) return false;
    	    return super.isVulnerableTo(type, source, damage);
    }
    
    @Override
    public boolean canBurn() { return false; }

	@Override
	public boolean waterDamage() { return true; }
}
