package com.lycanitesmobs.core.block.effect;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockBase;
import com.lycanitesmobs.core.info.ItemManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BlockQuickWeb extends BlockBase {
	
	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockQuickWeb(Block.Properties properties) {
		super(properties);

		this.group = LycanitesMobs.modInfo;
		this.blockName = "quickweb";
		
		// Stats:
		this.tickRate = 200;
		this.removeOnTick = true;
		this.loopTicks = false;
		this.canBeCrushed = false;

		this.noBreakCollision = false;

		this.setRegistryName(this.group.modid, this.blockName.toLowerCase());
		this.registerDefaultState(this.getStateDefinition().any().setValue(AGE, 0));

		ItemManager.getInstance().cutoutBlocks.add(this);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
	
	
	// ==================================================
	//                     Break
	// ==================================================
    /*@Override
    public Item getItemDropped(BlockState blockState, Random random, int fortune) {
        return ObjectManager.getItem("quickwebcharge");
    }

    @Override
    public int damageDropped(BlockState blockState) {
        return 0;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }*/
    
    
	// ==================================================
	//                Collision Effects
	// ==================================================
    @Override
	public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
		super.entityInside(blockState, world, pos, entity);
		entity.makeStuckInBlock(blockState, new Vector3d(0.25D, (double)0.05F, 0.25D));
	}


	// ==================================================
	//                      Rendering
	// ==================================================
    /*@Override Redundant?
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }*/
}
