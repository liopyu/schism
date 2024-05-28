package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

public class FindNearbyPlayersGoal extends Goal {
	BaseCreatureEntity host;

    // Properties:
	private double searchRange = 64D;

	private int searchTime = 0;
	private int searchRate = 20;


	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public FindNearbyPlayersGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

	/**
	 * Sets the player search range (in blocks).
	 * @param searchRange The range to find blocks.
	 * @return This goal for chaining.
	 */
	public FindNearbyPlayersGoal setSearchRange(double searchRange) {
    	this.searchRange = searchRange;
    	return this;
    }

	@Override
    public boolean canUse() {
		return this.host.isAlive();
    }

	@Override
    public void tick() {
		if(this.searchTime++ % this.searchRate != 0) {
			return;
		}

		LivingEntity newTarget = null;
		try {
			this.host.playerTargets.clear();
			for(PlayerEntity player : this.host.getCommandSenderWorld().players()) {
				if(this.host.distanceTo(player) <= this.searchRange) {
					this.host.playerTargets.add(player);
				}
			}

		}
		catch (Exception e) {
			LycanitesMobs.logWarning("", "An exception occurred when player target selecting, this has been skipped to prevent a crash.");
			e.printStackTrace();
		}
    }
}
