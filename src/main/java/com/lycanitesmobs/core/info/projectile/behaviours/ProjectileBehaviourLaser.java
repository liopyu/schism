package com.lycanitesmobs.core.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.HashSet;

public class ProjectileBehaviourLaser extends ProjectileBehaviour {
	/** The aiming speed of the laser. **/
	public double speed = 1;
	
	/** The width of the laser. **/
	public float width = 1;

	/** The total range of the laser. **/
	public double range = 16;

	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("speed"))
			this.speed = json.get("speed").getAsDouble();
		if(json.has("width"))
			this.width = json.get("width").getAsFloat();
		if(json.has("range"))
			this.range = json.get("range").getAsDouble();
	}

	@Override
	public void onProjectileUpdate(BaseProjectileEntity projectile) {
		if(!(projectile instanceof CustomProjectileEntity)) {
			return;
		}
		projectile.movement = false;
		((CustomProjectileEntity)projectile).syncThrower();
		((CustomProjectileEntity)projectile).laserWidth = this.width;

		// Follow Thrower/Parent Projectile:
		if(projectile.getOwner() != null) {
			Entity entityToFollow = projectile.getOwner();
			if(((CustomProjectileEntity)projectile).getParent() != null) {
				entityToFollow = ((CustomProjectileEntity)projectile).getParent();
			}
			double xPos = entityToFollow.position().x();
			double yPos = entityToFollow.position().y();
			if (!(entityToFollow instanceof BaseProjectileEntity)) {
				yPos += entityToFollow.getDimensions(entityToFollow.getPose()).height * 0.5D;
			}
			double zPos = entityToFollow.position().z();
			/*if(entityToFollow instanceof BaseCreatureEntity) {
				BaseCreatureEntity creatureToFollow = (BaseCreatureEntity)entityToFollow;
				xPos = creatureToFollow.getFacingPosition(creatureToFollow, 0, creatureToFollow.rotationYaw + 90F).getX();
				zPos = creatureToFollow.getFacingPosition(creatureToFollow, 0, creatureToFollow.rotationYaw).getZ();
			}*/
			projectile.setPos(xPos, yPos, zPos);
			projectile.setDeltaMovement(entityToFollow.getDeltaMovement());
		}

		// Update Laser End:
		this.updateEnd(projectile);
	}

	/**
	 * Updates the end of the laser.
	 * @param projectile The projectile firing its laser!
	 * @return The laser end used for stopping the laser at.
	 */
	public void updateEnd(BaseProjectileEntity projectile) {
		// Laser Aiming:
		double targetX = projectile.position().x();
		double targetY = projectile.position().y();
		double targetZ = projectile.position().z();

		// Entity Laser Aiming:
		boolean lockedLaser = false;
		if(((CustomProjectileEntity)projectile).getParent() != null) {
			double[] target = projectile.getFacingPosition(projectile, this.range, ((CustomProjectileEntity)projectile).laserAngle);
			targetX = target[0] + ((MathHelper.cos((projectile.updateTick + ((CustomProjectileEntity)projectile).laserAngle) * 0.25F) * 1.0F) - 0.5F);
			targetY = target[1] + (((MathHelper.cos((projectile.updateTick + ((CustomProjectileEntity)projectile).laserAngle) * 0.25F) * 1.0F) - 0.5F) * 10);
			targetZ = target[2] + ((MathHelper.cos((projectile.updateTick + ((CustomProjectileEntity)projectile).laserAngle) * 0.25F) * 1.0F) - 0.5F);
		}
		else if(projectile.getOwner() != null) {
			if(projectile.getOwner() instanceof BaseCreatureEntity && ((BaseCreatureEntity)projectile.getOwner()).getTarget() != null) {
				((CustomProjectileEntity)projectile).setTarget(((BaseCreatureEntity) projectile.getOwner()).getTarget());
			}
			Entity attackTarget = ((CustomProjectileEntity)projectile).getTarget();
			if(attackTarget != null) {
				targetX = attackTarget.position().x();
				targetY = attackTarget.position().y() + (attackTarget.getDimensions(attackTarget.getPose()).height / 2);
				targetZ = attackTarget.position().z();
				lockedLaser = true;
			}
			else {
				Vector3d lookDirection = projectile.getOwner().getLookAngle();
				targetX = projectile.getOwner().position().x() + (lookDirection.x * this.range);
				targetY = projectile.getOwner().position().y() + projectile.getOwner().getEyeHeight() + (lookDirection.y * this.range);
				targetZ = projectile.getOwner().position().z() + (lookDirection.z * this.range);
			}
		}

		// Raytracing:
		HashSet<Entity> excludedEntities = new HashSet<>();
		excludedEntities.add(projectile);
		if(projectile.getOwner() != null) {
			excludedEntities.add(projectile.getOwner());
			if(projectile.getOwner().getControllingPassenger() != null) {
				excludedEntities.add(projectile.getOwner().getControllingPassenger());
			}
		}
		RayTraceResult rayTraceResult = Utilities.raytrace(projectile.getCommandSenderWorld(), projectile.position().x(), projectile.position().y(), projectile.position().z(), targetX, targetY, targetZ, this.width, projectile, excludedEntities);

		// Update Laser End Position:
		if(rayTraceResult != null && !lockedLaser) {
			targetX = rayTraceResult.getLocation().x;
			targetY = rayTraceResult.getLocation().y;
			targetZ = rayTraceResult.getLocation().z;
			if(rayTraceResult instanceof EntityRayTraceResult) {
				Entity entityHit = ((EntityRayTraceResult)rayTraceResult).getEntity();
				if (entityHit != null) {
					targetY += entityHit.getDimensions(entityHit.getPose()).height / 2;
				}
			}
		}

		((CustomProjectileEntity)projectile).setLaserEnd(new Vector3d(targetX, targetY, targetZ));

		// Laser Damage:
		if(projectile.updateTick % 10 == 0 && projectile.isAlive() && rayTraceResult instanceof EntityRayTraceResult) {
			EntityRayTraceResult entityRayTraceResult = (EntityRayTraceResult)rayTraceResult;
			if(((CustomProjectileEntity)projectile).getLaserEnd().distanceTo(entityRayTraceResult.getEntity().position()) <= (this.width * 10)) {
				boolean doDamage = true;
				if (entityRayTraceResult.getEntity() instanceof LivingEntity) {
					doDamage = projectile.canDamage((LivingEntity) entityRayTraceResult.getEntity());
				}
				if (doDamage) {
					this.updateDamage(projectile, entityRayTraceResult.getEntity());
				}
			}
		}

		if (projectile.projectileLife % 2 == 0) {
			projectile.playSound(projectile.getBeamSound(), 1.0F, 1.0F / (projectile.getCommandSenderWorld().getRandom().nextFloat() * 0.4F + 0.8F));
		}
	}


	public boolean updateDamage(BaseProjectileEntity projectile, Entity target) {
		boolean attackSuccess;
		float damage = projectile.getDamage(target);
		float damageInit = damage;

		// Prevent Knockback:
		double targetKnockbackResistance = 0;
		if(projectile.knockbackChance < 1) {
			if(projectile.knockbackChance <= 0 || projectile.getCommandSenderWorld().getRandom().nextDouble() <= projectile.knockbackChance) {
				if(target instanceof LivingEntity) {
					targetKnockbackResistance = ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
					((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
				}
			}
		}

		// Deal Damage:
		if(projectile.getOwner() instanceof BaseCreatureEntity) {
			BaseCreatureEntity creatureThrower = (BaseCreatureEntity)projectile.getOwner();
			attackSuccess = creatureThrower.doRangedDamage(target, projectile, damage, projectile.isBlockedByEntity(target));
		}
		else {
			double pierceDamage = 1;
			if(damage <= pierceDamage)
				attackSuccess = target.hurt(DamageSource.thrown(projectile, projectile.getOwner()).bypassArmor().bypassMagic(), damage);
			else {
				int hurtResistantTimeBefore = target.invulnerableTime;
				target.hurt(DamageSource.thrown(projectile, projectile.getOwner()).bypassArmor().bypassMagic(), (float)pierceDamage);
				target.invulnerableTime = hurtResistantTimeBefore;
				damage -= pierceDamage;
				attackSuccess = target.hurt(DamageSource.thrown(projectile, projectile.getOwner()), damage);
			}
		}

		if(target instanceof LivingEntity) {
			projectile.onDamage((LivingEntity) target, damageInit, attackSuccess);
		}

		// Restore Knockback:
		if(projectile.knockbackChance < 1) {
			if(projectile.knockbackChance <= 0 || projectile.getCommandSenderWorld().getRandom().nextDouble() <= projectile.knockbackChance) {
				if(target instanceof LivingEntity)
					((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
			}
		}

		return attackSuccess;
	}

	@Override
	public void onProjectileImpact(BaseProjectileEntity projectile, World world, BlockPos pos) {}
}
