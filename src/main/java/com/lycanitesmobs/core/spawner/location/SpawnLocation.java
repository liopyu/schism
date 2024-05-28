package com.lycanitesmobs.core.spawner.location;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.spawner.CoordSorterFurthest;
import com.lycanitesmobs.core.spawner.CoordSorterNearest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SpawnLocation {
    /** Spawn Locations define where spawns will take place, how these work can vary based on the type of Spawn Trigger. **/

    /** The minimum xyz distances in blocks from the central spawn position to spawn from. Required. **/
    public Vector3i rangeMin = new Vector3i(0, 0, 0);

    /** The maximum xyz distances in blocks from the central spawn position to spawn from. Required. **/
    public Vector3i rangeMax = new Vector3i(0, 0, 0);

    /** The minimum allowed y height. **/
    public int yMin = -1;

	/** The maximum allowed y height. **/
	public int yMax = -1;

    /** Determines the order that the returned positions should be in. Can be: difficulty, random, near or far (from the trigger position). **/
    public String sorting = "difficulty";


	/** Loads this Spawn Condition from the provided JSON data. **/
	public static SpawnLocation createFromJSON(JsonObject json) {
		String type = json.get("type").getAsString();
		SpawnLocation spawnLocation = null;

		if("base".equalsIgnoreCase(type)) {
			spawnLocation = new SpawnLocation();
		}
		else if("random".equalsIgnoreCase(type)) {
			spawnLocation = new RandomSpawnLocation();
		}
		else if("block".equalsIgnoreCase(type)) {
			spawnLocation = new BlockSpawnLocation();
		}
		else if("material".equalsIgnoreCase(type)) {
			spawnLocation = new MaterialSpawnLocation();
		}
		else if("structure".equalsIgnoreCase(type)) {
			spawnLocation = new StructureSpawnLocation();
		}

		spawnLocation.loadFromJSON(json);
		return spawnLocation;
	}


    /** Loads this Spawn Location from the provided JSON data. **/
    public void loadFromJSON(JsonObject json) {
		this.rangeMin = JSONHelper.getVector3i(json, "rangeMin");

		this.rangeMax = JSONHelper.getVector3i(json, "rangeMax");

		if(json.has("yMin"))
			this.yMin = json.get("yMin").getAsInt();

		if(json.has("yMax"))
			this.yMax = json.get("yMax").getAsInt();

		if(json.has("sorting"))
			this.sorting = json.get("sorting").getAsString();
    }


    /** Returns a list of positions to spawn at. **/
    public List<BlockPos> getSpawnPositions(World world, PlayerEntity player, BlockPos triggerPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();
        int yPos = this.getOffset(world.random, this.rangeMin.getY(), this.rangeMax.getY());
		if((this.yMax < 0 || yPos <= this.yMax) && (this.yMin < 0 || yPos >= this.yMin)) {
			Vector3i offset = new Vector3i(
					this.getOffset(world.random, this.rangeMin.getX(), this.rangeMax.getX()),
					yPos,
					this.getOffset(world.random, this.rangeMin.getZ(), this.rangeMax.getZ())
			);
			spawnPositions.add(triggerPos.offset(offset));
		}

        return this.sortSpawnPositions(spawnPositions, world, triggerPos);
    }


    /** Returns a random offset from the provided min and max values. **/
    public int getOffset(Random random, int min, int max) {
        if(max <= min) {
            return 0;
        }
        int offset = min + random.nextInt(max - min);
        if(random.nextBoolean()) {
            offset = -offset;
        }
        return offset;
    }


	/** Sorts a list of spawning positions. **/
	public List<BlockPos> sortSpawnPositions(List<BlockPos> spawnPositions, World world, BlockPos triggerPos) {
		String sorting = this.sorting;

		if("difficulty".equalsIgnoreCase(this.sorting)) {
			sorting = "random";
			if(world.getDifficulty().getId() <= 1) {
				sorting = "far";
			}
			else if(world.getDifficulty().getId() >= 3) {
				sorting = "near";
			}
		}

		if("random".equalsIgnoreCase(sorting)) {
			Collections.shuffle(spawnPositions);
		}
		else if("near".equalsIgnoreCase(sorting)) {
			Collections.sort(spawnPositions, new CoordSorterNearest(triggerPos));
		}
		else if("far".equalsIgnoreCase(sorting)) {
			Collections.sort(spawnPositions, new CoordSorterFurthest(triggerPos));
		}
		return spawnPositions;
	}
}
