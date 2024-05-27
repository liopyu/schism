package com.schism.core.items;

import com.mojang.datafixers.util.Pair;
import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.items.actions.AbstractItemAction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Locale;

public class ItemDefinition extends AbstractDefinition
{
    // Properties:
    protected String creativeModeTab;
    protected String creativeModeIcon;
    protected int stackSize;
    protected int durability;
    protected BlockRegistryObject<Item> containerItem;
    protected String rarity;
    protected boolean fireResistant;
    protected boolean noRepair;

    // Food:
    protected boolean food;
    protected int foodNutrition;
    protected float foodSaturation;
    protected boolean foodIsMeat;
    protected boolean foodAlwaysEat;
    protected boolean foodFastEat;
    protected List<Pair<BlockRegistryObject<MobEffect>, Float>> foodEffects;

    // Behavior:
    protected int cooldown;
    protected float experience;
    protected UseAnim useAnimation;
    protected int useTicks;
    protected boolean useContinuous;
    protected int useCost;
    protected int inventoryTicks;
    protected int fuelTime;

    // Lists:
    protected List<AbstractItemAction> actions;

    // Instances:
    protected Item item;

    /**
     * Theme definitions hold information about a collection of decorative blocks.
     * @param subject The name of the block theme, block names will be based on this.
     * @param dataStore The data store that holds config data about this definition.
     */
    public ItemDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return ItemRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.creativeModeTab = dataStore.stringProp("properties.creative_mode_tab");
        this.creativeModeIcon = dataStore.stringProp("properties.creative_mode_icon");
        this.stackSize = dataStore.intProp("properties.stack_size");
        this.durability = dataStore.intProp("properties.durability");
        this.containerItem = new BlockRegistryObject<>(dataStore.stringProp("properties.container_item_id"), () -> ForgeRegistries.ITEMS);
        this.rarity = dataStore.stringProp("properties.rarity");
        this.fireResistant = dataStore.booleanProp("properties.fire_resistant");
        this.noRepair = dataStore.booleanProp("properties.no_repair");

        this.food = dataStore.has("food");
        this.foodNutrition = dataStore.intProp("food.nutrition");
        this.foodSaturation = dataStore.floatProp("food.saturation");
        this.foodIsMeat = dataStore.booleanProp("food.is_meat");
        this.foodAlwaysEat = dataStore.booleanProp("food.always_eat");
        this.foodFastEat = dataStore.booleanProp("food.fast_eat");
        this.foodEffects = dataStore.listProp("food.chance_effect_ids").stream().map(entry -> Pair.of(
                new BlockRegistryObject<>(entry.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS),
                entry.floatProp("chance"))).toList();

        this.cooldown = dataStore.intProp("behavior.cooldown");
        this.experience = dataStore.floatProp("behavior.experience");
        String useAnimationName = dataStore.stringProp("behavior.use_animation");
        try {
            this.useAnimation = useAnimationName.isEmpty() ? UseAnim.NONE : UseAnim.valueOf(useAnimationName.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid use animation: " + useAnimationName + " for: " + this);
        }
        this.useTicks = dataStore.intProp("behavior.use_ticks");
        this.useContinuous = dataStore.booleanProp("behavior.use_continuous");
        this.useCost = dataStore.intProp("behavior.use_cost");
        this.inventoryTicks = dataStore.intProp("behavior.inventory_ticks");
        this.fuelTime = dataStore.intProp("behavior.fuel_time");

        this.actions = AbstractItemAction.listFromMap(this, dataStore.mapProp("item_actions"));
    }

    /**
     * Gets the item that should be left behind when this item is consumed when crafting or used as fuel, etc.
     * @return The item to return after this item has been used for crafted or as fuel, etc.
     */
    public Item containerItem()
    {
        return this.containerItem.get();
    }

    /**
     * Gets the cooldown in ticks before the subject item can be used again.
     * @return The item use cooldown in ticks.
     */
    public int cooldown()
    {
        return this.cooldown;
    }

    /**
     * Gets the experience gained when this item is smelted.
     * @return The experience gained when smelted.
     */
    public float experience()
    {
        return this.experience;
    }

    /**
     * Gets the animation to play when the subject item is used.
     * @return The item use animation to play.
     */
    public UseAnim useAnimation()
    {
        return this.useAnimation;
    }

    /**
     * Gets the number of ticks before triggering actions. This also acts as an override for the time in ticks it takes to eat, etc.
     * @return The action use tick interval/duration.
     */
    public int useTicks()
    {
        return this.useTicks;
    }

    /**
     * Gets if this item can be used indefinitely.
     * @return True if this item can be used indefinitely and not released.
     */
    public boolean useContinuous()
    {
        return this.useContinuous;
    }

    /**
     * Gets how much the item costs to be used, this is either how much the stack size is depleted by or how much durability to take (when not creative).
     * @return The amount to decrease stack size or durability by when the item is used.
     */
    public int useCost()
    {
        return this.useCost;
    }

    /**
     * Gets the number of ticks before triggering actions while this item is in an inventory.
     * @return The action inventory tick interval.
     */
    public int inventoryTicks()
    {
        return this.inventoryTicks;
    }

    /**
     * Gets how long this item can be used as fuel for, used by furnaces, etc.
     * @return The fuel time in ticks of this item. 0 or below disables use as fuel.
     */
    public int fuelTime()
    {
        return this.fuelTime;
    }

    /**
     * Returns a list of all actions for the subject item.
     * @return A list of item actions.
     */
    public List<AbstractItemAction> actions()
    {
        return this.actions;
    }

    /**
     * Returns the Minecraft Item that this definition is for.
     * @return The minecraft item instance.
     */
    public Item item()
    {
        if (this.item != null) {
            return this.item;
        }

        if (!this.creativeModeIcon.isEmpty()) {
            ItemRepository.get().setCreativeModeIcon(this.creativeModeIcon, this::item);
        }

        Item.Properties properties = new Item.Properties();
        properties.tab(ItemRepository.get().creativeModeTab(this.creativeModeTab));
        if (this.durability > 0) {
            properties.durability(this.durability);
        } else if (this.stackSize == 0) {
            properties.stacksTo(64);
        } else {
            properties.stacksTo(Math.max(this.stackSize, 1));
        }
        try {
            properties.rarity(this.rarity.isEmpty() ? Rarity.COMMON : Rarity.valueOf(this.rarity.toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid item rarity: " + this.rarity + " for: " + this);
        }
        if (this.fireResistant) {
            properties.fireResistant();
        }
        if (this.noRepair) {
            properties.setNoRepair();
        }

        if (this.food) {
            FoodProperties.Builder foodBuilder = new FoodProperties.Builder();
            foodBuilder.nutrition(this.foodNutrition);
            foodBuilder.saturationMod(this.foodSaturation);
            if (this.foodIsMeat) {
                foodBuilder.meat();
            }
            if (this.foodAlwaysEat) {
                foodBuilder.alwaysEat();
            }
            if (this.foodFastEat) {
                foodBuilder.fast();
            }
            this.foodEffects.forEach(effectChancePair -> foodBuilder.effect(
                    () -> new MobEffectInstance(effectChancePair.getFirst().get()),
                    effectChancePair.getSecond()));
            properties.food(foodBuilder.build());
        }

        this.item = new SimpleItem(this, properties);
        return this.item;
    }

    /**
     * Registers items.
     * @param registry The forge registry.
     */
    public void registerItem(final IForgeRegistry<Item> registry)
    {
        if (this.item() == null) {
            throw new RuntimeException("Tried to register a null item for: " + this.subject());
        }
        registry.register(this.item());
    }
}
