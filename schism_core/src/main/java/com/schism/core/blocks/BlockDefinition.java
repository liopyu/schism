package com.schism.core.blocks;

import com.schism.core.Schism;
import com.schism.core.blocks.actions.AbstractBlockAction;
import com.schism.core.blocks.types.*;
import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.generation.LevelGenerator;
import com.schism.core.items.FluidBucketItem;
import com.schism.core.items.ItemRepository;
import com.schism.core.resolvers.MaterialResolver;
import com.schism.core.resolvers.SoundTypeResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockDefinition extends AbstractDefinition
{
    /**
     * An instance of the latest definition that is about to create a block, needed to get around a vanilla lock when setting states.
     */
    public static BlockDefinition LATEST_BUILDING_DEFINITION;

    // Properties:
    protected String creativeModeTab;
    protected String creativeModeIcon;
    protected String type;
    protected String baseBlock;
    protected String material;
    protected boolean hasCollision;
    protected String soundType;
    protected float destroyTime;
    protected float explosionResistance;
    protected int lightEmission;
    protected float friction;
    protected float speedFactor;
    protected float jumpFactor;
    protected boolean toolForDrops;
    protected boolean tickRandom;

    // Client:
    protected String renderType;

    // Behavior:
    protected int age;
    protected int tickRateMin;
    protected int tickRateMax;
    protected boolean persistentDefault;
    protected boolean useFireSource;

    // Lists:
    protected List<AbstractBlockAction> actions;
    protected List<String> sourceBlockIds;

    // Fluid:
    protected int fluidColor;
    protected int fluidDensity;
    protected int fluidViscosity;
    protected int fluidTemperature;
    protected int fluidLuminosity;
    protected boolean fluidMultiply;
    protected int fluidSloping;
    protected int fluidDropOff;
    protected int fluidTickRate;

    // World Generation:
    protected Map<String, DataStore> generations;

    // Instances:
    protected BlockActor actor;
    protected Block block;
    protected BlockItem item;
    protected ForgeFlowingFluid stillFluid;
    protected ForgeFlowingFluid flowingFluid;
    protected BucketItem bucket;

    /**
     * Creates a new Block Definition for a default simple stone block, used when generating from themes.
     * @param subject The name of the block to create.
     * @param type The type of the block to create, ex: simple, stairs, fence, etc.
     * @return Returns the newly created Block Definition.
     */
    public static BlockDefinition basic(String subject, String type)
    {
        Map<String, DataStore> propertiesMap = new HashMap<>();
        propertiesMap.put("creative_mode_tab", new DataStore("blocks"));
        propertiesMap.put("creative_mode_icon", new DataStore(type.equals("simple") ? "blocks" : ""));
        propertiesMap.put("type", new DataStore(type));
        propertiesMap.put("base_block", new DataStore(type.equals("stairs") ? subject.replace("_stairs", "") : subject));
        propertiesMap.put("material", new DataStore("stone"));
        propertiesMap.put("has_collision", new DataStore(true));
        propertiesMap.put("sound_type", new DataStore("stone"));
        propertiesMap.put("destroy_time", new DataStore(type.equals("crystal") ? 0.5F : 2.0F));
        propertiesMap.put("explosion_resistance", new DataStore(1200F));
        propertiesMap.put("light_emission", new DataStore(type.equals("crystal") ? 15 : 0));
        propertiesMap.put("friction", new DataStore(0.6F));
        propertiesMap.put("speed_factor", new DataStore(1F));
        propertiesMap.put("jump_factor", new DataStore(1F));
        propertiesMap.put("tool_for_drops", new DataStore(true));
        DataStore propertiesData = new DataStore(propertiesMap);

        Map<String, DataStore> blockMap = new HashMap<>();
        blockMap.put("properties", propertiesData);
        DataStore blockData = new DataStore(blockMap);

        return new BlockDefinition(subject, blockData);
    }

    /**
     * Block definitions hold information about individual blocks.
     * @param subject The name of the block.
     * @param dataStore The data store that holds config data about this definition.
     */
    public BlockDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        // Properties
        this.creativeModeTab = dataStore.stringProp("properties.creative_mode_tab");
        this.creativeModeIcon = dataStore.stringProp("properties.creative_mode_icon");
        this.type = dataStore.stringProp("properties.type");
        this.baseBlock = dataStore.stringProp("properties.base_block");
        this.material = dataStore.stringProp("properties.material");
        this.hasCollision = dataStore.booleanProp("properties.has_collision");
        this.soundType = dataStore.stringProp("properties.sound_type");
        this.destroyTime = dataStore.floatProp("properties.destroy_time");
        this.explosionResistance = dataStore.floatProp("properties.explosion_resistance");
        this.lightEmission = dataStore.intProp("properties.light_emission");
        this.friction = dataStore.floatProp("properties.friction");
        this.speedFactor = dataStore.floatProp("properties.speed_factor");
        this.jumpFactor = dataStore.floatProp("properties.jump_factor");
        this.toolForDrops = dataStore.booleanProp("properties.tool_for_drops");
        this.tickRandom = dataStore.booleanProp("properties.tick_random");

        // Rendering:
        this.renderType = dataStore.stringProp("client.render_type");

        // Behavior:
        this.tickRateMin = dataStore.intProp("behavior.tick_rate_min");
        this.tickRateMax = dataStore.intProp("behavior.tick_rate_max");
        this.age = dataStore.intProp("behavior.age");
        this.persistentDefault = dataStore.booleanProp("behavior.persistent_default");
        this.useFireSource = dataStore.booleanProp("behavior.use_fire_source");

        // Actions:
        this.actions = AbstractBlockAction.listFromMap(this, dataStore.mapProp("block_actions"));
        this.sourceBlockIds = dataStore.listProp("source_block_ids").stream().map(DataStore::stringValue).collect(Collectors.toList());

        // Fluid:
        if (this.type.equals("fluid")) {
            this.fluidColor = Integer.decode(dataStore.stringProp("fluid.color"));
            this.fluidDensity = dataStore.intProp("fluid.density");
            this.fluidViscosity = dataStore.intProp("fluid.viscosity");
            this.fluidTemperature = dataStore.intProp("fluid.temperature");
            this.fluidLuminosity = dataStore.intProp("fluid.luminosity");
            this.fluidMultiply = dataStore.booleanProp("fluid.multiply");
            this.fluidSloping = dataStore.intProp("fluid.sloping");
            this.fluidDropOff = dataStore.intProp("fluid.drop_off");
            this.fluidTickRate = dataStore.intProp("fluid.tick_rate");
        }

        // World Generation:
        this.generations = dataStore.mapProp("generation");
    }

    @Override
    public String type()
    {
        return BlockRepository.get().type();
    }

    /**
     * Gets the material name, this can be mapped to a Minecraft Material via the MaterialResolver singleton.
     * @return The material name.
     */
    public String material()
    {
        return this.material;
    }

    /**
     * Gets the render type name, this can be mapped to a block render type, ex: cutout (for webs).
     * @return The render type name.
     */
    public String renderType()
    {
        return this.renderType;
    }

    /**
     * Returns if the block should stick or not.
     * @return True fi the block should tick, false otherwise.
     */
    public boolean ticks()
    {
        return this.tickRateMin > 0;
    }

    /**
     * Gets the tick rate, if above 0 the block will schedule a tick on place and neighbor updates.
     * @return The tick rate.
     */
    public int tickRate(Random random)
    {
        if (this.tickRateMin >= this.tickRateMax) {
            return this.tickRateMin;
        }
        return random.nextInt(this.tickRateMin, this.tickRateMax + 1);
    }

    /**
     * Gets the age of the block, defaults to 0 but can be set to 1, 2, 7, 15 or 25 to have various actions only activate when age reaches maximum.
     * @return The max age.
     */
    public int age()
    {
        return this.age;
    }

    /**
     * Returns if the block should be set as persistent by default.
     * @return Whether the block should be persistent by default.
     */
    public boolean persistentDefault()
    {
        return this.persistentDefault;
    }

    /**
     * Returns if the block can use fire sources as a source, used by actions that remove a block over time as well as fire block types.
     * @return Whether the block can use fire sources as a source.
     */
    public boolean useFireSource()
    {
        return this.useFireSource;
    }

    /**
     * Returns the distance in blocks that a fluid will search for to determine its slope angle.
     * @return The fluid slop angle block distance.
     */
    public int fluidSloping()
    {
        return this.fluidSloping;
    }

    /**
     * Returns the level value to decrease the fluid by each time it spreads starting from 11 down to 1.
     * @return The level value to decrease the fluid by each time it spreads.
     */
    public int fluidDropOff()
    {
        return this.fluidDropOff;
    }

    /**
     * Determines if the provided block state is considered a source block.
     * @param levelReader The level reader.
     * @param blockState The block state to check.
     * @param blockPos The block position to check.
     * @param direction The block direction to check.
     * @return True if the provided block state is considered a source block, false otherwise.
     */
    public boolean isSourceBlockState(LevelReader levelReader, BlockState blockState, BlockPos blockPos, Direction direction)
    {
        if (this.useFireSource() && blockState.isFireSource(levelReader, blockPos, direction)) {
            return true;
        }
        return DataStore.idInList(blockState.getBlock().getRegistryName(), this.sourceBlockIds);
    }

    /**
     * Returns a list of actions, these contain special effect logic.
     * @return A list of actions.
     */
    public List<AbstractBlockAction> actions()
    {
        return this.actions;
    }

    public BlockActor actor()
    {
        if (this.actor == null) {
            this.actor = new BlockActor(this);
        }
        return this.actor;
    }

    /**
     * Returns the Minecraft Block that this definition is for.
     * @return The minecraft block instance.
     */
    public Block block()
    {
        if (this.block != null) {
            return this.block;
        }

        Material material = MaterialResolver.get().fromString(this.material());
        Block.Properties properties = Block.Properties.of(material);

        if (!this.hasCollision) {
            properties.noCollission();
        }

        properties.sound(SoundTypeResolver.get().fromString(this.soundType));

        properties.destroyTime(this.destroyTime);
        if (this.destroyTime <= 0) {
            properties.instabreak();
        }

        properties.explosionResistance(this.explosionResistance);
        properties.lightLevel(blockState -> this.lightEmission);
        properties.friction(this.friction);
        properties.speedFactor(this.speedFactor);
        properties.jumpFactor(this.jumpFactor);

        if (this.toolForDrops) {
            properties.requiresCorrectToolForDrops();
        }
        if (this.tickRandom) {
            properties.randomTicks();
        }

        LATEST_BUILDING_DEFINITION = this;
        this.block = switch (this.type) {
            case "stairs" -> {
                Supplier<BlockState> baseBlockStateSupplier = () -> {
                    Optional<BlockDefinition> blockDefinition = BlockRepository.get().getDefinition(this.baseBlock);
                    return blockDefinition.map(definition -> definition.block().defaultBlockState()).orElse(null);
                };
                yield new StairsBlock(this, this.actor(), properties, baseBlockStateSupplier);
            }
            case "slab" -> new SlabBlock(this, this.actor(), properties);
            case "pillar" -> new PillarBlock(this, this.actor(), properties);
            case "fence" -> new FenceBlock(this, this.actor(), properties);
            case "wall" -> new WallBlock(this, this.actor(), properties);
            case "fluid" -> new FluidBlock(this, this.actor(), properties.dynamicShape(), this::stillFluid);
            case "fire" -> new FireBlock(this, this.actor(), properties);
            default -> new SimpleBlock(this, this.actor(), properties);
        };
        return this.block;
    }

    /**
     * Returns the Minecraft Block Item that this definition is for.
     * @return The minecraft block item instance (an item that contains a block).
     */
    public BlockItem item()
    {
        if (this.item == null) {
            if (!this.creativeModeIcon.isEmpty()) {
                ItemRepository.get().setCreativeModeIcon(this.creativeModeIcon, this::item);
            }
            Item.Properties properties = new Item.Properties();
            if (!this.type.equals("fluid")) {
                properties.tab(ItemRepository.get().creativeModeTab(this.creativeModeTab));
            }
            this.item = new BlockItem(this.block(), properties);
            this.item.setRegistryName(this.resourceLocation());
        }

        return this.item;
    }

    /**
     * Returns the bucket for this block.
     * @return The bucket for this block or null if not a fluid type.
     */
    public BucketItem bucket()
    {
        if (!this.type.equals("fluid")) {
            return null;
        }
        if (this.bucket == null) {
            Item.Properties properties = new Item.Properties();
            properties.tab(ItemRepository.get().creativeModeTab(this.creativeModeTab));
            properties.stacksTo(1);
            properties.craftRemainder(Items.BUCKET);
            this.bucket = new FluidBucketItem(this::stillFluid, properties);
            this.bucket.setRegistryName(this.subject() + "_bucket");
        }
        return this.bucket;
    }

    /**
     * Generates fluid properties for creating fluids.
     * @return Fluid properties for generating fluids.
     */
    protected ForgeFlowingFluid.Properties fluidProperties()
    {
        FluidAttributes.Builder fluidBuilder = FluidAttributes.builder(new ResourceLocation(Schism.NAMESPACE, "block/" + this.subject() + "_still"), new ResourceLocation(Schism.NAMESPACE, "block/" + this.subject() + "_flowing"));
        fluidBuilder.color(this.fluidColor);
        fluidBuilder.density(this.fluidDensity);
        fluidBuilder.viscosity(this.fluidViscosity);
        fluidBuilder.temperature(this.fluidTemperature);
        fluidBuilder.luminosity(this.fluidLuminosity);
        ForgeFlowingFluid.Properties fluidProperties = new ForgeFlowingFluid.Properties(this::stillFluid, this::flowingFluid, fluidBuilder);
        if (this.fluidMultiply) {
            fluidProperties.canMultiply();
        }
        fluidProperties.tickRate(this.fluidTickRate);
        fluidProperties.bucket(this::bucket);
        fluidProperties.block(() -> (FluidBlock) this.block());
        return fluidProperties;
    }

    /**
     * Returns the still fluid for this block.
     * @return The still fluid for this block or null if not a fluid type.
     */
    public ForgeFlowingFluid stillFluid()
    {
        if (!this.type.equals("fluid")) {
            return null;
        }
        if (this.stillFluid == null) {
            this.stillFluid = new Fluid.Still(this, this.fluidProperties());
        }
        return this.stillFluid;
    }

    /**
     * Returns the flowing fluid for this block.
     * @return The flowing fluid for this block or null if not a fluid type.
     */
    public ForgeFlowingFluid flowingFluid()
    {
        if (!this.type.equals("fluid")) {
            return null;
        }
        if (this.flowingFluid == null) {
            this.flowingFluid = new Fluid.Flowing(this, this.fluidProperties());
        }
        return this.flowingFluid;
    }

    /**
     * Registers a block.
     * @param registry The forge registry.
     */
    public void registerBlock(final IForgeRegistry<Block> registry)
    {
        if (this.block() == null) {
            throw new RuntimeException("Tried to register a null block for: " + this.subject());
        }
        registry.register(this.block());
    }

    /**
     * Registers fluids.
     * @param registry The forge registry.
     */
    public void registerFluid(final IForgeRegistry<net.minecraft.world.level.material.Fluid> registry)
    {
        if (this.stillFluid() != null) {
            registry.register(this.stillFluid());
        }
        if (this.flowingFluid() != null) {
            registry.register(this.flowingFluid());
        }
    }

    /**
     * Registers items.
     * @param registry The forge registry.
     */
    public void registerItem(final IForgeRegistry<Item> registry)
    {
        if (this.item() == null) {
            throw new RuntimeException("Tried to register a null block item for: " + this.subject());
        }
        registry.register(this.item());
        if (this.bucket() != null) {
            registry.register(this.bucket());
        }
    }

    /**
     * Registers world generations.
     * @param registry The forge registry.
     */
    public void registerFeatures(final IForgeRegistry<Feature<?>> registry)
    {
        if (this.generations == null) {
            return;
        }
        this.generations.forEach((key, value) -> LevelGenerator.get().register(registry, this.subject() + "_" + key, this.block(), value));
    }
}
