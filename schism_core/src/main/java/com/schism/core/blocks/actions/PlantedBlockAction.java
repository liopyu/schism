package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockActor;
import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PlantedBlockAction extends AbstractBlockAction
{
    protected final float removeChance;
    protected final int ageMin;
    protected final List<Direction> directionsSolid;
    protected final List<String> specificBlockIds;
    protected final List<Direction> directionsSpecific;
    protected final List<Direction> directionsFlammable;

    public PlantedBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.removeChance = dataStore.floatProp("remove_chance");
        this.ageMin = dataStore.intProp("age_min");
        this.directionsSolid = dataStore.directionListProp("directions_solid");
        this.specificBlockIds = dataStore.listProp("specific_block_ids").stream().map(DataStore::stringValue).toList();
        this.directionsSpecific = dataStore.directionListProp("directions_specific");
        this.directionsFlammable = dataStore.directionListProp("directions_flammable");
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos)
    {
        if (blockState.getOptionalValue(BlockActor.PERSISTENT).orElse(false)) {
            return true;
        }

        // Age Check:
        if (this.ageMin > 0 && this.definition().actor().age(blockState) < this.ageMin) {
            return true;
        }

        // Planted Checks:
        if (this.directionsSolid.stream().anyMatch(direction -> levelReader.getBlockState(blockPos.relative(direction)).isFaceSturdy(levelReader, blockPos.relative(direction), direction.getOpposite()))) {
            return true;
        }
        if (this.directionsSolid.stream().anyMatch(direction -> DataStore.idInList(levelReader.getBlockState(blockPos.relative(direction)).getBlock().getRegistryName(), this.specificBlockIds))) {
            return true;
        }
        return this.directionsFlammable.stream().anyMatch(direction -> levelReader.getBlockState(blockPos.relative(direction)).isFlammable(levelReader, blockPos.relative(direction), direction.getOpposite()));
    }

    @Override
    public BlockState tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        if (!this.canSurvive(blockState, level, blockPos) && (this.removeChance >= 1 || random.nextFloat() <= this.removeChance)) {
            level.removeBlock(blockPos, true);
            return null;
        }
        return blockState;
    }

    @Override
    public BlockState neighborChanged(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block neighborBlock, @NotNull BlockPos neighborBlockPos, boolean someBoolean)
    {
        if (!this.canSurvive(blockState, level, blockPos) && this.removeChance >= 1) {
            level.removeBlock(blockPos, true);
            return null;
        }
        return blockState;
    }

    @Override
    public BlockState neighborChangedBlockState(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, BlockState neighborBlockState, BlockPos neighborBlockPos)
    {
        if (!this.canSurvive(blockState, levelAccessor, blockPos) && this.removeChance >= 1) {
            levelAccessor.removeBlock(blockPos, true);
            return null;
        }
        return blockState;
    }
}
