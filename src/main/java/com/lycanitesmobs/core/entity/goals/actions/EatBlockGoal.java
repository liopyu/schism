package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class EatBlockGoal extends Goal {
	// Targets:
    private BaseCreatureEntity host;
    
    // Properties:
    private Block[] blocks = new Block[0];
    private Material[] materials = new Material[0];
    private Block replaceBlock = Blocks.AIR;
    private int eatTime = 40;
    private int eatTimeMax = 40;


    public EatBlockGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

     public EatBlockGoal setBlocks(Block... setBlocks) {
    	this.blocks = setBlocks;
     	return this;
     }

     public EatBlockGoal setMaterials(Material... setMaterials) {
    	this.materials = setMaterials;
     	return this;
     }

     public EatBlockGoal setReplaceBlock(Block block) {
    	this.replaceBlock = block;
     	return this;
     }

     public EatBlockGoal setEatTime(int setTime) {
    	this.eatTimeMax = setTime;
     	return this;
     }

 	@Override
    public boolean canUse() {
    	 if(this.host.getRandom().nextInt(this.host.isBaby() ? 50 : 1000) != 0)
             return false;
    	 
    	 int i = MathHelper.floor(this.host.position().x());
         int j = MathHelper.floor(this.host.position().y());
         int k = MathHelper.floor(this.host.position().z());

         BlockState blockState = this.host.getCommandSenderWorld().getBlockState(new BlockPos(i, j - 1, k));
         return this.isValidBlock(blockState);
     }

     public boolean isValidBlock(BlockState blockState) {
         for(Block edibleBlock : this.blocks) {
        	 if(edibleBlock == blockState.getBlock())
        		 return true;
         }
         
         Material material = blockState.getMaterial();
         for(Material edibleMaterial : this.materials) {
        	 if(edibleMaterial == material)
        		 return true;
         }
         
         return false;
     }

 	@Override
    public void start() {
    	 this.eatTime = this.eatTimeMax;
         this.host.clearMovement();
     }

 	@Override
    public void stop() {
    	 this.eatTime = this.eatTimeMax;
     }

  	@Override
    public boolean canContinueToUse() {
    	  return this.eatTime > 0;
      }

 	@Override
    public void tick() {
         if(--this.eatTime != 0) return;
         
         int i = MathHelper.floor(this.host.position().x());
         int j = MathHelper.floor(this.host.position().y());
         int k = MathHelper.floor(this.host.position().z());
         BlockState blockState = this.host.getCommandSenderWorld().getBlockState(new BlockPos(i, j - 1, k));
         
         if(this.isValidBlock(blockState)) {
             //if(this.host.getEntityWorld().getGameRules().getGameRuleBooleanValue("mobGriefing"))
        	 this.host.getCommandSenderWorld().removeBlock(new BlockPos(i, j - 1, k), true); // Might be something else was x, y, z, false
         }

         this.host.getCommandSenderWorld().levelEvent(2001, new BlockPos(i, j - 1, k), Block.getId(blockState));
         this.host.getCommandSenderWorld().setBlock(new BlockPos(i, j - 1, k), this.replaceBlock.defaultBlockState(), 2);
         this.host.onEat();
     }
}
