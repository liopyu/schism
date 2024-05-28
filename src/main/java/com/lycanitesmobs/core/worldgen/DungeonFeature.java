package com.lycanitesmobs.core.worldgen;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.config.ConfigDungeons;
import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DungeonFeature extends Feature<NoFeatureConfig> {
	public DungeonFeature(Codec<NoFeatureConfig> configFactory) {
		super(configFactory);
	}

	@Override
	public boolean place(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		boolean enabled = ConfigDungeons.INSTANCE.dungeonsEnabled.get();
		int dungeonDistance = ConfigDungeons.INSTANCE.dungeonDistance.get();

		World world = reader.getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(!enabled || extendedWorld == null) {
			return false;
		}
		try {
			int dungeonSizeMax = dungeonDistance;
			ChunkPos chunkPos = new ChunkPos(pos);
			List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(chunkPos, dungeonSizeMax * 2);

			// Create New Instances:
			if (nearbyDungeons.isEmpty()) {
				for (int x = -1; x <= 1; x++) {
					for (int z = -1; z <= 1; z++) {
						if (x == 0 && z == 0) {
							continue;
						}
						if (x != 0 && z != 0) {
							continue;
						}
						LycanitesMobs.logDebug("Dungeon", "Creating A New Dungeon At Chunk: X" + (chunkPos.x + (dungeonSizeMax * x)) + " Z" + (chunkPos.z + (dungeonSizeMax * z)));
						DungeonInstance dungeonInstance = new DungeonInstance();
						int yPos = reader.getSeaLevel();
						if(yPos < 64) {
							yPos = 64;
						}
						BlockPos dungeonPos = new ChunkPos(chunkPos.x + (dungeonSizeMax * x), chunkPos.z + (dungeonSizeMax * z)).getWorldPosition().offset(7, yPos, 7);
						dungeonInstance.setOrigin(dungeonPos);
						if(dungeonInstance.init(world)) {
							extendedWorld.addDungeonInstance(dungeonInstance, new UUID(rand.nextLong(), rand.nextLong()));
							LycanitesMobs.logDebug("Dungeon", "Feature Created: " + dungeonInstance.toString());
						}
					}
				}
			}

			// Build Dungeons:
			nearbyDungeons = extendedWorld.getNearbyDungeonInstances(chunkPos, 0);
			for(DungeonInstance dungeonInstance : nearbyDungeons) {
				dungeonInstance.buildChunk(reader, world, chunkPos, rand);
			}
		}
		catch(Exception e) {
//			LycanitesMobs.logWarning("", "An exception occurred when trying to generate a dungeon.");
//			e.printStackTrace();
		}
		return true;
	}
}
