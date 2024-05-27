package com.schism.core.blocks.types;

import com.google.common.collect.ImmutableMap;
import com.schism.core.blocks.BlockActor;
import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.IHasDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FireBlock extends BaseFireBlock implements IHasDefinition<BlockDefinition>
{
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream()
            .filter((direction) -> direction.getKey() != Direction.DOWN)
            .collect(Util.toMap());

    protected static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);

    protected final BlockDefinition definition;
    protected final BlockActor actor;

    protected final Map<BlockState, VoxelShape> shapesCache;

    /**
     * Constructor
     * @param definition The Block Definition.
     * @param properties The Block Properties.
     */
    public FireBlock(BlockDefinition definition, BlockActor actor, Properties properties)
    {
        super(properties, 0);
        this.setRegistryName(definition.resourceLocation());
        this.definition = definition;
        this.actor = actor;

        BlockState defaultState = this.getStateDefinition().any();
        defaultState.setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false);
        this.registerDefaultState(this.actor.defaultState(defaultState));

        this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream()
                .filter(this.actor::isDefault)
                .collect(Collectors.toMap(Function.identity(), FireBlock::calculateShape)));
    }

    @Override
    public BlockDefinition definition()
    {
        return this.definition;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
        list.add(Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH);
        builder.add(EAST);
        builder.add(SOUTH);
        builder.add(WEST);
        builder.add(UP);
        BlockActor.createBlockStateDefinition(builder);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState replaceBlockState, boolean moving)
    {
        this.actor.onPlace(blockState, level, blockPos, replaceBlockState, moving);
    }

    @Override
    public void neighborChanged(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block neighborBlock, @NotNull BlockPos neighborBlockPos, boolean someBoolean)
    {
        this.actor.neighborChangedBlock(blockState, level, blockPos, neighborBlock, neighborBlockPos, someBoolean);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState neighborBlockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos neighborBlockPos)
    {
        blockState = this.getStateForLocation(blockState, levelAccessor, blockPos);
        return this.actor.neighborChangedBlockState(blockState, levelAccessor, blockPos, direction, neighborBlockState, neighborBlockPos);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos)
    {
        return this.actor.canSurvive(blockState, levelReader, blockPos);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        super.tick(blockState, level, blockPos, random);
        this.actor.tick(blockState, level, blockPos, random);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        this.actor.entityInside(blockState, level, blockPos, entity);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult)
    {
        super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
        return this.actor.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random)
    {
        this.actor.animateTick(blockState, level, blockPos, random);
    }

    /**
     * Overrides default can burn check and only returns true if fire sources is enabled. This is not used by the spread action.
     * @param blockState The block state to check.
     * @return True if this block can burn blocks and that the provided block state is flammable.
     */
    @Override
    protected boolean canBurn(BlockState blockState)
    {
        if (!this.definition().useFireSource()) {
            return false;
        }
        return ((net.minecraft.world.level.block.FireBlock) Blocks.FIRE).getFlameOdds(blockState) > 0;
    }

    /**
     * Gets the block state when placing this block, this can change based on surrounding blocks.
     * @param blockPlaceContext The block place context.
     * @return The block state for placing this block.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext)
    {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        return this.getStateForLocation(this.defaultBlockState(), level, blockPos);
    }

    /**
     * Gets the block state for this block based on surrounding blocks.
     * @param level The level.
     * @param blockPos The position.
     * @return The block state to use.
     */
    public BlockState getStateForLocation(BlockState blockState, LevelReader level, BlockPos blockPos)
    {
        BlockPos blockPosBelow = blockPos.below();
        BlockState blockStateBelow = level.getBlockState(blockPosBelow);
        boolean fullBlock = this.definition().isSourceBlockState(level, blockStateBelow, blockPosBelow, Direction.UP) || blockStateBelow.isFaceSturdy(level, blockPosBelow, Direction.UP);

        for (Direction direction : Direction.values()) {
            BooleanProperty directionProperty = PROPERTY_BY_DIRECTION.get(direction);
            if (directionProperty == null) {
                continue;
            }

            // On source or solid block (set all sides to false for full fire block):
            if (fullBlock) {
                blockState = blockState.setValue(directionProperty, false);
                continue;
            }

            // Adjacent to side of block (side solid sides to true):
            BlockPos checkBlockPos = blockPos.relative(direction);
            BlockState checkBlockState = level.getBlockState(checkBlockPos);
            blockState = blockState.setValue(directionProperty, checkBlockState.isCollisionShapeFullBlock(level, checkBlockPos));
        }

        return blockState;
    }

    /**
     * Gets the voxel shape of this block for the given state.
     * @param blockState The block state.
     * @param blockGetter The block getter (usually a level instance).
     * @param blockPos The block position.
     * @param collisionContext The collision context.
     * @return The voxel shape of this block.
     */
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext)
    {
        blockState = this.definition().actor().defaultState(blockState);
        return this.shapesCache.get(blockState);
    }

    /**
     * Calculates the voxel shape of the provided block state. The block state should have unrelated properties stripped from it.
     * @param blockState The block state.
     * @return The voxel shape of this block.
     */
    protected static VoxelShape calculateShape(BlockState blockState)
    {
        VoxelShape voxelShape = Shapes.empty();
        if (blockState.getValue(UP)) {
            voxelShape = UP_AABB;
        }
        if (blockState.getValue(NORTH)) {
            voxelShape = Shapes.or(voxelShape, NORTH_AABB);
        }
        if (blockState.getValue(SOUTH)) {
            voxelShape = Shapes.or(voxelShape, SOUTH_AABB);
        }
        if (blockState.getValue(EAST)) {
            voxelShape = Shapes.or(voxelShape, EAST_AABB);
        }
        if (blockState.getValue(WEST)) {
            voxelShape = Shapes.or(voxelShape, WEST_AABB);
        }
        if (voxelShape.isEmpty()) {
            voxelShape = DOWN_AABB;
        }
        return voxelShape;
    }
}
