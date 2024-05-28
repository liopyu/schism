package com.lycanitesmobs.core.entity.navigate;

import com.google.common.collect.ImmutableSet;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.fluid.BaseFluidBlock;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CreaturePathNavigator extends GroundPathNavigator {

    public BaseCreatureEntity entityCreature;
    protected BlockPos climbTargetPos;

    public CreaturePathNavigator(BaseCreatureEntity entityCreature, World world) {
        super(entityCreature, world);
        this.entityCreature = entityCreature;
    }

    /** Create PathFinder with CreatureNodeProcessor. **/
    @Override
    protected PathFinder createPathFinder(int searchRange) {
        this.nodeEvaluator = new CreatureNodeProcessor();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, searchRange);
    }

    /** Returns true if the entity is capable of navigating at all. **/
    @Override
    protected boolean canUpdatePath() {
        if(this.entityCreature.isFlying())
            return true;
        if(this.entityCreature.isInWater()) {
            return this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer();
        }
        return this.mob.isOnGround() || this.mob.isPassenger();
    }

    /** Return the position to path from. **/
    @Override
    protected Vector3d getTempMobPos() {
        return new Vector3d(this.mob.position().x(), this.getSurfaceY(), this.mob.position().z());
    }

    /** Returns a new path from starting path position to the provided target position. **/
    @Override
    public Path createPath(BlockPos pos, int range) {
        this.climbTargetPos = pos;
        if (this.entityCreature.isFlying()) {
            return this.createPath(ImmutableSet.of(pos), 8, false, range);
        }
        if(this.entityCreature.isInWater()) {
            return this.createPath(ImmutableSet.of(pos), 8, false, range);
        }
        return super.createPath(pos, range);
    }

    /** Return the Y position to path from. **/
    protected int getSurfaceY() {
        // If can swim:
        if(this.entityCreature.isUnderWater()) {

            // Slow swimmers (water bobbing):
            if(this.entityCreature.shouldFloat() && !this.entityCreature.shouldDive()) {
                int posY = MathHelper.floor(this.mob.getY());
                Block block = this.level.getBlockState(new BlockPos(this.mob.getX(), posY, this.mob.getZ())).getBlock();
                int searchCount = 0;

                while (this.isSwimmableBlock(block)) { // Search up for surface.
                    ++posY;
                    block = this.level.getBlockState(new BlockPos(MathHelper.floor(this.mob.position().x()), posY, MathHelper.floor(this.mob.position().z()))).getBlock();
                    ++searchCount;
                    if (searchCount > 16) {
                        return (int)this.mob.getBoundingBox().minY;
                    }
                }
                return posY;
            }
        }

        // Path From Current Y Pos:
        return MathHelper.floor(this.mob.getY() + 0.5D);
    }

    /** Trims path data from the end to the first sun covered block. Copied from GroundPathNavigator. **/
    @Override
    protected void trimPath() {
        this.setAvoidSun(this.entityCreature.daylightBurns() && this.entityCreature.getCommandSenderWorld().isDay());
        super.trimPath();
    }

    /** Returns true if the path is a straight unblocked line, walking mobs also check for hazards along the line. **/
    @Override
    protected boolean canMoveDirectly(Vector3d startVec, Vector3d endVec, int sizeX, int sizeY, int sizeZ) {
        // Flight/Swimming:
        if(this.entityCreature.isFlying() || this.entityCreature.isUnderWater()) {
            Vector3d vec3d = new Vector3d(endVec.x, endVec.y + (double)this.mob.getBbHeight() * 0.5D, endVec.z);
            RayTraceResult.Type directTraceType =  this.level.clip(new RayTraceContext(startVec, vec3d, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.mob)).getType();
            return directTraceType == RayTraceResult.Type.MISS;
        }

        // Performance Size Cap:
        sizeX = Math.min(sizeX, 2);
        sizeY = Math.min(sizeY, 3);
        sizeZ = Math.min(sizeZ, 2);
        return super.canMoveDirectly(startVec, endVec, sizeX, sizeY, sizeZ);
    }

    /** Starts pathing to target entity. **/
    @Override
    public boolean moveTo(Entity targetEntity, double speedIn) {
        Path path = this.createPath(targetEntity, 1);

        if (path != null) {
            return this.moveTo(path, speedIn);
        }

        // Climbing:
        else if(this.entityCreature.canClimb()) {
            this.climbTargetPos = targetEntity.blockPosition();
            this.speedModifier = speedIn;
            return true;
        }

        return false;
    }

    /** Returns true if the block is a block that the creature can swim in (water and lava for lava creatures). **/
    protected boolean isSwimmableBlock(Block block) {
        return this.isSwimmableBlock(block, 0);
    }

    /** Cached check skips the checking of the block type for performance. **/
    protected boolean isSwimmableBlock(Block block, int cachedCheck) {
        if(block == null || block == Blocks.AIR) {
            return false;
        }
        if(cachedCheck == 1 || this.isWaterBlock(block)) {
            return !this.entityCreature.waterDamage();
        }
        if(cachedCheck == 2 || this.isLavaBlock(block)) {
            return !this.entityCreature.canBurn();
        }
        if(cachedCheck == 3 || this.isOozeBlock(block)) {
            return !this.entityCreature.canFreeze();
        }
        return false;
    }

    /** Returns true if the block is a water block. **/
    protected boolean isWaterBlock(Block block) {
        return block == Blocks.WATER;
    }

    /** Returns true if the block is a lava block. **/
    protected boolean isLavaBlock(Block block) {
        return block == Blocks.LAVA || block == ObjectManager.getBlock("purelava");
    }

    /** Returns true if the block is a ooze block. **/
    protected boolean isOozeBlock(Block block) {
        return block == ObjectManager.getBlock("ooze");
    }

    /** Returns true if the entity can move to the block position. **/
    @Override
    public boolean isStableDestination(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);

        // Flight/Swimming:
        if(this.entityCreature.isFlying() || (this.entityCreature.isUnderWater() && this.entityCreature.isStrongSwimmer())) {
            if(blockState.getMaterial().isLiquid()) {
            	return this.entityCreature.isStrongSwimmer();
			}
            return !blockState.getMaterial().isSolid();
        }

        // Standing on Fluids:
        if (blockState.getBlock() instanceof FlowingFluidBlock) {
            FlowingFluidBlock fluidBlock = (FlowingFluidBlock) blockState.getBlock();
            boolean safeFluid = true;
            if (fluidBlock instanceof BaseFluidBlock) {
                BaseFluidBlock baseFluidBlock = (BaseFluidBlock)fluidBlock;
                if (!this.entityCreature.hasElement(baseFluidBlock.getElement())) {
                    safeFluid = false;
                }
            }
            if (safeFluid && this.entityCreature.canStandOnFluid(fluidBlock.getFluid())) {
                if (blockState.is(Blocks.WATER) && !this.entityCreature.waterDamage()) {
                    return true;
                }
                if (blockState.is(Blocks.LAVA) && this.entityCreature.isLavaCreature) {
                    return true;
                }
            }
        }

        // Water/Lava Breathing:
        if (!this.entityCreature.canBreatheAir() && !this.isSwimmableBlock(blockState.getBlock())) {
            return false;
        }

        return super.isStableDestination(blockPos);
    }

    /** Checks if the given Path Node Type is valid for ground pathing. **/
    @Override
    protected boolean hasValidPathType(PathNodeType pathNodeType) {
        if (pathNodeType == PathNodeType.WATER) {
            return this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer();
        } else if (pathNodeType == PathNodeType.LAVA) {
            return this.entityCreature.isLavaCreature;
        } else {
            return pathNodeType != PathNodeType.OPEN;
        }
    }

    /** Follows the path moving to the next index when needed, etc. Called by tick() if canNavigate() and noPath() are both true. **/
    @Override
    protected void followThePath() {
        float entityWidth = this.mob.getDimensions(Pose.STANDING).width;
        Vector3d currentPos = this.getTempMobPos();

        // Flight:
        if(this.entityCreature.isFlying() || this.entityCreature.isUnderWater()) {
            float entitySize = entityWidth * entityWidth;

            if (currentPos.distanceToSqr(this.path.getEntityPosAtNode(this.mob, this.path.getNextNodeIndex())) < entitySize) {
                this.path.advance();
            }

            int pathIndexRange = 6;
            for (int pathIndex = Math.min(this.path.getNextNodeIndex() + pathIndexRange, this.path.getNodeCount() - 1); pathIndex > this.path.getNextNodeIndex(); --pathIndex) {
                Vector3d pathVector = this.path.getEntityPosAtNode(this.mob, pathIndex);

                if (pathVector.distanceToSqr(currentPos) <= 36.0D && this.canMoveDirectly(currentPos, pathVector, 0, 0, 0)) {
                    this.path.setNextNodeIndex(pathIndex);
                    break;
                }
            }

            this.doStuckDetection(currentPos);
            return;
        }

        // Walking:
        super.followThePath();
    }

    /** Called on entity update to update the navigation progress. **/
    @Override
    public void tick() {

        // Climbing Tick:
        if (this.isDone() && this.entityCreature.canClimb() && this.climbTargetPos != null) {
            double entitySize = this.mob.getDimensions(Pose.STANDING).width * this.mob.getDimensions(Pose.STANDING).width;
            if (this.mob.distanceToSqr(Vector3d.atLowerCornerOf(this.climbTargetPos)) >= entitySize && (this.mob.position().y() <= (double)this.climbTargetPos.getY() || this.mob.distanceToSqr(new Vector3d(this.climbTargetPos.getX(), MathHelper.floor(this.mob.position().y()), this.climbTargetPos.getZ())) >= entitySize)) {
                this.mob.getMoveControl().setWantedPosition((double)this.climbTargetPos.getX(), (double)this.climbTargetPos.getY(), (double)this.climbTargetPos.getZ(), this.speedModifier);
            }
            else {
                this.climbTargetPos = null;
            }
            return;
        }

        // Update Path and Move:
        super.tick();
    }
}
