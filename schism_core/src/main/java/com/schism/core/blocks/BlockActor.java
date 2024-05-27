package com.schism.core.blocks;

import com.schism.core.blocks.actions.AbstractBlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class BlockActor
{
    public static final IntegerProperty AGE_1 = BlockStateProperties.AGE_1;
    public static final IntegerProperty AGE_2 = BlockStateProperties.AGE_2;
    public static final IntegerProperty AGE_7 = BlockStateProperties.AGE_7;
    public static final IntegerProperty AGE_15 = BlockStateProperties.AGE_15;
    public static final IntegerProperty AGE_25 = BlockStateProperties.AGE_25;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public static final BooleanProperty STATIC = BooleanProperty.create("static");

    protected final BlockDefinition definition;
    protected final boolean hasActions;
    protected final IntegerProperty ageProperty;

    /**
     * Creates the block state definition for this actor's block.
     * @param builder The block state definition builder.
     */
    public static void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        BlockDefinition definition = BlockDefinition.LATEST_BUILDING_DEFINITION;
        if (definition == null) {
            throw new RuntimeException("Block is being created without BlockDefinition.LATEST_BUILDING_DEFINITION being set, this is needed to get definition info when creating block states.");
        }
        if (definition.actions().isEmpty()) {
            return;
        }
        builder.add(PERSISTENT, STATIC);
        if (definition.age() > 0) {
            switch (definition.age()) {
                case 1 -> builder.add(AGE_1);
                case 2 -> builder.add(AGE_2);
                case 7 -> builder.add(AGE_7);
                case 15 -> builder.add(AGE_15);
                case 25 -> builder.add(AGE_25);
                default -> throw new RuntimeException("Invalid block age: " + definition.age() + " must be either 1, 2, 7, 16 or 25 this is because it uses fixed block states.");
            }
        }
    }

    /**
     * An action call which runs a method on an action passing a block state and other values and returning a block state.
     */
    public interface ActionCall {
        BlockState call(AbstractBlockAction action, BlockState blockState);
    }

    /**
     * Block Actors perform actions on behalf of a block.
     * @param definition The definition of the block.
     */
    public BlockActor(BlockDefinition definition)
    {
        this.definition = definition;
        this.hasActions = !definition.actions().isEmpty();
        this.ageProperty = switch (definition.age()) {
            case 1 -> AGE_1;
            case 2 -> AGE_2;
            case 7 -> AGE_7;
            case 15 -> AGE_15;
            case 25 -> AGE_25;
            default -> null;
        };
    }

    public BlockDefinition definition()
    {
        return this.definition;
    }

    /**
     * Sets the provided state to default for all action related states.
     * @param blockState The block state to set to default.
     * @return The block state set to default for all action related states.
     */
    public BlockState defaultState(BlockState blockState)
    {
        if (!this.hasActions) {
            return blockState;
        }
        blockState = blockState.setValue(PERSISTENT, this.definition().persistentDefault());
        blockState = blockState.setValue(STATIC, false);
        blockState = this.setAge(blockState, 0);
        return blockState;
    }

    /**
     * Determines if the provided block matches all default action related states.
     * @param blockState The block state to check.
     * @return True if the block state's values related to actions are all at their defaults.
     */
    public boolean isDefault(BlockState blockState)
    {
        if (!this.hasActions) {
            return true;
        }
        if (blockState.getValue(PERSISTENT) != this.definition().persistentDefault()) {
            return false;
        }
        if (blockState.getValue(STATIC)) {
            return false;
        }
        return this.age(blockState) == 0;
    }

    /**
     * Returns the age of the provided block state.
     * @param blockState The block state to get the age for.
     * @return The age of the block state.
     */
    public int age(BlockState blockState)
    {
        if (this.ageProperty == null) {
            return 0;
        }
        return blockState.getValue(this.ageProperty);
    }

    /**
     * Sets the age of the provided block state.
     * @param blockState The block state to set the age for.
     * @param age The age to set.
     * @return The new block state.
     */
    public BlockState setAge(BlockState blockState, int age)
    {
        if (this.ageProperty == null) {
            return blockState;
        }
        return blockState.setValue(this.ageProperty, Math.min(age, this.definition().age()));

    }

    /**
     * Calls the provided function on all actions that this actor has and returns a potentially modified block state.
     * @param blockState The block state.
     * @param actionCall The action call to call on each action.
     * @return A potentially modified block state.
     */
    @NotNull
    protected BlockState callActions(BlockState blockState, ActionCall actionCall)
    {
        if (!this.hasActions) {
            return blockState;
        }
        for (AbstractBlockAction action : this.definition().actions()) {
            if (blockState == null || blockState.getBlock() != this.definition().block()) {
                break;
            }
            blockState = actionCall.call(action, blockState);
        }
        return Objects.requireNonNullElse(blockState, Blocks.AIR.defaultBlockState());
    }

    /**
     * Called by this actor's block when it is placed into the level.
     * @param blockState The block state being placed.
     * @param level The level.
     * @param blockPos The position the block is placed at.
     * @param replaceBlockState The block state that is being replaced.
     * @param moving If the block was moved by a piston or something?
     */
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState replaceBlockState, boolean moving)
    {
        if (this.definition().ticks()) {
            level.scheduleTick(blockPos, this.definition().block(), this.definition().tickRate(level.getRandom()));
        }
        this.callActions(blockState, (AbstractBlockAction action, BlockState changeableBlockState) -> action.onPlace(blockState, level, blockPos, replaceBlockState, moving));
    }

    /**
     * Called by this actor's block when an adjacent block changes to a different block.
     * @param blockState The block state of this actor's block.
     * @param level The level.
     * @param blockPos The position of this actor's block.
     * @param neighborBlock The neighbor block that changed.
     * @param neighborBlockPos The neighbor block position.
     * @param someBoolean Not sure what this is lol.
     */
    public void neighborChangedBlock(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block neighborBlock, @NotNull BlockPos neighborBlockPos, boolean someBoolean)
    {
        if (this.definition().ticks()) {
            level.scheduleTick(blockPos, this.definition().block(), this.definition().tickRate(level.getRandom()));
        }
        this.callActions(blockState, (AbstractBlockAction action, BlockState changeableBlockState) -> action.neighborChanged(blockState, level, blockPos, neighborBlock, neighborBlockPos, someBoolean));
    }

    /**
     * Called by this actor's block when an adjacent block updates its state.
     * @param blockState The block state of this actor's block.
     * @param levelAccessor The level accessor.
     * @param blockPos The block position of the actor's block.
     * @param direction The direction of the changed block relative to the actor's block.
     * @param neighborBlockState The block state of the neighbor block that changed.
     * @param neighborBlockPos The block position of the neighbor block that changed.
     * @return The block state that the actor's block should have in response.
     */
    public BlockState neighborChangedBlockState(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, BlockState neighborBlockState, BlockPos neighborBlockPos)
    {
        return this.callActions(blockState, (AbstractBlockAction action, BlockState changeableBlockState) -> action.neighborChangedBlockState(blockState, levelAccessor, blockPos, direction, neighborBlockState, neighborBlockPos));
    }

    /**
     * Called by this actor's block to determine if it can survive in its current position.
     * @param blockState The block state.
     * @param levelReader The level reader.
     * @param blockPos The block position.
     * @return True if the block can survive, false otherwise.
     */
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos)
    {
        if (!this.hasActions) {
            return true;
        }
        return this.definition().actions().stream().allMatch(action -> action.canSurvive(blockState, levelReader, blockPos));
    }

    /**
     * Called by this actor's block when it ticks.
     * @param blockState The block state of this actor's block.
     * @param level The (server side only) level.
     * @param blockPos The position of this actor's block.
     * @param random An instance of random.
     */
    public void tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        this.callActions(blockState, ((AbstractBlockAction action, BlockState changeableBlockState) -> action.tick(changeableBlockState, level, blockPos, random)));
    }

    /**
     * Called by this actor's block when an entity is inside it every tick.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The block position.
     * @param entity The entity that is inside.
     */
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        this.callActions(blockState, ((AbstractBlockAction action, BlockState changeableBlockState) -> action.entityInside(blockState, level, blockPos, entity)));
    }

    /**
     * Called by this actor's block when it is used by a player.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The block position.
     * @param player The player using the block.
     * @param interactionHand The hand that the player is using the block with.
     * @param blockHitResult The block hit result.
     * @return An interaction result.
     */
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult)
    {
        InteractionResult result = InteractionResult.PASS;
        if (!this.hasActions) {
            return result;
        }
        for (AbstractBlockAction action : this.definition().actions()) {
            result = action.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return result;
    }

    /**
     * Called by this actor's block when doing a client side animation tick, used for particles and sounds.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The block position.
     * @param random An instance of random.
     */
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random)
    {
        if (!this.hasActions) {
            return;
        }
        this.definition().actions().forEach((AbstractBlockAction action) -> action.animateTick(blockState, level, blockPos, random));
    }
}
