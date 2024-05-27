package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.Action;
import com.schism.core.database.DataStore;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

abstract public class AbstractBlockAction extends Action<BlockDefinition>
{
    public AbstractBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);
    }

    /**
     * Creates a list of block actions from the provided map.
     * @param definition The definition the list is for.
     * @param dataStoreMap A map of keys and nested data stores for populating actions with.
     * @return The newly created block action list.
     */
    public static List<AbstractBlockAction> listFromMap(BlockDefinition definition, Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream()
                .map(dataStoreEntry -> AbstractBlockAction.create(definition, dataStoreEntry))
                .filter(Objects::nonNull).toList();
    }

    /**
     * Creates a new block action instance for the provided data store entry.
     * @param definition The definition the action is for.
     * @param dataStoreEntry The data store entry to get action properties from.
     * @return The newly created block action.
     */
    public static AbstractBlockAction create(BlockDefinition definition, Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "age" -> new AgeBlockAction(definition, dataStore);
            case "damage" -> new DamageBlockAction(definition, dataStore);
            case "deteriorate" -> new DeteriorateBlockAction(definition, dataStore);
            case "effect" -> new EffectBlockAction(definition, dataStore);
            case "environment" -> new EnvironmentBlockAction(definition, dataStore);
            case "particles" -> new ParticlesBlockAction(definition, dataStore);
            case "planted" -> new PlantedBlockAction(definition, dataStore);
            case "spread" -> new SpreadBlockAction(definition, dataStore);
            case "kill" -> new KillBlockAction(definition, dataStore);
            case "move" -> new MoveBlockAction(definition, dataStore);
            case "sound" -> new SoundBlockAction(definition, dataStore);
            case "speed" -> new SpeedBlockAction(definition, dataStore);
            default -> throw new RuntimeException("Invalid Block Action Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Called by the subject block when it is placed into the level.
     * @param blockState The block state being placed.
     * @param level The level.
     * @param blockPos The position the block is placed at.
     * @param replaceBlockState The block state that is being replaced.
     * @param moving If the block was moved by a piston or something?
     */
    public BlockState onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState replaceBlockState, boolean moving)
    {
        return blockState;
    }

    /**
     * Called by the subject block when an adjacent block changes.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The position of the block.
     * @param neighborBlock The neighbor block that changed.
     * @param neighborBlockPos The neighbor block position.
     * @param someBoolean Not sure what this is lol.
     * @return The block state that the actor's block should have in response.
     */
    public BlockState neighborChanged(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block neighborBlock, @NotNull BlockPos neighborBlockPos, boolean someBoolean)
    {
        return blockState;
    }

    /**
     * Called by the subject block when an adjacent block updates its state.
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
        return blockState;
    }

    /**
     * Called by the subject block to determine if it can survive in its current position.
     * @param blockState The block state.
     * @param levelReader The level reader.
     * @param blockPos The block position.
     * @return True if the block can survive, false otherwise.
     */
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos)
    {
        return true;
    }

    /**
     * Called by the subject block when it ticks.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The position.
     * @param random An instance of random.
     * @return Returns the block state that was passed or a modified version of it or null if ticking should halt.
     */
    public BlockState tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        return blockState;
    }

    /**
     * Called by the subject block when an entity is inside it every tick.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The block position.
     * @param entity The entity that is inside.
     */
    public BlockState entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        return blockState;
    }

    /**
     * Called by the subject block when it is used by a player.
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
        return InteractionResult.PASS;
    }

    /**
     * Called by the subject block when doing a client side animation tick, used for particles and sounds.
     * @param blockState The block state.
     * @param level The level.
     * @param blockPos The block position.
     * @param random An instance of random.
     */
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random)
    {

    }
}
