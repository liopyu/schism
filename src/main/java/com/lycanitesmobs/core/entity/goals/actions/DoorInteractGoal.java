package com.lycanitesmobs.core.entity.goals.actions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal.Flag;

public abstract class DoorInteractGoal extends Goal {
	// Targets:
    protected MobEntity host;
    protected BlockPos doorPosition;
    protected boolean doorInteract;
    private boolean hasStoppedDoorInteraction;

    // Properties:
    protected int entityPosX;
    protected int entityPosY;
    protected int entityPosZ;
    float entityPositionX;
    float entityPositionZ;

	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public DoorInteractGoal(MobEntity setHost) {
        this.doorPosition = BlockPos.ZERO;
        this.host = setHost;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

	
	// ==================================================
 	//                  Should Execute
 	// ==================================================
	@Override
    public boolean canUse() {
        if(!this.host.horizontalCollision)
            return false;

        Path pathEntity = this.host.getNavigation().getPath();

        if(pathEntity != null && !pathEntity.isDone()) {
            for(int i = 0; i < Math.min(pathEntity.getNextNodeIndex() + 2, pathEntity.getNodeCount()); ++i) {
                PathPoint pathpoint = pathEntity.getNode(i);
                this.entityPosX = pathpoint.x;
                this.entityPosY = pathpoint.y + 1;
                this.entityPosZ = pathpoint.z;

                if(this.host.distanceToSqr((double)this.entityPosX, this.host.position().y(), (double)this.entityPosZ) <= 2.25D) {
                    BlockPos possibleDoorPos = new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ);
                    if(this.isDoor(possibleDoorPos)) {
                        this.doorPosition = possibleDoorPos;
                        this.doorInteract = true;
                        return true;
                    }
                }
            }

            this.entityPosX = MathHelper.floor(this.host.position().x());
            this.entityPosY = MathHelper.floor(this.host.position().y() + 1.0D);
            this.entityPosZ = MathHelper.floor(this.host.position().z());
            BlockPos possibleDoorPos = new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ);
            if(this.isDoor(possibleDoorPos)) {
                this.doorPosition = possibleDoorPos;
                this.doorInteract = true;
                return true;
            }
        }

        this.doorInteract = false;
        return false;
    }

	
	// ==================================================
 	//                      Start
 	// ==================================================
	@Override
    public void start() {
        this.hasStoppedDoorInteraction = false;
        this.entityPositionX = (float)((double)((float)this.entityPosX + 0.5F) - this.host.position().x());
        this.entityPositionZ = (float)((double)((float)this.entityPosZ + 0.5F) - this.host.position().z());
    }

	
	// ==================================================
 	//                Continue Executing
 	// ==================================================
	@Override
    public boolean canContinueToUse() {
        return !this.hasStoppedDoorInteraction;
    }

	
	// ==================================================
 	//                     Update
 	// ==================================================
	@Override
    public void tick() {
        float f = (float)((double)((float)this.entityPosX + 0.5F) - this.host.position().x());
        float f1 = (float)((double)((float)this.entityPosZ + 0.5F) - this.host.position().z());
        float f2 = this.entityPositionX * f + this.entityPositionZ * f1;

        if(f2 < 0.0F)
            this.hasStoppedDoorInteraction = true;
    }

	
	// ==================================================
 	//                   Is Door
 	// ==================================================
    private boolean isDoor(BlockPos pos) {
        BlockState iblockstate = this.host.getCommandSenderWorld().getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block instanceof DoorBlock && iblockstate.getMaterial() == Material.WOOD;
    }
}
