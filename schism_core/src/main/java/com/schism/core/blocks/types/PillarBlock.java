package com.schism.core.blocks.types;

import com.schism.core.blocks.BlockActor;
import com.schism.core.blocks.BlockDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PillarBlock extends SimpleBlock
{
    public static EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    /**
     * Constructor
     * @param definition The Block Definition.
     */
    public PillarBlock(BlockDefinition definition, BlockActor actor, Properties properties)
    {
        super(definition, actor, properties);
        this.registerDefaultState(this.actor.defaultState(this.getStateDefinition().any().setValue(AXIS, Direction.Axis.Y)));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return Objects.requireNonNullElseGet(super.getStateForPlacement(context), this::defaultBlockState).setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public BlockState rotate(BlockState blockState, @NotNull Rotation rotation)
    {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (blockState.getValue(AXIS)) {
                case X -> blockState.setValue(AXIS, Direction.Axis.Z);
                case Z -> blockState.setValue(AXIS, Direction.Axis.X);
                default -> blockState;
            };
            default -> blockState;
        };
    }
}
