package com.lycanitesmobs.core.entity.navigate;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;

public class CreatureMoveController extends MovementController {

    protected BaseCreatureEntity entityCreature;
    /** Used by flight movement for changing course, makes for smoother movement. **/
    protected int courseChangeCooldown;

    public CreatureMoveController(BaseCreatureEntity baseCreatureEntity) {
        super(baseCreatureEntity);
        this.entityCreature = baseCreatureEntity;
    }

    /** Called on update to move the entity. **/
    @Override
    public void tick() {
        // Rider:
        if(this.isControlledByRider()) {
            return;
        }

        // Swimming:
        if(this.entityCreature.isStrongSwimmer() && this.entityCreature.isUnderWater()) {
            this.tickSwimming();
            return;
        }

        // Flying:
        if(this.entityCreature.isFlying() && !this.entityCreature.isUnderWater()) {
            this.tickFlying();
            return;
        }

        // Walking:
        this.tickWalking();
    }


    // ==================== Checks ====================
    /** Returns true if the entity is controlled by its rider. **/
    public boolean isControlledByRider() {
        // Mounted By Player:
        if(this.entityCreature != null && this.entityCreature.getControllingPassenger() instanceof PlayerEntity && this.entityCreature.canBeControlledByRider()) {
            return true;
        }

        return false;
    }


    // ==================== Movements ====================
    /** Used by land entities for ground movement. **/
    public void tickWalking() {
        float moveZ;
        if (this.operation == MovementController.Action.STRAFE) {
            float moveSpeed = (float)this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            float scaledSpeed = (float)this.speedModifier * moveSpeed;
            float moveForward = this.strafeForwards;
            float moveStrafe = this.strafeRight;
            float velocity = MathHelper.sqrt(moveForward * moveForward + moveStrafe * moveStrafe);
            if (velocity < 1.0F) {
                velocity = 1.0F;
            }

            velocity = scaledSpeed / velocity;
            moveForward *= velocity;
            moveStrafe *= velocity;
            float yawSin = MathHelper.sin(this.mob.yRot * 0.017453292F);
            float yawCos = MathHelper.cos(this.mob.yRot * 0.017453292F);
            float moveX = moveForward * yawCos - moveStrafe * yawSin;
            moveZ = moveStrafe * yawCos + moveForward * yawSin;
            PathNavigator pathNavigator = this.mob.getNavigation();
            NodeProcessor nodeProcessor = pathNavigator.getNodeEvaluator();
            if (nodeProcessor.getBlockPathType(this.mob.level, MathHelper.floor(this.mob.position().x() + (double) moveX), MathHelper.floor(this.mob.position().y()), MathHelper.floor(this.mob.position().z() + (double) moveZ)) != PathNodeType.WALKABLE) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
                scaledSpeed = moveSpeed;
            }

            this.mob.setSpeed(scaledSpeed);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = MovementController.Action.WAIT;
        }
        else if (this.operation == MovementController.Action.MOVE_TO) {
            this.operation = MovementController.Action.WAIT;
            double distanceX = this.wantedX - this.mob.position().x();
            double distanceZ = this.wantedZ - this.mob.position().z();
            double distanceY = this.wantedY - this.mob.position().y();
            double distanceXZ = distanceX * distanceX + distanceZ * distanceZ;
            double distance = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
            if (distance < 2.500000277905201E-7D) {
                this.mob.setZza(0.0F);
                return;
            }

            moveZ = (float)(MathHelper.atan2(distanceZ, distanceX) * 57.2957763671875D) - 90.0F;
            this.mob.yRot = this.rotlerp(this.mob.yRot, moveZ, 90.0F);
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));

            // Jumping:
            BlockPos entityPos = this.mob.blockPosition();
            BlockState blockState = this.mob.level.getBlockState(entityPos);
            VoxelShape collisionShape = blockState.getCollisionShape(this.mob.level, entityPos);
            double jumpRange = (double)Math.max(1.0F, this.mob.getDimensions(Pose.STANDING).width + 0.25F);
            if (distanceY > (double)this.mob.maxUpStep && distanceXZ < jumpRange || !collisionShape.isEmpty() && this.mob.position().y() < collisionShape.max(Direction.Axis.Y) + (double)entityPos.getY()) {
                this.mob.getJumpControl().jump();
                this.operation = MovementController.Action.JUMPING;
            }
        }
        else if (this.operation == MovementController.Action.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));
            if (this.mob.isOnGround()) {
                this.operation = MovementController.Action.WAIT;
            }
        }
        else {
            this.mob.setZza(0.0F);
        }
    }

    /** Used by strong swimmers for fast, smooth movement. **/
    public void tickSwimming() {
        if (this.operation == MovementController.Action.MOVE_TO && !this.entityCreature.getNavigation().isDone()) {
            double x = this.wantedX - this.entityCreature.position().x();
            double y = this.wantedY - this.entityCreature.position().y();
            double z = this.wantedZ - this.entityCreature.position().z();
            double distance = x * x + y * y + z * z;
            distance = MathHelper.sqrt(distance);
            y = y / distance;
            float f = (float)(MathHelper.atan2(z, x) * (180D / Math.PI)) - 90.0F;
            this.entityCreature.yRot = this.rotlerp(this.entityCreature.yRot, f, 90.0F);
            this.entityCreature.yBodyRot = this.entityCreature.yRot;
            float f1 = (float)(this.speedModifier * this.entityCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
            this.entityCreature.setSpeed(this.entityCreature.getSpeed() + (f1 - this.entityCreature.getSpeed()) * 0.125F);

            double d4 = Math.sin((double)(this.entityCreature.tickCount + this.entityCreature.getId()) * 0.5D) * 0.05D;
            double d5 = Math.cos((double)(this.entityCreature.yRot * 0.017453292F));
            double d6 = Math.sin((double)(this.entityCreature.yRot * 0.017453292F));
            double motionX = d4 * d5;
            double motionZ = d4 * d6;
            d4 = Math.sin((double)(this.entityCreature.tickCount + this.entityCreature.getId()) * 0.75D) * 0.05D;
            double motionY = d4 * (d6 + d5) * 0.25D;
            motionY += (double)this.entityCreature.getSpeed() * y * 0.125D;
            this.entityCreature.setDeltaMovement(this.entityCreature.getDeltaMovement().add(motionX, motionY, motionZ));

            LookController lookController = this.entityCreature.getLookControl();
            double d7 = this.entityCreature.position().x() + x / distance * 2.0D;
            double d8 = (double)this.entityCreature.getEyeHeight() + this.entityCreature.position().y() + y / distance;
            double d9 = this.entityCreature.position().z() + z / distance * 2.0D;
            double d10 = lookController.getWantedX();
            double d11 = lookController.getWantedY();
            double d12 = lookController.getWantedZ();

            if (!lookController.isHasWanted()) {
                d10 = d7;
                d11 = d8;
                d12 = d9;
            }

            this.entityCreature.getLookControl().setLookAt(d10 + (d7 - d10) * 0.125D, d11 + (d8 - d11) * 0.125D, d12 + (d9 - d12) * 0.125D, 10.0F, 40.0F);
        }
        else {
            this.entityCreature.setSpeed(0.0F);
        }
    }

    /** Used by flyers for swift, fast air movement. **/
    public void tickFlying() {
        if (this.operation == MovementController.Action.MOVE_TO) {
            double xDistance = this.wantedX - this.entityCreature.position().x();
            double yDistance = this.wantedY - this.entityCreature.position().y();
            double zDistance = this.wantedZ - this.entityCreature.position().z();
            double distance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;

            if (this.courseChangeCooldown-- <= 0) {
                this.courseChangeCooldown += this.entityCreature.getRandom().nextInt(5) + 2;
                distance = (double)MathHelper.sqrt(distance);
                if(distance >= 1D) {
                    this.entityCreature.setSpeed((float)this.entityCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                    double speed = (this.entityCreature.getSpeed() / 2.4D) * this.getSpeedModifier();
                    double motionX = xDistance / distance * speed;
                    double motionY = yDistance / distance * speed;
                    double motionZ = zDistance / distance * speed;
                    this.entityCreature.setDeltaMovement(this.entityCreature.getDeltaMovement().add(motionX, motionY, motionZ));
                }
                else {
                    this.operation = MovementController.Action.WAIT;
                }
            }
        }

        // Look At Target or Movement Direction:
        if (this.entityCreature.getTarget() != null) {
            LivingEntity entitylivingbase = this.entityCreature.getTarget();
            double distanceX = entitylivingbase.position().x() - this.entityCreature.position().x();
            double distanceZ = entitylivingbase.position().z() - this.entityCreature.position().z();
            this.entityCreature.yBodyRot = this.entityCreature.yRot = -((float)MathHelper.atan2(distanceX, distanceZ)) * (180F / (float)Math.PI);
        }
        else if(this.operation == MovementController.Action.MOVE_TO) {
            this.entityCreature.yBodyRot = this.entityCreature.yRot = -((float)MathHelper.atan2(this.entityCreature.getDeltaMovement().x(), this.entityCreature.getDeltaMovement().z())) * (180F / (float)Math.PI);
        }
    }
}
