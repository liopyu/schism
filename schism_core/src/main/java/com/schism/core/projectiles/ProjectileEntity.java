package com.schism.core.projectiles;

import com.schism.core.database.IHasDefinition;
import com.schism.core.util.Vec3;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class ProjectileEntity extends Projectile implements IHasDefinition<ProjectileDefinition>
{
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(ProjectileEntity.class, EntityDataSerializers.FLOAT);

    protected final ProjectileDefinition definition;
    protected final int lifetime;
    protected float renderTicks;

    public ProjectileEntity(ProjectileDefinition definition, Level level)
    {
        super(definition.entityType(), level);
        this.definition = definition;
        this.lifetime = definition.lifetime(level.getRandom());
    }

    /**
     * Initialises the position, owner and delta of this projectile from the provided owner entity.
     * @param entity The owner entity to shoot this projectile from.
     */
    public void initFromEntity(Entity entity)
    {
        this.setOwner(entity);
        this.setPos(entity.getEyePosition());
        this.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0F, this.definition().velocity(), 1F);
    }

    @Override
    public ProjectileDefinition definition()
    {
        return this.definition;
    }

    @Override
    protected void defineSynchedData()
    {
        this.getEntityData().define(SCALE, 1F);
    }

    /**
     * Gets how many ticks this entity instance has been active in the level for.
     * @return The entity tick count.
     */
    public int tickCount()
    {
        return this.tickCount;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.tickCount > this.lifetime) {
            this.expire();
            return;
        }
        this.definition().actions().forEach(action -> action.onTick(this));
        this.collisionTick();
        this.moveTick();
    }

    /**
     * Performs collision updates.
     */
    protected void collisionTick()
    {
        this.checkInsideBlocks();

        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        if (hitResult instanceof BlockHitResult blockHitResult) {
            this.onHitBlock(blockHitResult);
        } else if (hitResult instanceof EntityHitResult entityHitResult) {
            this.onHitEntity(entityHitResult);
        }
    }

    /**
     * Moves the projectile based on deltas.
     */
    protected void moveTick()
    {
        Vec3 deltaMovement = new Vec3(this.getDeltaMovement());
        Vec3 nextPosition = new Vec3(this.position()).add(deltaMovement);
        this.setDeltaMovement(deltaMovement.add(0, -this.definition().gravity() * 0.1F, 0).physVec3());
        this.updateRotation();
        this.setPos(nextPosition.physVec3());
    }

    @Override
    protected void checkInsideBlocks() {
        AABB boundingBox = this.getBoundingBox();
        BlockPos blockPosMin = new BlockPos(boundingBox.minX + 0.001D, boundingBox.minY + 0.001D, boundingBox.minZ + 0.001D);
        BlockPos blockPosMax = new BlockPos(boundingBox.maxX - 0.001D, boundingBox.maxY - 0.001D, boundingBox.maxZ - 0.001D);
        if (this.level.hasChunksAt(blockPosMin, blockPosMax)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for(int x = blockPosMin.getX(); x <= blockPosMax.getX(); ++x) {
                for(int y = blockPosMin.getY(); y <= blockPosMax.getY(); ++y) {
                    for(int z = blockPosMin.getZ(); z <= blockPosMax.getZ(); ++z) {
                        mutableBlockPos.set(x, y, z);
                        BlockState blockState = this.level.getBlockState(mutableBlockPos);
                        try {
                            blockState.entityInside(this.level, mutableBlockPos, this);
                            if (!blockState.isAir()) {
                                this.onInsideBlock(blockState, mutableBlockPos);
                            }
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                            CrashReportCategory.populateBlockDetails(crashreportcategory, this.level, mutableBlockPos, blockState);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called each update tick when this projectile is inside a block excluding air.
     * Note: This projectile can be inside multiple blocks at the same time.
     * @param blockState The block state this projectile is inside.
     * @param blockPos The position of the block that this projectile is inside.
     */
    protected void onInsideBlock(BlockState blockState, BlockPos blockPos) {
        this.definition().actions().forEach(action -> action.onPierceBlock(this, blockState, blockPos));
    }

    @Override
    public void onHitBlock(BlockHitResult hitResult)
    {
        BlockState blockState = this.getLevel().getBlockState(hitResult.getBlockPos());
        boolean solidBlock = blockState.isCollisionShapeFullBlock(this.getLevel(), hitResult.getBlockPos());
        if (solidBlock && this.definition().impactSolidBlocks()) {
            this.definition().actions().forEach(action -> action.onImpactBlock(this, blockState, hitResult.getBlockPos()));
            this.discard();
            return;
        }
        if (!solidBlock && this.definition().impactHollowBlocks()) {
            this.definition().actions().forEach(action -> action.onImpactBlock(this, blockState, hitResult.getBlockPos()));
            this.discard();
        }
    }

    @Override
    public void onHitEntity(@NotNull EntityHitResult hitResult)
    {
        if (this.definition().impactEntities()) {
            this.definition().actions().forEach(action -> action.onImpactEntity(this, hitResult.getEntity()));
            this.discard();
            return;
        }
        this.definition().actions().forEach(action -> action.onPierceEntity(this, hitResult.getEntity()));
    }

    /**
     * Discards this projectile, called when this projectile has exceeded its lifetime.
     */
    public void expire()
    {
        this.definition().actions().forEach(action -> action.onExpire(this));
        this.discard();
    }

    /**
     * Returns the current size of this projectile, this affects both collision and rendering.
     * @return The size of this projectile.
     */
    public float size()
    {
        return 1;
    }

    /**
     * Gets the position of this projectile's laser end when active.
     * @return The laser end position or vec 3 zero for no laser end.
     */
    public Vec3 laserEnd()
    {
        return Vec3.ZERO;
    }

    /**
     * Gets and increases the render tick of this projectile. Available for smoother animations.
     * @param deltaTicks The number of ticks since the last render frame to increase by.
     * @return The render tick for this projectile, also increases the tick.
     */
    public float renderTicks(float deltaTicks)
    {
        return this.renderTicks += deltaTicks;
    }
}
