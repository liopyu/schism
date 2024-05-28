package com.lycanitesmobs.core.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.spawner.Spawner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;

public class CropBlockSpawnTrigger extends BlockSpawnTrigger {

	/** Constructor **/
	public CropBlockSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}


	@Override
	public boolean isTriggerBlock(BlockState blockState, World world, BlockPos blockPos, int fortune, @Nullable LivingEntity entity) {
		Block block = blockState.getBlock();
		return block instanceof IPlantable || block instanceof VineBlock;
	}

	@Override
	public int getBlockLevel(BlockState blockState, World world, BlockPos blockPos) {
		return 0;
	}
}
