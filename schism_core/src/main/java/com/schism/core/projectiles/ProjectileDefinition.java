package com.schism.core.projectiles;

import com.schism.core.Schism;
import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.CachedDefinition;
import com.schism.core.database.CachedList;
import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import com.schism.core.items.ChargeItem;
import com.schism.core.items.ItemRepository;
import com.schism.core.projectiles.actions.AbstractProjectileAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class ProjectileDefinition extends AbstractDefinition implements EntityType.EntityFactory<ProjectileEntity>
{
    // Properties:
    protected String creativeModeTab;
    protected String creativeModeIcon;
    protected float width;
    protected float height;

    // Behavior:
    protected float gravity;
    protected float velocity;
    protected int lifetimeMin;
    protected int lifetimeMax;
    protected boolean impactHollowBlocks;
    protected boolean impactSolidBlocks;
    protected boolean impactEntities;
    protected CachedList<ElementDefinition> cachedChargeElements;

    // Render:
    protected String renderType;
    protected float renderScale;
    protected boolean renderGlow;
    protected float renderSpin;
    protected int renderAnimationFrames;

    // Lists:
    protected List<AbstractProjectileAction> actions;

    // Instances:
    protected ChargeItem chargeItem;
    protected EntityType<ProjectileEntity> entityType;
    protected ResourceLocation texture;

    /**
     * Projectile definitions hold information about projectiles.
     * @param subject The name of the projectile.
     * @param dataStore The data store that holds config data about this definition.
     */
    public ProjectileDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return ProjectileRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.creativeModeTab = dataStore.stringProp("properties.creative_mode_tab");
        this.creativeModeIcon = dataStore.stringProp("properties.creative_mode_icon");
        this.width = dataStore.floatProp("properties.width");
        this.height = dataStore.floatProp("properties.height");

        this.gravity = dataStore.floatProp("behavior.gravity");
        this.velocity = dataStore.floatProp("behavior.velocity");
        this.lifetimeMin = dataStore.intProp("behavior.lifetime.min");
        this.lifetimeMax = dataStore.intProp("behavior.lifetime.max");
        this.impactHollowBlocks = dataStore.booleanProp("behavior.impact.hollow_blocks");
        this.impactSolidBlocks = dataStore.booleanProp("behavior.impact.solid_blocks");
        this.impactEntities = dataStore.booleanProp("behavior.impact.entities");
        this.cachedChargeElements = new CachedList<> (dataStore.listProp("behavior.charge_elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());

        this.renderType = dataStore.stringProp("render.type");
        this.renderScale = dataStore.floatProp("render.scale");
        this.renderGlow = dataStore.booleanProp("render.glow");
        this.renderSpin = dataStore.floatProp("render.spin");
        this.renderAnimationFrames = dataStore.intProp("render.animation_frames");

        this.actions = AbstractProjectileAction.listFromMap(this, dataStore.mapProp("projectile_actions"));
    }

    /**
     * Gets the base gravity of this projectile.
     * @return The base gravity of this projectile.
     */
    public float gravity()
    {
        return this.gravity;
    }

    /**
     * Gets the base velocity of this projectile.
     * @return The base velocity of this projectile.
     */
    public float velocity()
    {
        return this.velocity;
    }

    /**
     * Gets a potentially random lifetime that a projectile should last for in ticks.
     * @param random An instance of random for random lifetime ranges if defined.
     * @return The projectile lifetime in ticks.
     */
    public int lifetime(Random random)
    {
        if (this.lifetimeMax <= this.lifetimeMin) {
            return this.lifetimeMin;
        }
        return random.nextInt(this.lifetimeMin, this.lifetimeMax + 1);
    }

    /**
     * Whether this projectile is stopped by hollow blocks.
     * @return True if the subject projectile is stopped by hollow blocks.
     */
    public boolean impactHollowBlocks()
    {
        return this.impactHollowBlocks;
    }

    /**
     * Whether this projectile is stopped by solid blocks.
     * @return True if the subject projectile is stopped by solid blocks.
     */
    public boolean impactSolidBlocks()
    {
        return this.impactSolidBlocks;
    }

    /**
     * Whether this projectile is stopped by entities.
     * @return True if the subject projectile is stopped by entities.
     */
    public boolean impactEntities()
    {
        return this.impactEntities;
    }

    /**
     * Gets the list of elements the charge item for this projectile should contain.
     * @return The list of charge elements for this projectile.
     */
    public List<ElementDefinition> chargeElements()
    {
        return this.cachedChargeElements.get();
    }

    /**
     * Gets the list of actions this projectile uses.
     * @return The list of projectile actions.
     */
    public List<AbstractProjectileAction> actions()
    {
        return this.actions;
    }

    /**
     * Gets the texture of this projectile.
     * @return The texture of this projectile.
     */
    public ResourceLocation spriteTexture()
    {
        if (this.texture == null) {
            this.texture = new ResourceLocation(Schism.NAMESPACE, "textures/item/charge/" + this.subject() + ".png");
        }
        return this.texture;
    }

    /**
     * Gets the type of renderer to use.
     * @return The render type.
     */
    public String renderType()
    {
        return this.renderType;
    }

    /**
     * The base scale to render the projectile at, the actual size is multiplied by this when rendering.
     * @return The render scale.
     */
    public float renderScale()
    {
        return this.renderScale;
    }

    /**
     * Whether the projectile glows and ignores light level.
     * @return True if the projectile glows and ignores light level.
     */
    public boolean renderGlow()
    {
        return this.renderGlow;
    }

    /**
     * An amount of rotation per tick to apply to the projectile.
     * @return The projectile spin to apply each render tick.
     */
    public float renderSpin()
    {
        return this.renderSpin;
    }

    /**
     * How many frames the projectile has.
     * @return The number of sprite animation frames.
     */
    public int renderAnimationFrames()
    {
        return this.renderAnimationFrames;
    }

    /**
     * Gets the Charge Item created for this projectile.
     * @return The Charge Item of this projectile.
     */
    public ChargeItem chargeItem()
    {
        if (this.chargeItem != null) {
            return this.chargeItem;
        }

        if (!this.creativeModeIcon.isEmpty()) {
            ItemRepository.get().setCreativeModeIcon(this.creativeModeIcon, this::chargeItem);
        }

        Item.Properties properties = new Item.Properties();
        properties.tab(ItemRepository.get().creativeModeTab(this.creativeModeTab));
        properties.stacksTo(64);
        properties.rarity(Rarity.UNCOMMON);
        properties.fireResistant();

        this.chargeItem = new ChargeItem(this, properties);
        DispenserBlock.registerBehavior(this.chargeItem, new ChargeDispenserBehavior(this));

        return this.chargeItem;
    }

    public EntityType<ProjectileEntity> entityType()
    {
        if (this.entityType != null) {
            return this.entityType;
        }

        EntityType.Builder<ProjectileEntity> builder = EntityType.Builder.of(this, MobCategory.MISC);
        builder.setTrackingRange(40);
        builder.setUpdateInterval(3);
        builder.setShouldReceiveVelocityUpdates(true);
        builder.sized(this.width, this.height);
        this.entityType = builder.build(this.subject());
        this.entityType.setRegistryName(this.resourceLocation());

        return this.entityType;
    }

    /**
     * Creates an entity from an entity type in the provided level.
     * @param level The level to create the entity in.
     * @return The created entity or null if no entity could be created.
     */
    public @NotNull ProjectileEntity create(Level level)
    {
        return this.create(this.entityType(), level);
    }

    @Override
    public @NotNull ProjectileEntity create(EntityType<ProjectileEntity> entityType, Level level)
    {
        return new ProjectileEntity(this, level);
    }

    /**
     * Registers items.
     * @param registry The forge registry.
     */
    public void registerChargeItem(final IForgeRegistry<Item> registry)
    {
        if (this.chargeItem() == null) {
            throw new RuntimeException("Tried to register a null charge item for: " + this.subject());
        }
        registry.register(this.chargeItem());
    }

    /**
     * Registers entity types.
     * @param registry The forge registry.
     */
    public void registerEntityType(final IForgeRegistry<EntityType<?>> registry)
    {
        if (this.chargeItem() == null) {
            throw new RuntimeException("Tried to register a null entity type for: " + this.entityType());
        }
        registry.register(this.entityType());
    }
}
