package com.lycanitesmobs.core.entity.navigate;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.fluid.FluidState;
import net.minecraft.pathfinding.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

import javax.annotation.Nullable;

public class CreatureNodeProcessor extends WalkNodeProcessor implements ICreatureNodeProcessor {

    public BaseCreatureEntity entityCreature;

    public static double getGroundY(IBlockReader blockReader, BlockPos pos) {
        BlockPos blockpos = pos.below();
        VoxelShape voxelshape = blockReader.getBlockState(blockpos).getCollisionShape(blockReader, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
    }

    @Override
    public void prepare(Region region, MobEntity mob) {
        this.updateEntitySize(mob);
        super.prepare(region, mob);
        if(mob instanceof BaseCreatureEntity) {
            this.entityCreature = (BaseCreatureEntity) mob;
        }
    }

    @Override
    public void updateEntitySize(Entity updateEntity) {
        this.entityWidth = Math.min(MathHelper.floor(this.getWidth(true, updateEntity) + 1.0F), 3);
        this.entityHeight = Math.min(MathHelper.floor(updateEntity.getDimensions(Pose.STANDING).height + 1.0F), 3);
        this.entityDepth = Math.min(MathHelper.floor(this.getWidth(true, updateEntity) + 1.0F), 3);
    }

    /** Returns the starting position to create a new path from. **/
    @Override
    public PathPoint getStart() {

        // Flying/Strong Swimming:
        if(this.flying() || (this.entityCreature.isStrongSwimmer() && this.entityCreature.isUnderWater())) {
            return this.getNode(MathHelper.floor(this.mob.getBoundingBox().minX), MathHelper.floor(this.mob.getBoundingBox().minY + 0.5D), MathHelper.floor(this.mob.getBoundingBox().minZ));
        }

        return super.getStart();
    }

    /** Returns true if the entity is capable of pathing/moving in water at all. **/
    @Override
    public boolean canFloat() {
        if(this.entityCreature != null)
            return this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer();
        return super.canFloat();
    }

    /** Returns a PathPoint to the given coordinates. **/
    @Override
    public FlaggedPathPoint getGoal(double x, double y, double z) {
        // Flying/Strong Swimming:
        if(this.flying() || this.swimming()) {
            return new FlaggedPathPoint(this.getNode(MathHelper.floor(x - this.getWidth(false)), MathHelper.floor(y + 0.5D), MathHelper.floor(z - this.getWidth(false))));
        }
        return super.getGoal(x, y, z);
    }

    /** Checks points around the provided fromPoint and adds it to path options if it is a valid point to travel to. **/
    @Override
    public int getNeighbors(PathPoint[] pathOptions, PathPoint fromPoint) {
        this.updateEntitySize(mob);

        // Flying/Strong Swimming/Diving:
        if(this.flying() || this.swimming()) {
            int i = 0;
            for (Direction direction : Direction.values()) {
                PathPoint pathPoint = null;
                if(this.swimming()) {
                    pathPoint = this.getWaterNode(fromPoint.x + direction.getStepX(), fromPoint.y + direction.getStepY(), fromPoint.z + direction.getStepZ());
                }
                if(pathPoint == null) {
                    pathPoint = this.getFlightNode(fromPoint.x + direction.getStepX(), fromPoint.y + direction.getStepY(), fromPoint.z + direction.getStepZ());
                }
                if(pathPoint != null && !pathPoint.closed) {
                    pathOptions[i++] = pathPoint;
                }
            }
            return i;
        }

        return super.getNeighbors(pathOptions, fromPoint);
    }

    @Override
    public PathNodeType getBlockPathType(IBlockReader world, int x, int y, int z, MobEntity mobEntity, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
        if(this.swimming()) {
            return PathNodeType.WATER;
        }
        return super.getBlockPathType(world, x, y, z, mobEntity, Math.min(3, xSize), Math.min(2, ySize), Math.min(3, zSize), canBreakDoorsIn, canEnterDoorsIn);
    }








    /** Returns true if the entity should use swimming focused pathing. **/
    public boolean swimming() {
        if(this.entityCreature == null) {
            return false;
        }
        if(this.entityCreature.isInWater()) {
            return this.entityCreature.isStrongSwimmer() || (this.entityCreature.canWade() && this.entityCreature.shouldDive());
        }
        return false;
    }

    /** Returns true if the entity should use flight focused pathing. **/
    public boolean flying() {
        return this.entityCreature != null && this.entityCreature.isFlying() && !this.entityCreature.isUnderWater();
    }

    /**
     * Returns a width to path with.
     * @param blockChecks If true, this width is used for checking blocks, a reduced width can be returned for better performance here.
     * @return The entity width to path with.
     */
    public double getWidth(boolean blockChecks) {
		return this.getWidth(blockChecks, this.mob);
    }

    /**
     * Returns a width to path with.
     * @param blockChecks If true, this width is used for checking blocks, a reduced width can be returned for better performance here.
     * @param entity The entity to get the width of.
     * @return The entity width to path with.
     */
    public double getWidth(boolean blockChecks, Entity entity) {
		return Math.min(3, (double)entity.getDimensions(Pose.STANDING).width);
	}

    /** Flight Pathing **/
    protected PathPoint getFlightNode(int x, int y, int z) {
        PathNodeType pathnodetype = this.isFlyablePathNode(x, y, z);
        if(this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
            if(pathnodetype == PathNodeType.WATER)
                return this.getNode(x, y, z);
        }
        return pathnodetype == PathNodeType.OPEN ? this.getNode(x, y, z) : null;
    }

    protected PathNodeType isFlyablePathNode(int x, int y, int z) {
        BlockPos centerPos = new BlockPos(x, y, z);
        for (int i = 0; i <= this.entityWidth; ++i) {
            for (int j = 0; j <= Math.min(this.entityHeight, 2); ++j) {
                for (int k = 0; k <= this.entityDepth; ++k) {
                    BlockState iblockstate = this.level.getBlockState(centerPos.offset(i, k, j));

                    // Non-Solid:
					if (!iblockstate.getMaterial().isSolid() && !iblockstate.getMaterial().isLiquid()) {
						return PathNodeType.OPEN;
					}

                    // Check For Open Air:
                    if (iblockstate.getMaterial() != Material.AIR) {
                        // If Can Swim Check For Swimmable Node:
                        if(this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
                            return this.isSwimmablePathNode(x, y, z);
                        }
                        return PathNodeType.BLOCKED;
                    }
                }
            }
        }

        return PathNodeType.OPEN;
    }

    /** Power Swim Pathing **/
    @Nullable
    protected PathPoint getWaterNode(int x, int y, int z) {
        PathNodeType pathnodetype;
        if(this.entityCreature != null && this.entityCreature.isFlying()) {
            pathnodetype = this.isFlyablePathNode(x, y, z);
            if(pathnodetype == PathNodeType.OPEN)
                return this.getNode(x, y, z);
        }
        else {
            pathnodetype = this.isSwimmablePathNode(x, y, z);
        }
        return pathnodetype == PathNodeType.WATER ? this.getNode(x, y, z) : null;
    }

    protected PathNodeType isSwimmablePathNode(int x, int y, int z) {
        BlockPos centerPos = new BlockPos(x, y, z);
        for (int i = 0; i <= this.entityWidth; ++i) {
            for (int j = 0; j <= Math.min(this.entityHeight, 2); ++j) {
                for (int k = 0; k <= this.entityDepth; ++k) {
                    BlockPos blockPos = centerPos.offset(i, k, j);

                    // Block State Checks:
                    BlockState blockState = this.level.getBlockState(blockPos);

                    if(this.entityCreature == null || !blockState.isPathfindable(this.level, blockPos, PathType.WATER)) {
                        if(j == y) { // Y must be water.
                            return PathNodeType.BLOCKED;
                        }
                        if(!blockState.isPathfindable(this.level, blockPos, PathType.AIR) && !blockState.isPathfindable(this.level, blockPos, PathType.WATER)) { // Blocked above water.
                            return PathNodeType.BLOCKED;
                        }
                    }

                    // Water Damages:
                    if (this.entityCreature.waterDamage() && blockState.getBlock() == Blocks.WATER) {
                        return PathNodeType.BLOCKED;
                    }

                    // Lava Damages:
                    if (this.entityCreature.canBurn() && blockState.getBlock() == Blocks.LAVA) {
                        return PathNodeType.BLOCKED;
                    }

                    // Ooze Swimming (With Water Damage):
                    if(!this.entityCreature.canFreeze() && ObjectManager.getBlock("ooze") != null && blockState.getBlock() == ObjectManager.getBlock("ooze")) {
                        return PathNodeType.WATER;
                    }

                    // Custom Fluid State Checks: - Added some direct checks to improve performance.
                    if (blockState.getBlock() instanceof FlowingFluidBlock) {
                        FluidState fluidState = this.level.getFluidState(blockPos);

                        // Water Damages:
                        if (this.entityCreature.waterDamage() && fluidState.is(FluidTags.WATER)) {
                            return PathNodeType.BLOCKED;
                        }

                        // Lava Damages:
                        if (this.entityCreature.canBurn() && fluidState.is(FluidTags.LAVA)) {
                            return PathNodeType.BLOCKED;
                        }
                    }
                }
            }
        }
        return PathNodeType.WATER;
    }
}
