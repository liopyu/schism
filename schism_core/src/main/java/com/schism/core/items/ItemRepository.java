package com.schism.core.items;

import com.schism.core.AbstractRepository;
import com.schism.core.Schism;
import com.schism.core.database.DataStore;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemRepository extends AbstractRepository<ItemDefinition>
{
    private static final ItemRepository INSTANCE = new ItemRepository();

    private Map<String, CreativeModeTab> creativeModeTabs = new HashMap<>();
    private Map<String, Supplier<Item>> creativeModeIcons = new HashMap<>();

    /**
     * Gets the singleton ItemRepository instance.
     * @return The Item Repository which loads and stores items.
     */
    public static ItemRepository get()
    {
        return INSTANCE;
    }

    protected ItemRepository()
    {
        super("item");
    }

    @Override
    public ItemDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new ItemDefinition(subject, dataStore);
    }

    /**
     * Gets or creates a creative mod tab by key.
     * @param key The creative mod tab key string.
     * @return A creative mod tab.
     */
    public CreativeModeTab creativeModeTab(String key)
    {
        if (!this.creativeModeTabs.containsKey(key)) {
            this.creativeModeTabs.put(key, new CreativeModeTab(Schism.NAMESPACE + "." + key)
            {
                @Override
                public @NotNull ItemStack makeIcon()
                {
                    return ItemRepository.get().creativeModeIcon(key);
                }
            });
        }
        return this.creativeModeTabs.get(key);
    }

    /**
     * Gets a creative mode icon for the specified tab key.
     * @param key The creative mod tab key string.
     * @return An item stack to act as the tab's icon.
     */
    public ItemStack creativeModeIcon(String key)
    {
        if (!this.creativeModeIcons.containsKey(key)) {
            return new ItemStack(Blocks.NETHER_GOLD_ORE);
        }
        return new ItemStack(this.creativeModeIcons.get(key).get());
    }

    /**
     * Sets the creative mode icon for the provided tab key.
     * @param key The creative mod tab key string.
     * @param itemCallback A callback that returns the item to set.
     */
    public void setCreativeModeIcon(String key, Supplier<Item> itemCallback)
    {
        this.creativeModeIcons.put(key, itemCallback);
    }

    /**
     * Registers items into Forge.
     * @param itemRegistryEvent The item registry event.
     */
    @SubscribeEvent
    public void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent)
    {
        this.definitions.values().stream().sorted().forEach(itemDefinition -> itemDefinition.registerItem(itemRegistryEvent.getRegistry()));
    }
}
