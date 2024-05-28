package com.lycanitesmobs.core.worldgen;

import com.google.common.collect.ImmutableSet;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.fluid.BaseFluidBlock;
import com.lycanitesmobs.core.info.ItemManager;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class WorldGenManager {
	private static WorldGenManager INSTANCE;
	public static WorldGenManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new WorldGenManager();
		}
		return INSTANCE;
	}
	
	// Placements:
	public AlwaysPlacement alwaysPlacement = new AlwaysPlacement(NoPlacementConfig.CODEC);
	
	// Features:
	public ChunkSpawnFeature chunkSpawnFeature = new ChunkSpawnFeature(NoFeatureConfig.CODEC);
	public DungeonFeature dungeonFeature = new DungeonFeature(NoFeatureConfig.CODEC);
	public Map<String, ConfiguredFeature<?, ?>> fluidConfiguredFeatures = new HashMap<>();

	/**
	 * Called on startup after all LM assets have been defined but before mob startup events.
	 */
	public void startup() {
		for (String fluidName : ItemManager.getInstance().worldgenFluidBlocks.keySet()) {
			BaseFluidBlock fluidBlock = ItemManager.getInstance().worldgenFluidBlocks.get(fluidName);
			ConfiguredFeature<?, ?> lakeFeature = null;
			ConfiguredFeature<?, ?> springFeature = null;
			LiquidsConfig springConfig = new LiquidsConfig(fluidBlock.getFluid().defaultFluidState(), true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE));

			if (fluidBlock.defaultBlockState().getMaterial() == Material.WATER) {
				lakeFeature = Feature.LAKE.configured(new BlockStateFeatureConfig(fluidBlock.defaultBlockState())).decorated(Placement.WATER_LAKE.configured(new ChanceConfig(40)));
				springFeature = Feature.SPRING.configured(springConfig).decorated(Placement.RANGE_BIASED.configured(new TopSolidRangeConfig(8, 8, 256))).squared().count(20);
			}
			else {
				lakeFeature = Feature.LAKE.configured(new BlockStateFeatureConfig(fluidBlock.defaultBlockState())).decorated(Placement.LAVA_LAKE.configured(new ChanceConfig(40)));
				springFeature = Feature.SPRING.configured(springConfig).decorated(Placement.RANGE_VERY_BIASED.configured(new TopSolidRangeConfig(8, 16, 256))).squared().count(20);
			}

			fluidConfiguredFeatures.put(fluidName + "_lake", lakeFeature);
			fluidConfiguredFeatures.put(fluidName + "_spring", springFeature);
		}
	}

	@SubscribeEvent
	public void registerPlacements(RegistryEvent.Register<Placement<?>> event) {
		event.getRegistry().register(this.alwaysPlacement.setRegistryName(LycanitesMobs.modInfo.modid, "always"));
	}

	@SubscribeEvent
	public void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry().register(this.chunkSpawnFeature.setRegistryName(LycanitesMobs.modInfo.modid, "chunkspawn"));
		event.getRegistry().register(this.dungeonFeature.setRegistryName(LycanitesMobs.modInfo.modid, "dungeon"));
	}
}
