package com.lycanitesmobs.core.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.spawner.Spawner;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MixBlockSpawnTrigger extends BlockSpawnTrigger {

	/** Constructor **/
	public MixBlockSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}

	@Override
	public int getBlockLevel(BlockState blockState, World world, BlockPos blockPos) {
		return 0;
	}


	/** Called every time liquids mix to form a block. **/
	public void onMix(World world, BlockState blockState, BlockPos mixPos) {
		// Check Block:
		if(!this.isTriggerBlock(blockState, world, mixPos, 0, null)) {
			return;
		}

		// Chance:
		if(this.chance < 1 && world.random.nextDouble() > this.chance) {
			return;
		}

		this.trigger(world, null, mixPos.above(), this.getBlockLevel(blockState, world, mixPos), 0);
	}
}
