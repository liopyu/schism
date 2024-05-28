package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class EntityBobeko extends AgeableCreatureEntity {
    public EntityBobeko(EntityType<? extends EntityBobeko> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = false;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

	@Override
    public void aiStep() {
        super.aiStep();
        
        // Trail:
        if(!this.getCommandSenderWorld().isClientSide && (this.tickCount % 10 == 0 || this.isMoving() && this.tickCount % 5 == 0)) {
            int trailHeight = 2;
            if(this.isBaby())
                trailHeight = 1;
            for(int y = 0; y < trailHeight; y++) {
                Block block = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, y, 0)).getBlock();
                if(block == Blocks.AIR || block == Blocks.SNOW || block == ObjectManager.getBlock("frostcloud"))
                    this.getCommandSenderWorld().setBlockAndUpdate(this.blockPosition().offset(0, y, 0), ObjectManager.getBlock("frostcloud").defaultBlockState());
            }
        }
        
        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
	        for(int i = 0; i < 2; ++i) {
	            this.getCommandSenderWorld().addParticle(ParticleTypes.ITEM_SNOWBALL, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
	        }
    }

	@Override
	public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(blockState.getMaterial() == Material.GRASS || blockState.getMaterial() == Material.TOP_SNOW)
                return 10F;
            if(blockState.getMaterial() == Material.DIRT || blockState.getMaterial() == Material.ICE)
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    @Override
    public boolean canBeLeashed(PlayerEntity player) {
	    return true;
    }

    @Override
    public int getNoBagSize() { return 0; }

    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }

    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
        if(type.equals("ooze")) return false;
        return super.isVulnerableTo(type, source, damage);
    }

    @Override
    public boolean canBeAffected(EffectInstance potionEffect) {
        if(potionEffect.getEffect() == Effects.MOVEMENT_SLOWDOWN) return false;
        if(potionEffect.getEffect() == Effects.HUNGER) return false;
        return super.canBeAffected(potionEffect);
    }

    @Override
    public HashMap<Integer, String> getInteractCommands(PlayerEntity player, ItemStack itemStack) {
    	HashMap<Integer, String> commands = new HashMap<Integer, String>();
    	commands.putAll(super.getInteractCommands(player, itemStack));
    	
    	if(itemStack != null) {
    		// Milk:
    		if(itemStack.getItem() == Items.BUCKET)
    			commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Milk");
    	}
    	
    	return commands;
    }

    @Override
    public boolean performCommand(String command, PlayerEntity player, ItemStack itemStack) {
    	
    	// Milk:
    	if(command.equals("Milk")) {
    		this.replacePlayersItem(player, itemStack, new ItemStack(Items.MILK_BUCKET));
    		return true;
    	}
    	
    	return super.performCommand(command, player, itemStack);
    }
}
