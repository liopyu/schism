package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class AgeBlockAction extends AbstractBlockAction
{
    protected final int speedMin;
    protected final int speedMax;

    public AgeBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.speedMin = dataStore.intProp("speed_min");
        this.speedMax = dataStore.intProp("speed_max");
    }

    @Override
    public BlockState tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        if (this.speedMin <= 0) {
            return blockState;
        }

        // On Source:
        BlockPos blockPosBelow = blockPos.below();
        BlockState lowerBlockState = level.getBlockState(blockPosBelow);
        if (this.definition().isSourceBlockState(level, lowerBlockState, blockPosBelow, Direction.UP)) {
            return blockState;
        }

        // Increase age:
        int age = this.definition().actor().age(blockState);
        if (age >= this.definition().age()) {
            return blockState;
        }
        int speed = this.speedMin;
        if (this.speedMax > this.speedMin) {
            speed = random.nextInt(this.speedMin, this.speedMax + 1);
        }
        blockState = this.definition().actor().setAge(blockState, age + speed);
        level.setBlock(blockPos, blockState, 4);

        return blockState;
    }
}
