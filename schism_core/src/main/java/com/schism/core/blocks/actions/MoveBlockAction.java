package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.util.WeightedCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Random;

public class MoveBlockAction extends AbstractBlockAction
{
    protected final int upWeight;
    protected final int downWeight;
    protected final int cardinalWeight;
    protected final int sticky;

    public MoveBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.upWeight = dataStore.intProp("up_weight");
        this.downWeight = dataStore.intProp("down_weight");
        this.cardinalWeight = dataStore.intProp("cardinal_weight");
        this.sticky = dataStore.intProp("sticky");
    }

    @Override
    public BlockState tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        // Create a weighted collection of valid directions:
        WeightedCollection<Direction> weightedDirections = Arrays.stream(Direction.values()).filter(direction -> {
            if (switch (direction) {
                case UP -> this.upWeight <= 0;
                case DOWN -> this.downWeight <= 0;
                default -> this.cardinalWeight <= 0;
            }) {
                return false;
            }
            BlockPos targetPos = blockPos.relative(direction);
            if (targetPos.getY() >= level.getHeight() || targetPos.getY() < level.getMinBuildHeight()) {
                return false;
            }
            return level.getBlockState(targetPos).isAir();
        }).map(direction -> switch (direction) {
            case UP -> new WeightedCollection.Entry<>(this.upWeight, direction);
            case DOWN -> new WeightedCollection.Entry<>(this.downWeight, direction);
            default -> new WeightedCollection.Entry<>(this.cardinalWeight, direction);
        }).collect(WeightedCollection.Collector.toWeightedCollection());

        // Stop if there are no free directions:
        if (weightedDirections.isEmpty()) {
            return blockState;
        }

        // Move in a random direction:
        this.cloudMove(blockState, level, blockPos, weightedDirections.get(random), 0);
        return null;
    }

    public void cloudMove(BlockState blockState, ServerLevel level, BlockPos blockPos, Direction moveDirection, int count)
    {
        // Move the target block:
        level.setBlock(blockPos.relative(moveDirection), blockState, 3);
        level.removeBlock(blockPos, false);

        // Move adjacent clouds:
        if (this.sticky > count) {
            Arrays.stream(Direction.values())
                    .filter(direction -> direction != moveDirection)
                    .forEach(direction -> cloudMove(blockState, level, blockPos.relative(direction), moveDirection, count + 1));
        }
    }
}
