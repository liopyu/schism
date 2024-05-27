package com.schism.core.projectiles.actions;

import com.schism.core.blocks.types.FireBlock;
import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import com.schism.core.util.Maths;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaceBlocksProjectileAction extends AbstractProjectileAction
{
    protected final CachedRegistryObject<Block> cachedBlock;
    protected final int radius;
    protected final int height;
    protected final boolean replaceSoftBlocks;
    protected final boolean onImpact;
    protected final int onPierceTick;

    public PlaceBlocksProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedBlock = new CachedRegistryObject<>(dataStore.stringProp("block_id"), () -> ForgeRegistries.BLOCKS);
        this.radius = dataStore.intProp("radius");
        this.height = dataStore.intProp("height");
        this.replaceSoftBlocks = dataStore.booleanProp("replace_soft_blocks");
        this.onImpact = dataStore.booleanProp("on_impact");
        this.onPierceTick = dataStore.intProp("on_pierce_tick");
    }

    @Override
    public void onPierceBlock(ProjectileEntity projectileEntity, BlockState blockState, BlockPos blockPos)
    {
        if (this.onPierceTick <= 0 || projectileEntity.tickCount() % this.onPierceTick != 0) {
            return;
        }
        this.placeBlocks(projectileEntity, blockPos.above());
    }

    @Override
    public void onImpactBlock(ProjectileEntity projectileEntity, BlockState blockState, BlockPos blockPos)
    {
        if (!this.onImpact) {
            return;
        }
        this.placeBlocks(projectileEntity, blockPos.above());
    }

    @Override
    public void onPierceEntity(ProjectileEntity projectileEntity, Entity entity)
    {
        if (this.onPierceTick <= 0 || projectileEntity.tickCount() % this.onPierceTick != 0 || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        this.placeBlocks(projectileEntity, livingEntity.blockPosition());
    }

    @Override
    public void onImpactEntity(ProjectileEntity projectileEntity, Entity entity)
    {
        if (!this.onImpact || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        this.placeBlocks(projectileEntity, livingEntity.blockPosition());
    }

    /**
     * Places blocks at the provided position.
     * @param projectileEntity The projectile placing blocks.
     * @param blockPos The position to place blocks at.
     */
    public void placeBlocks(ProjectileEntity projectileEntity, BlockPos blockPos)
    {
        Level level = projectileEntity.getLevel();
        BlockState placeState = this.cachedBlock.get().defaultBlockState();

        if (radius == 1) {
            if (!this.canReplace(level, blockPos)) {
                return;
            }
            if (placeState.getBlock() instanceof FireBlock fireBlock) {
                placeState = fireBlock.getStateForLocation(placeState, level, blockPos);
            } else if (placeState.getBlock() instanceof LiquidBlock liquidBlock) {
                placeState = liquidBlock.defaultBlockState().setValue(LiquidBlock.LEVEL, 8);
            }
            level.setBlock(blockPos, placeState, 3);
            return;
        }

        for (int distance = 0; distance < this.radius; distance++) {
            for (float angle = 0; angle < Maths.TAU; angle += 0.5F) {
                for (int y = 0; y < this.height; y++) {
                    BlockPos placePos = new BlockPos(
                            (int) Math.round(blockPos.getX() + Math.sin(angle) * distance),
                            blockPos.getY(),
                            (int) Math.round(blockPos.getZ() + Math.cos(angle) * distance)
                    );
                    if (!this.canReplace(level, placePos)) {
                        continue;
                    }
                    if (placeState.getBlock() instanceof FireBlock fireBlock) {
                        placeState = fireBlock.getStateForLocation(placeState, level, blockPos);
                    } else if (placeState.getBlock() instanceof LiquidBlock liquidBlock) {
                        placeState = liquidBlock.defaultBlockState().setValue(LiquidBlock.LEVEL, 8);
                    }
                    level.setBlock(placePos, placeState, 3);
                }
            }
        }
    }

    /**
     * Determines if the block at the provided block position can be replaced.
     * @param level The level to check in.
     * @param blockPos The block position to check.
     * @return True if the block can be replaced.
     */
    protected boolean canReplace(Level level, BlockPos blockPos)
    {
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isAir()) {
            return true;
        }
        if (this.replaceSoftBlocks && !blockState.isCollisionShapeFullBlock(level, blockPos)) {
            return level.getBlockEntity(blockPos) == null;
        }
        return false;
    }
}
