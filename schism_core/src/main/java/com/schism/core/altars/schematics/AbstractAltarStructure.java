package com.schism.core.altars.schematics;

import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.Map;

public abstract class AbstractAltarStructure
{
    /**
     * Creates an altar structure from the provided data source.
     * @param dataStore The data source to read from.
     * @return The newly created altar structure.
     */
    public static AbstractAltarStructure create(DataStore dataStore)
    {
        return switch (dataStore.stringProp("type")) {
            case "json" -> new JsonAltarStructure(dataStore);
            default -> throw new RuntimeException("Invalid Altar Structure Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Returns the size of the structure in blocks.
     * @return The size of the structure (dimensions by blocks).
     */
    public abstract Vec3 size();

    /**
     * Gets a map of block lists by position.
     * @return A map of block lists keyed by position.
     */
    public abstract Map<Vec3, List<BlockRegistryObject<Block>>> layout();

    /**
     * Gets the blocks in this structure at the provided position.
     * @param position The position in the structure to get a block for.
     * @return A list of blocks this structure can use at the position.
     */
    public abstract List<Block> blocksAt(Vec3 position);

    /**
     * Checks if this structure is built at the provided position.
     * @param level The level to check in.
     * @param originPos The position where the altar core should be.
     * @param offset The structure offset to check from, this can be for example an altar's core position.
     * @return True if this structure is detected, false if not.
     */
    public abstract boolean isAt(Level level, BlockPos originPos, Vec3 offset);

    /**
     * Builds this structure at the provided position.
     * @param level The level to build in.
     * @param originPos The position to build from.
     * @param offset The structure offset to build from, this can be for example an altar's core position (usually with y omitted).
     * @param rotation The structure rotation.
     */
    public abstract void buildAt(Level level, BlockPos originPos, Vec3 offset, Rotation rotation);
}
