package com.lycanitesmobs.core.spawner.trigger;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.spawner.Spawner;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SleepSpawnTrigger extends BlockSpawnTrigger {
	/** Constructor **/
	public SleepSpawnTrigger(Spawner spawner) {
		super(spawner);
	}



	/** Called every time a player attempts to use a bed. **/
	public boolean onSleep(World world, PlayerEntity player, BlockPos spawnPos, BlockState blockState) {
		ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

		// Chance:
		if(this.chance < 1 && player.getRandom().nextDouble() > this.chance) {
			return false;
		}

		// Check Block:
		if(!this.isTriggerBlock(blockState, world, spawnPos, 0, player)) {
			return false;
		}

		// Trigger:
		boolean success = this.trigger(world, player, spawnPos, 0, 0);
		if(this.cooldown > -1 && playerExt != null) {
			this.playerUsedTicks.put(player, playerExt.timePlayed);
		}
		return success;
	}
}
