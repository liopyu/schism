package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockActor;
import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class DeteriorateBlockAction extends AbstractBlockAction
{
    protected final float chance;
    protected final int ageMin;
    protected final boolean destroyedByRain;

    public DeteriorateBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.chance = dataStore.floatProp("chance");
        this.ageMin = dataStore.intProp("age_min");
        this.destroyedByRain = dataStore.booleanProp("destroyed_by_rain");
    }

    @Override
    public BlockState tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        if (blockState.getOptionalValue(BlockActor.PERSISTENT).orElse(false)) {
            return blockState;
        }

        // Age Check:
        if (this.ageMin > 0 && this.definition().actor().age(blockState) < this.ageMin) {
            return blockState;
        }

        // On Source:
        BlockPos blockPosBelow = blockPos.below();
        BlockState lowerBlockState = level.getBlockState(blockPosBelow);
        if (this.definition().isSourceBlockState(level, lowerBlockState, blockPosBelow, Direction.UP)) {
            return blockState;
        }

        // Rain Destruction
        if (this.rainDestruction(level, blockPos)) {
            level.removeBlock(blockPos, true);
            return null;
        }

        // Random Destruction:
        if (this.chance >= 1 || random.nextFloat() <= this.chance) {
            level.removeBlock(blockPos, true);
            return null;
        }

        return blockState;
    }

    /**
     * Determines if rain should remove the block if rain removal is enabled.
     * @param level The level.
     * @param blockPos The block position to check around.
     * @return True if rain should remove the block and rain removal is enabled.
     */
    protected boolean rainDestruction(Level level, BlockPos blockPos)
    {
        if (!this.destroyedByRain || !level.isRaining()) {
            return false;
        }
        return level.isRainingAt(blockPos) || level.isRainingAt(blockPos.west()) || level.isRainingAt(blockPos.east()) || level.isRainingAt(blockPos.north()) || level.isRainingAt(blockPos.south());
    }
}
