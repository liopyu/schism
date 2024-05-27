package com.schism.core.blocks;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.*;

public class BlockRepository extends AbstractRepository<BlockDefinition>
{
    private static final BlockRepository INSTANCE = new BlockRepository();

    /**
     * Gets the singleton BlockRepository instance.
     * @return The Block Repository which loads and stores all blocks.
     */
    public static BlockRepository get()
    {
        return INSTANCE;
    }

    protected BlockRepository()
    {
        super("block");
    }

    @Override
    public BlockDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new BlockDefinition(subject, dataStore);
    }

    /**
     * Loads a theme definition where theme based block collections are generated and updated.
     * @param themeDefinition A theme definition to create block definitions from.
     */
    public void loadThemeDefinition(ThemeDefinition themeDefinition)
    {
        String theme = themeDefinition.subject();

        this.addDefinition(BlockDefinition.basic(theme + "stone", "simple"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_stairs", "stairs"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_slab", "slab"));

        this.addDefinition(BlockDefinition.basic(theme + "stone_brick", "simple"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_brick_stairs", "stairs"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_brick_slab", "slab"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_brick_fence", "fence"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_brick_wall", "wall"));

        this.addDefinition(BlockDefinition.basic(theme + "stone_tile", "simple"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_tile_stairs", "stairs"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_tile_slab", "slab"));

        this.addDefinition(BlockDefinition.basic(theme + "stone_polished", "simple"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_chiseled", "simple"));
        this.addDefinition(BlockDefinition.basic(theme + "stone_pillar", "pillar"));

        this.addDefinition(BlockDefinition.basic(theme + "_crystal", "crystal"));
    }

    /**
     * Registers blocks into Forge.
     * @param event The block registry event.
     */
    @SubscribeEvent
    public void onBlocksRegistry(final NewRegistryEvent event)
    {
        RegistryBuilder<Block> builder = new RegistryBuilder<>();
        var registry = event.create(builder).get();
        this.definitions.values().stream().sorted().forEach(blockDefinition -> blockDefinition.registerBlock(registry));
    }

    /**
     * Registers fluids into Forge.
     * @param event The fluid registry event.
     */
    @SubscribeEvent
    public void onFluidsRegistry(final NewRegistryEvent event)
    {
        RegistryBuilder<net.minecraft.world.level.material.Fluid> builder = new RegistryBuilder<>();
        var registry = event.create(builder).get();
        this.definitions.values().stream().sorted().forEach(blockDefinition -> blockDefinition.registerFluid(registry));
    }

    /**
     * Registers block items into Forge.
     * @param event The item registry event.
     */
    @SubscribeEvent
    public void onItemsRegistry(final NewRegistryEvent event)
    {
        RegistryBuilder<Item> builder = new RegistryBuilder<>();
        var registry = event.create(builder).get();
        this.definitions.values().stream().sorted().forEach(blockDefinition -> blockDefinition.registerItem(registry));
    }

    /**
     * Registers level generation features into Forge.
     * @param event The feature registry event.
     */
    @SubscribeEvent
    public void onFeatureRegistry(final NewRegistryEvent event)
    {
        RegistryBuilder<Feature<?>> builder = new RegistryBuilder<>();
        var registry = event.create(builder).get();
        this.definitions.values().stream().sorted().forEach(blockDefinition -> blockDefinition.registerFeatures(registry));
    }
}
