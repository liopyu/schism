package com.schism.core.generation;

import com.schism.core.Schism;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class LevelGenerator
{
    private static final LevelGenerator INSTANCE = new LevelGenerator();

    protected List<GenerationEntry> lakes = new ArrayList<>();
    protected List<GenerationEntry> springs = new ArrayList<>();

    public record GenerationEntry(Holder<PlacedFeature> featureHolder, List<String> biomeCategories) {}

    /**
     * Gets the singleton FeatureResolver instance.
     * @return The World Generation Feature Resolver, a utility for creating world generation features.
     */
    public static LevelGenerator get()
    {
        return INSTANCE;
    }

    /**
     * Registers a level generation from the provided data store.
     * @param registry The forge registry.
     * @param name The name to register the generation with.
     * @param coreBlock A core block to generate with.
     * @param dataStore The data store to get generation information from.
     */
    public void register(final IForgeRegistry<Feature<?>> registry, String name, Block coreBlock, DataStore dataStore)
    {
        List<String> biomeCategories = dataStore.listProp("biome_categories").stream().map(DataStore::stringValue).toList();
        String type = dataStore.stringProp("type");
        switch(type) {

            // Lake:
            case "lake" -> {
                Block stoneBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(dataStore.stringProp("stone_block")));
                if (stoneBlock == null) {
                    throw new RuntimeException("Invalid Lake Generation Stone Block ID: " + dataStore.stringProp("stone_block"));
                }
                BlockStateProvider stoneProvider = BlockStateProvider.simple(stoneBlock);
                BlockStateProvider coreProvider = BlockStateProvider.simple(coreBlock);
                LakeFeature.Configuration config = new LakeFeature.Configuration(coreProvider, stoneProvider);

                Feature<LakeFeature.Configuration> feature = new LakeFeature(LakeFeature.Configuration.CODEC);
                feature.setRegistryName(new ResourceLocation(Schism.NAMESPACE, name));
                registry.register(feature);

                List<PlacementModifier> placementModifiers = new ArrayList<>();
                if (!dataStore.booleanProp("underground")) {
                    placementModifiers.add(PlacementUtils.HEIGHTMAP_WORLD_SURFACE);
                } else {
                    placementModifiers.add(HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())));
                    placementModifiers.add(EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.allOf(BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE), BlockPredicate.insideWorld(new BlockPos(0, -5, 0))), 32));
                    placementModifiers.add(SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5));
                }
                placementModifiers.add(RarityFilter.onAverageOnceEvery(dataStore.intProp("average_chunk")));
                placementModifiers.add(InSquarePlacement.spread());
                placementModifiers.add(BiomeFilter.biome());

                ConfiguredFeature<LakeFeature.Configuration, Feature<LakeFeature.Configuration>> configuredFeature = new ConfiguredFeature<>(feature, config);
                Holder<PlacedFeature> featureHolder = PlacementUtils.register(name, new Holder.Direct<>(configuredFeature), placementModifiers);
                this.lakes.add(new GenerationEntry(featureHolder, biomeCategories));
            }

            // Spring:
            case "spring" -> {
                FluidState fluidState = Fluids.WATER.defaultFluidState();
                if (coreBlock instanceof LiquidBlock liquidBlock) {
                    fluidState = liquidBlock.getFluidState(coreBlock.defaultBlockState());
                }
                SpringConfiguration config = new SpringConfiguration(fluidState, true, 4, 1, HolderSet.direct(Block::builtInRegistryHolder, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.CALCITE, Blocks.DIRT, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW, Blocks.PACKED_ICE));

                Feature<SpringConfiguration> feature = new SpringFeature(SpringConfiguration.CODEC);
                feature.setRegistryName(new ResourceLocation(Schism.NAMESPACE, name));
                registry.register(feature);

                ConfiguredFeature<SpringConfiguration, Feature<SpringConfiguration>> configuredFeature = new ConfiguredFeature<>(Feature.SPRING, config);
                Holder<PlacedFeature> featureHolder = PlacementUtils.register(name, new Holder.Direct<>(configuredFeature), CountPlacement.of(dataStore.intProp("count")), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(192)), BiomeFilter.biome());
                this.springs.add(new GenerationEntry(featureHolder, biomeCategories));
            }

            default -> throw new RuntimeException("Invalid Block Level Generation Type: " + type);
        };
    }

    /**
     * Called during the biome loading event, used for adding features.
     * @param event The biome loading event/
     */

    public void biomeLoadingEvent(final ModifiableBiomeInfo event)
    {
        this.lakes.forEach(entry -> this.addGenerationFeature(event, GenerationStep.Decoration.LAKES, entry));
        this.springs.forEach(entry -> this.addGenerationFeature(event, GenerationStep.Decoration.FLUID_SPRINGS, entry));
    }

    protected void addGenerationFeature(final ModifiableBiomeInfo event, GenerationStep.Decoration decoration, GenerationEntry generationEntry)
    {
        if (!generationEntry.biomeCategories().isEmpty() && !generationEntry.biomeCategories().contains(event.getCategory().getName())) {
            return;
        }
        event.().addFeature(decoration, generationEntry.featureHolder());
    }
}
