package com.lycanitesmobs.core.block;

import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

public class BlockPillar extends BlockBase {
    public static EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockPillar(Block.Properties properties, ModInfo group, String name) {
		super(properties, group, name);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
	}

	// ==================================================
	//                   Blockstate
	// ==================================================
	public BlockState rotate(BlockState state, Rotation rot) {
		switch(rot) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch((Direction.Axis)state.getValue(AXIS)) {
					case X:
						return state.setValue(AXIS, Direction.Axis.Z);
					case Z:
						return state.setValue(AXIS, Direction.Axis.X);
					default:
						return state;
				}
			default:
				return state;
		}
	}

	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
	}
}
