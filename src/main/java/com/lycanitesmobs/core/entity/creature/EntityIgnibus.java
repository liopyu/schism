package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.RapidFireProjectileEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityIgnibus extends RideableCreatureEntity implements IGroupHeavy {
    protected boolean wantsToLand;
    protected boolean  isLanded;

    public EntityIgnibus(EntityType<? extends EntityIgnibus> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.isLavaCreature = true;
        this.flySoundSpeed = 20;
        this.hasAttackSound = false;
        
        this.setAttackCooldownMax(20);
        this.setupMob();

        this.maxUpStep = 1.0F;
        this.hitAreaWidthScale = 1.5F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setStaminaTime(100).setRange(20.0F).setMinChaseDistance(10.0F));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Land/Fly:
        if(!this.getCommandSenderWorld().isClientSide) {
            if(this.isLanded) {
                this.wantsToLand = false;
                if(this.hasPickupEntity() || this.getControllingPassenger() != null || this.isLeashed() || this.isInWater() || (!this.isTamed() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean())) {
                    this.leap(1.0D, 1.0D);
                    this.isLanded = false;
                }
                if(this.isTamed() && !this.isSitting()) {
                    this.isLanded = false;
                }
            }
            else {
                if(this.wantsToLand) {
                    if(this.isSafeToLand()) {
                        this.isLanded = true;
                    }
                }
                else {
                    if (!this.hasPickupEntity() && !this.hasAttackTarget() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                        this.wantsToLand = true;
                    }
                }
            }
            if(this.hasPickupEntity() || this.getControllingPassenger() != null || this.hasAttackTarget() || this.isInWater()) {
                this.wantsToLand = false;
            }
            else if(this.isTamed() && this.isSitting() && !this.isLeashed()) {
                this.wantsToLand = true;
            }
        }

        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
            for(int i = 0; i < 2; ++i) {
                this.getCommandSenderWorld().addParticle(ParticleTypes.SMOKE, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
                this.getCommandSenderWorld().addParticle(ParticleTypes.FLAME, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
            }
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        rider.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, (5 * 20) + 5, 1));
        super.riderEffects(rider);
    }

    @Override
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        if(this.wantsToLand || !this.isLanded) {
            BlockPos groundPos;
            for(groundPos = wanderPosition.below(); groundPos.getY() > 0 && this.getCommandSenderWorld().getBlockState(groundPos).getBlock() == Blocks.AIR; groundPos = groundPos.below()) {}
            if(this.getCommandSenderWorld().getBlockState(groundPos).getMaterial().isSolid()) {
                return groundPos.above();
            }
        }
        return super.getWanderPosition(wanderPosition);
    }


    @Override
    public double getFlightOffset() {
        if(!this.wantsToLand) {
            super.getFlightOffset();
        }
        return 0;
    }

    @Override
    public boolean isFlying() { return !this.isLanded; }

    @Override
    public boolean isStrongSwimmer() { return false; }

    @Override
    public void attackRanged(Entity target, float range) {
        // Type:
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("primeember");
        if(projectileInfo == null) {
            return;
        }

        for (int projectileX = -1; projectileX <= 1; projectileX++) {
            for (int projectileY = -1; projectileY <= 1; projectileY++) {
                Vector3d offset = this.getFacingPositionDouble(0, -6D + (0.5D * projectileY), 0, 4, this.yRot);
                offset.add(this.getFacingPositionDouble(0, 0, 0, 2D * projectileX, this.yRot + 90D));
                RapidFireProjectileEntity projectile = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
                this.fireProjectile(projectile, target, range, 0, offset, 0.6f, 3f, 4F);
            }
        }

        super.attackRanged(target, range);
    }

    @Override
    public boolean canBurn() { return false; }

    @Override
    public boolean canBreatheUnderlava() {
        return true;
    }

    @Override
    public float getFallResistance() {
        return 100;
    }

    @Override
    public float getDamageModifier(DamageSource damageSrc) {
        if(damageSrc.isFire())
            return 0F;
        else return super.getDamageModifier(damageSrc);
    }

    @Override
    public void mountAbility(Entity rider) {
        if(this.getCommandSenderWorld().isClientSide)
            return;

        if(this.abilityToggled)
            return;

        if(this.hasPickupEntity()) {
            this.dropPickupEntity();
            return;
        }

        if(this.getStamina() < this.getStaminaCost())
            return;

        if(rider instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("primeember");
            if(projectileInfo == null) {
                return;
            }

            // Type:
            List<RapidFireProjectileEntity> projectiles = new ArrayList<>();

            RapidFireProjectileEntity projectileEntry = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), player, 15, 3);
            projectiles.add(projectileEntry);

			RapidFireProjectileEntity projectileEntry2 = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
			projectileEntry2.offsetX += 1.0D;
			projectileEntry2.setProjectileScale(0.25f);
			projectiles.add(projectileEntry2);

			RapidFireProjectileEntity projectileEntry3 = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
			projectileEntry3.offsetX -= 1.0D;
			projectileEntry3.setProjectileScale(0.25f);
			projectiles.add(projectileEntry3);

			RapidFireProjectileEntity projectileEntry4 = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
			projectileEntry4.offsetZ += 1.0D;
			projectileEntry4.setProjectileScale(0.25f);
			projectiles.add(projectileEntry4);

			RapidFireProjectileEntity projectileEntry5 = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
			projectileEntry5.offsetZ -= 1.0D;
			projectileEntry5.setProjectileScale(0.25f);
			projectiles.add(projectileEntry5);

			RapidFireProjectileEntity projectileEntry6 = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
			projectileEntry6.offsetY += 1.0D;
			projectileEntry6.setProjectileScale(0.25f);
			projectiles.add(projectileEntry6);

			RapidFireProjectileEntity projectileEntry7 = new RapidFireProjectileEntity(ProjectileManager.getInstance().oldProjectileTypes.get(RapidFireProjectileEntity.class), projectileInfo, this.getCommandSenderWorld(), this, 15, 3);
			projectileEntry7.offsetY -= 10D;
			projectileEntry7.setProjectileScale(0.25f);
			projectiles.add(projectileEntry7);

            for(RapidFireProjectileEntity projectile : projectiles) {
                projectile.setProjectileScale(1f);

                // Y Offset:
                projectile.setPos(
                        projectile.position().x(),
                        projectile.position().y() - this.getDimensions(Pose.STANDING).height / 4,
                        projectile.position().z()
                );

                // Launch:
                this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.getCommandSenderWorld().addFreshEntity(projectile);
            }
            this.triggerAttackCooldown();
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 2;
    }

    public int getStaminaRecoveryWarmup() {
        return 2 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }

    @Override
    public float getBrightness() {
        if(isAttackOnCooldown())
            return 1.0F;
        else
            return super.getBrightness();
    }
}
