package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.blocks.types.FireBlock;
import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SpreadBlockAction extends AbstractBlockAction
{
    protected final float spreadChance;
    protected final boolean spreadFlammable;
    protected final List<String> spreadBlockIds;
    protected final CachedRegistryObject<Block> alternateBlock;
    protected final float alternateBlockChance;

    public SpreadBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.spreadChance = dataStore.floatProp("spread_chance");
        this.spreadFlammable = dataStore.booleanProp("spread_flammable");
        this.spreadBlockIds = dataStore.listProp("spread_block_ids").stream().map(DataStore::stringValue).collect(Collectors.toList());
        this.alternateBlock = new CachedRegistryObject<>(dataStore.stringProp("alternate_block_id"), () -> ForgeRegistries.BLOCKS);
        this.alternateBlockChance = dataStore.floatProp("alternate_block_chance");
    }

    @Override
    public BlockState tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        // Handle Spreading:
        int age = this.definition().actor().age(blockState);
        if (this.spreadChance <= 0 || (this.spreadChance < 1 && this.spreadChance < random.nextFloat())) {
            return blockState;
        }
        boolean reduceSpread = this.isPoorLocation(level, blockPos);
        int spreadStrength = reduceSpread ? -50 : 0;
        Arrays.stream(Direction.values()).toList().forEach(direction -> this.trySpreadTo(level, blockPos.relative(direction), spreadStrength, random, age, direction.getOpposite()));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                for (int y = -1; y <= 4; ++y) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    int spreadChallenge = 100;
                    if (y > 1) {
                        spreadChallenge += (y - 1) * 100;
                    }

                    mutableBlockPos.setWithOffset(blockPos, x, y, z);
                    int odds = this.getSpreadOdds(level, mutableBlockPos);
                    if (odds > 0) {
                        int spreadPower = (odds + 40 + level.getDifficulty().getId() * 7) / (age + 30);
                        if (reduceSpread) {
                            spreadPower /= 2;
                        }

                        if (spreadPower > 0 && random.nextInt(spreadChallenge) <= spreadPower && !this.isNearRain(level, mutableBlockPos)) {
                            int spreadAge = Math.min(this.definition().age(), age + 1);
                            level.setBlock(mutableBlockPos, this.getSpreadStateWithAge(level, mutableBlockPos, spreadAge, random), 3);
                        }
                    }
                }
            }
        }

        return blockState;
    }

    /**
     * Determines if the provided position should reduce the spread rate of the subject block.
     * @param level The level.
     * @param blockPos The position to check.
     * @return True if the provided position is a valid position for the subject block to be at.
     */
    private boolean isPoorLocation(Level level, BlockPos blockPos)
    {
        return this.definition().useFireSource() && level.isHumidAt(blockPos);
    }

    /**
     * Attempts to spread the subject block to the provided position.
     * @param level The level.
     * @param blockPos The position to spread to.
     * @param strength The initial strength of the spread, this is affected by the spread direction.
     * @param random An instance of random.
     * @param age The age of the subject block.
     * @param direction The direction to spread in.
     */
    private void trySpreadTo(Level level, BlockPos blockPos, int strength, Random random, int age, Direction direction)
    {
        strength += (direction == Direction.UP || direction == Direction.DOWN) ? 250 : 300;
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.getBlock() == this.definition().block()) {
            return;
        }
        int flammability = blockState.getFlammability(level, blockPos, direction);
        if (random.nextInt(strength) < flammability) {
            if (random.nextInt(age + 10) < 5 && !level.isRainingAt(blockPos)) {
                int spreadAge = Math.min(age + 1, this.definition().age());
                level.setBlock(blockPos, this.getSpreadStateWithAge(level, blockPos, spreadAge, random), 3);
            } else {
                level.removeBlock(blockPos, false);
            }
            blockState.onCaughtFire(level, blockPos, direction, null);
        }

    }

    /**
     * Creates a new instance of the subject block state with the provided age.
     * @param level The level.
     * @param blockPos The position to spread to.
     * @param age The age to set.
     * @param random An instance of random used for alternate blocks.
     * @return A new instance of the subject block state with the provided age.
     */
    private BlockState getSpreadStateWithAge(Level level, BlockPos blockPos, int age, Random random)
    {
        BlockState blockState;
        if (this.alternateBlock.isPresent() && this.alternateBlockChance > 0 && this.alternateBlockChance > random.nextFloat()) {
            blockState = this.alternateBlock.get().defaultBlockState();
        } else {
            blockState = this.definition().block().defaultBlockState();
            blockState = this.definition().actor().setAge(blockState, age);
        }
        if (blockState.getBlock() instanceof FireBlock fireBlock) {
            blockState = fireBlock.getStateForLocation(blockState, level, blockPos);
        }
        return blockState;
    }

    /**
     * Gets the odds of spreading to the provided position, this uses fire spread odds from other blocks.
     * @param level The level.
     * @param blockPos The position to spread to.
     * @return The odds of spreading to the provided position.
     */
    private int getSpreadOdds(Level level, BlockPos blockPos)
    {
        if (!level.isEmptyBlock(blockPos)) {
            return 0;
        }
        int spreadOdds = 0;
        for (Direction direction : Direction.values()) {
            BlockState blockState = level.getBlockState(blockPos.relative(direction));
            spreadOdds = Math.max(blockState.getFireSpreadSpeed(level, blockPos.relative(direction), direction.getOpposite()), spreadOdds);
        }
        return spreadOdds;
    }

    /**
     * Determines if rain should prevent spreading to the provided position.
     * @param level The level.
     * @param blockPos The block position to check around.
     * @return True if rain should prevent spreading to the provided position.
     */
    private boolean isNearRain(Level level, BlockPos blockPos)
    {
        if (!this.definition().useFireSource() || !level.isRaining()) {
            return false;
        }
        return level.isRainingAt(blockPos) || level.isRainingAt(blockPos.west()) || level.isRainingAt(blockPos.east()) || level.isRainingAt(blockPos.north()) || level.isRainingAt(blockPos.south());
    }
}
