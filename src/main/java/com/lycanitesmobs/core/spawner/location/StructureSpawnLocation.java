package com.lycanitesmobs.core.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.helpers.JSONHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class StructureSpawnLocation extends RandomSpawnLocation {

	/** The structure ids to use as spawn locations. **/
	public List<ResourceLocation> structureIds = new ArrayList<>();

	/** How close to the player (in blocks) Structures must be. Default: 100. **/
	public int structureRange = 100;


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		if(json.has("structureIds")) {
			List<String> structureIdStrings = JSONHelper.getJsonStrings(json.getAsJsonArray("structureIds"));
			structureIds.clear();
			for(String structureIdName : structureIdStrings) {
				structureIds.add(new ResourceLocation(structureIdName));
			}
		}

		if(json.has("structureRange"))
			this.structureRange = json.get("structureRange").getAsInt();
	}


	@Override
	public List<BlockPos> getSpawnPositions(World world, PlayerEntity player, BlockPos triggerPos) {
		if (!(world instanceof ServerWorld)) {
			LycanitesMobs.logWarning("", "[JSONSpawner] Structure spawn location was called with a non ServerWorld World instance.");
			return new ArrayList<>();
		}

		List<BlockPos> spawnPositions = new ArrayList<>();
		for(ResourceLocation structureId : this.structureIds) {
			Structure<?> spawnStructure = RegistryManager.ACTIVE.getRegistry(Registry.STRUCTURE_FEATURE_REGISTRY).getValue(structureId);
			if (spawnStructure == null) {
				continue;
			}
			LycanitesMobs.logDebug("JSONSpawner", "Getting Nearest " + structureId + " Structures Within Range");

			BlockPos structurePos = null;
			try {
				structurePos = ((ServerWorld) world).findNearestMapFeature(spawnStructure, triggerPos, this.structureRange, false);
			} catch (Exception e) {}

			// No Structure:
			if (structurePos == null) {
				LycanitesMobs.logDebug("JSONSpawner", "No " + structureId + " Structures found.");
				continue;
			}

			// Too Far:
			double structureDistance = Math.sqrt(structurePos.distSqr(triggerPos));
			if (structureDistance > this.structureRange) {
				LycanitesMobs.logDebug("JSONSpawner", "No " + structureId + " Structures within range, nearest was: " + structureDistance + "/" + this.structureRange + " at: " + structurePos);
				continue;
			}

			// Structure Found:
			LycanitesMobs.logDebug("JSONSpawner", "Found a " + structureId + " Structure within range, at: " + structurePos + " distance: " + structureDistance + "/" + this.structureRange);
			spawnPositions.addAll(super.getSpawnPositions(world, player, structurePos));
		}

		return this.sortSpawnPositions(spawnPositions, world, triggerPos);
	}

}
