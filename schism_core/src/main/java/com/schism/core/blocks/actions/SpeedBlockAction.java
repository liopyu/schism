package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SpeedBlockAction extends AbstractBlockAction
{
    protected final Vec3 multiplier;

    public SpeedBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        multiplier = dataStore.vec3Prop("multiplier");
    }

    @Override
    public BlockState entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        entity.makeStuckInBlock(blockState, this.multiplier.physVec3());
        return blockState;
    }
}
