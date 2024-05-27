package com.schism.core.blocks.types;

import com.schism.core.blocks.BlockActor;
import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.IHasDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class FluidBlock extends LiquidBlock implements IHasDefinition<BlockDefinition>
{
    protected final BlockDefinition definition;
    protected final BlockActor actor;

    /**
     * Constructor
     * @param definition The Block Definition.
     * @param properties The Block Properties.
     * @param fluidSupplier A callback that returns the Fluid for this Fluid Block, this should be the still fluid.
     */
    public FluidBlock(BlockDefinition definition, BlockActor actor, Properties properties, Supplier<ForgeFlowingFluid> fluidSupplier)
    {
        super(fluidSupplier, properties);
        this.setRegistryName(definition.resourceLocation());
        this.definition = definition;
        this.actor = actor;
        this.registerDefaultState(this.actor.defaultState(this.getStateDefinition().any()));
    }

    @Override
    public BlockDefinition definition()
    {
        return this.definition;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
        list.add(Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        BlockActor.createBlockStateDefinition(builder);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState replaceBlockState, boolean moving)
    {
        if (this.checkSpreadAndMix(blockState, level, blockPos)) {
            level.scheduleTick(blockPos, blockState.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }
        this.actor.onPlace(blockState, level, blockPos, replaceBlockState, moving);
    }

    @Override
    public void neighborChanged(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block neighborBlock, @NotNull BlockPos neighborBlockPos, boolean someBoolean)
    {
        this.actor.neighborChangedBlock(blockState, level, blockPos, neighborBlock, neighborBlockPos, someBoolean);
        if (this.checkSpreadAndMix(blockState, level, blockPos)) {
            level.scheduleTick(blockPos, blockState.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState neighborBlockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos neighborBlockPos)
    {
        if (blockState.getFluidState().isSource() || neighborBlockState.getFluidState().isSource()) {
            levelAccessor.scheduleTick(blockPos, blockState.getFluidState().getType(), this.getFluid().getTickDelay(levelAccessor));
        }
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
        super.tick(blockState, level, blockPos, random);
        this.actor.tick(blockState, level, blockPos, random);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        super.entityInside(blockState, level, blockPos, entity);
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
        super.animateTick(blockState, level, blockPos, random);
        this.actor.animateTick(blockState, level, blockPos, random);
    }

    /**
     * Determines if this fluid block should schedule a tick so that it can spread in any direction, this also handles mixing with other blocks.
     * @param blockState The block state of the spreading fluid.
     * @param level The level.
     * @param blockPos The position of the spreading fluid.
     * @return True if this fluid should spread and needs a block update tick scheduled, false if not.
     */
    protected boolean checkSpreadAndMix(BlockState blockState, Level level, BlockPos blockPos)
    {
        return POSSIBLE_FLOW_DIRECTIONS.stream().allMatch(direction -> {
            BlockPos targetBlockPos = blockPos.relative(direction);
            if (level.getBlockState(targetBlockPos).getBlock() == this) {
                return true;
            }
            FluidState targetFluidState = level.getFluidState(targetBlockPos);

            // Mix with Water:
            if (targetFluidState.is(FluidTags.WATER)) {
                Block mixedBlock = level.getFluidState(blockPos).isSource() ? Blocks.STONE : Blocks.COBBLESTONE;
                this.mix(level, targetBlockPos, mixedBlock);
                return false;
            }

            // Mix with Lava:
            if (targetFluidState.is(FluidTags.LAVA)) {
                Block mixedBlock = level.getFluidState(blockPos).isSource() ? Blocks.ANDESITE : Blocks.COBBLESTONE;
                this.mix(level, targetBlockPos, mixedBlock);
                return false;
            }

            return true;
        });
    }

    /**
     * Mixes this block into a new block.
     * @param level The level.
     * @param blockPos The block position.
     */
    protected void mix(Level level, BlockPos blockPos, Block mixedBlock)
    {
        level.setBlockAndUpdate(blockPos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, blockPos, blockPos, mixedBlock.defaultBlockState()));
        level.levelEvent(1501, blockPos, 0); // Mixing fizz event.
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return !this.getFluid().is(FluidTags.LAVA);
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(LEVEL) == 0) {
            levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.getFluid().getBucket());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return this.getFluid().getPickupSound();
    }
}
