package com.lycanitesmobs.core.entity.goals.actions;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class BreakDoorGoal extends DoorInteractGoal {
	//Properties:
    private int breakingTime;
    private int lastBreakTime = -1;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public BreakDoorGoal(MobEntity setHost) {
        super(setHost);
    }

	
	// ==================================================
 	//                  Should Execute
 	// ==================================================
	@Override
    public boolean canUse() {
    	if(!super.canUse())
    		return false;
    	if(!this.host.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
    		return false;
    	return true;
    }

	
	// ==================================================
 	//                      Start
 	// ==================================================
	@Override
    public void start() {
        super.start();
        this.breakingTime = 0;
    }

	
	// ==================================================
 	//                Continue Executing
 	// ==================================================
	@Override
    public boolean canContinueToUse() {
		return this.breakingTime <= 240 && !this.canDestroy() && this.doorPosition.closerThan(this.host.position(), 2.0D);
    }

	protected boolean canDestroy() {
		if (!this.doorInteract) {
			return false;
		} else {
			BlockState blockState = this.host.level.getBlockState(this.doorPosition);
			if (!(blockState.getBlock() instanceof DoorBlock)) {
				this.doorInteract = false;
				return false;
			} else {
				return blockState.getValue(DoorBlock.OPEN);
			}
		}
	}

	
	// ==================================================
 	//                      Reset
 	// ==================================================
	@Override
    public void stop() {
        super.stop();
        this.host.getCommandSenderWorld().destroyBlockProgress(this.host.getId(), new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ), -1);
    }

	
	// ==================================================
 	//                     Update
 	// ==================================================
	@Override
    public void tick() {
        super.tick();

        if(this.host.getRandom().nextInt(20) == 0)
            this.host.getCommandSenderWorld().levelEvent(1010, new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ), 0);

        ++this.breakingTime;
        int breaking = (int)((float)this.breakingTime / 240.0F * 10.0F);

        if(breaking != this.lastBreakTime) {
            this.host.getCommandSenderWorld().destroyBlockProgress(this.host.getId(), new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ), breaking);
            this.lastBreakTime = breaking;
        }

        if(this.breakingTime == 240 && this.host.getCommandSenderWorld().getDifficulty() == Difficulty.HARD) {
            this.host.getCommandSenderWorld().removeBlock(new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ), true);
            this.host.getCommandSenderWorld().levelEvent(1012, new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ), 0);
            this.host.getCommandSenderWorld().levelEvent(2001, new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ), 0);
        }
    }
}
