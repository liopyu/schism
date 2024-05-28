package com.lycanitesmobs.core.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.spawner.Spawner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TreeBlockSpawnTrigger extends BlockSpawnTrigger {

	/** Constructor **/
	public TreeBlockSpawnTrigger(Spawner spawner) {
		super(spawner);
	}

	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);
	}


	@Override
	public boolean isTriggerBlock(BlockState blockState, World world, BlockPos blockPos, int fortune, @Nullable LivingEntity entity) {
		return this.isTreeLogBlock(blockState.getBlock(), world, blockPos) || this.isTreeLeavesBlock(blockState.getBlock(), world, blockPos);
	}

	public boolean isTreeLogBlock(Block block, World world, BlockPos pos) {
		if(this.isLog(world.getBlockState(pos))) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			for(int searchX = x - 1; searchX <= x + 1; searchX++) {
				for(int searchZ = z - 1; searchZ <= z + 1; searchZ++) {
					for(int searchY = y; searchY <= Math.min(world.getMaxBuildHeight(), y + 32); searchY++) {
						if(this.isLeaves(world.getBlockState(new BlockPos(searchX, searchY, searchZ))))
							return true;
						if(!world.isEmptyBlock(new BlockPos(x, searchY, z)))
							break;
					}
				}
			}
		}
		String blockName = block.getRegistryName().toString();
		if((blockName.contains("tree") || blockName.contains("traverse")) && blockName.contains("branch")) {
			return true;
		}
		return false;
	}

	public boolean isTreeLeavesBlock(Block block, World world, BlockPos pos) {
		if(this.isLeaves(world.getBlockState(pos))) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			for(int searchX = x - 1; searchX <= x + 1; searchX++) {
				for(int searchZ = z - 1; searchZ <= z + 1; searchZ++) {
					for(int searchY = y; searchY >= Math.max(0, y - 32); searchY--) {
						if(this.isLog(world.getBlockState(new BlockPos(searchX, searchY, searchZ))))
							return true;
						if(!world.isEmptyBlock(new BlockPos(x, searchY, z)))
							break;
					}
				}
			}
		}
		String blockName = block.getRegistryName().toString();
		if((blockName.contains("tree") || blockName.contains("traverse")) && blockName.contains("leaves")) {
			return true;
		}
		return false;
	}

	public boolean isLog(BlockState blockState) {
		Block block = blockState.getBlock();
		if(block.is(BlockTags.LOGS)) {
			return true;
		}
		return false;
	}

	public boolean isLeaves(BlockState blockState) {
		Block block = blockState.getBlock();
		if(block instanceof LeavesBlock || block.is(BlockTags.LEAVES)) {
			return true;
		}
		return false;
	}
}
