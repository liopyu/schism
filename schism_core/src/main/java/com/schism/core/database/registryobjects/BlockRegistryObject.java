package com.schism.core.database.registryobjects;

import com.schism.core.database.CachedObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

public class BlockRegistryObject<ObjectT extends IForgeBlock> extends CachedObject<ObjectT>
{
    /**
     * Cached Registry Objects take a forge registry id and will return the object it matches with caching for performance.
     * @param id The id of the object to get and cache.
     * @param registry A supplier that provides the registry to get the object from.
     */
    public BlockRegistryObject(String id, Supplier<IForgeRegistry<ObjectT>> registry)
    {
        super(() -> registry.get().getValue(new ResourceLocation(id)));
    }
}
