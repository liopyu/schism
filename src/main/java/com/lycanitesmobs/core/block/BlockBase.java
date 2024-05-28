package com.lycanitesmobs.core.block;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockBase extends Block {
	
	// Properties:
	public ModInfo group;
	public String blockName = "block_base";
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	
	// Stats:
	/** If set to a value above 0, the block will update on the specified number of ticks. **/
	public int tickRate = 0;
	/** If true, this block will be set to air on it's first tick, useful for blocks that despawn over time like fire. **/
	public boolean removeOnTick = false;
	/** If true after performing a tick update, another tick update will be scheduled thus creating a loop. **/
	public boolean loopTicks = true;
	/** Will falling blocks such as sand or gravel destroy this block if they land on it? */
	public boolean canBeCrushed = false;

	/** If true, this block cannot be broken or even hit like a solid block. **/
	public boolean noBreakCollision = false;
	
	// Rendering:
	public static enum RENDER_TYPE {
		NONE(-1), NORMAL(0), CROSS(1), TORCH(2), FIRE(3), FLUID(4); // More found on RenderBlock, or use Client Proxies for custom renderers.
		public final int id;
	    private RENDER_TYPE(int value) { this.id = value; }
	    public int getValue() { return id; }
	}

    public BlockBase(Block.Properties properties, ModInfo group, String name) {
        super(properties);
        this.group = group;
        this.blockName = name;
		this.setRegistryName(this.group.modid, this.blockName.toLowerCase());
    }

	public BlockBase(Block.Properties properties) {
		super(properties);
	}

	@Override
	@Nonnull
	public String getDescriptionId() {
		return "block." + LycanitesMobs.modInfo.modid + "." + this.blockName;
	}

	@Override
	public IFormattableTextComponent getName() {
    	return new TranslationTextComponent(this.getDescriptionId());
	}

	@OnlyIn(Dist.CLIENT)
	@Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(this.getDescription(stack, world));
    }

    public ITextComponent getDescription(ItemStack itemStack, @Nullable IBlockReader world) {
        return new TranslationTextComponent(this.getDescriptionId() + ".description").withStyle(TextFormatting.GREEN);
    }
	
	
	// ==================================================
	//                      Place
	// ==================================================
	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		// Initial Block Ticking:
		if(this.tickRate > 0) {
			world.getBlockTicks().scheduleTick(pos, this, this.tickRate(world));
		}
	}
    
    
	// ==================================================
	//                     Ticking
	// ==================================================
    // ========== Tick Rate ==========
    public int tickRate(IWorldReader world) {
    	return this.tickRate;
    }

    // ========== Tick Update ==========
    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) { //tick()
        if (world.isClientSide)
            return;

        // Remove On Tick:
        if (this.removeOnTick && this.canRemove(world, pos, state, random))
            world.removeBlock(pos, true);

        // Looping Tick:
        else if (this.tickRate > 0 && this.loopTicks)
            world.getBlockTicks().scheduleTick(pos, this, this.tickRate(world), TickPriority.LOW);
    }

    // ========== Can Remove ==========
    /** Returns true if the block should be removed naturally (remove on tick). **/
    public boolean canRemove(World world, BlockPos pos, BlockState state, Random random) {
        return true;
    }
    
    
	// ==================================================
	//                    Collision
	// ==================================================
	@Override
	public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
    	if(this.noBreakCollision) {
			return VoxelShapes.empty();
		}
		return super.getShape(blockState, world, blockPos, selectionContext);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, IBlockReader world, BlockPos blockPos, ISelectionContext selectionContext) {
		return this.hasCollision ? blockState.getShape(world, blockPos) : VoxelShapes.empty();
	}
    
    
	// ==================================================
	//                Collision Effects
	// ==================================================
	@Override
	public void stepOn(World world, BlockPos pos, Entity entity) {
		super.stepOn(world, pos, entity);
	}

    @Override
    public void entityInside(BlockState blockState, World world, BlockPos pos, Entity entity) {
		super.entityInside(blockState, world, pos, entity);
	}
}
