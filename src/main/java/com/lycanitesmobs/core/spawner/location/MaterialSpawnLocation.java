package com.lycanitesmobs.core.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MaterialSpawnLocation extends BlockSpawnLocation {
    /** A list of block materials to either spawn in or not spawn in depending on if it is a blacklist or whitelist. **/
    public List<Material> materials = new ArrayList<>();


	@Override
	public void loadFromJSON(JsonObject json) {
		this.materials = JSONHelper.getJsonMaterials(json);

		super.loadFromJSON(json);
	}

	/** Returns if the provided block position is valid. **/
	@Override
	public boolean isValidBlock(World world, BlockPos blockPos) {
		BlockState blockState = world.getBlockState(blockPos);

		if(!this.surface || !this.underground) {
			if(world.canSeeSkyFromBelowWater(blockPos)) {
				if(!this.surface) {
					return false;
				}
			}
			else {
				if(!this.underground) {
					return false;
				}
			}
		}

		if("blacklist".equalsIgnoreCase(this.listType)) {
			return !this.materials.contains(blockState);
		}
		else {
			return this.materials.contains(blockState);
		}
	}
}
