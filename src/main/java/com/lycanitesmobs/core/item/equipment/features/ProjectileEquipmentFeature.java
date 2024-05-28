package com.lycanitesmobs.core.item.equipment.features;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ProjectileEquipmentFeature extends EquipmentFeature {
	/** The name of the projectile to spawn. **/
	public String projectileName;

	/** How this feature spawns projectiles. Can be: 'hit' (when damaging an entity), 'primary' (left click) or 'secondary' (right click). **/
	public String projectileTrigger = "secondary";

	/** The pattern to fire projectiles in. Can be 'simple', 'spread' or 'ring'. **/
	public String projectilePattern = "simple";

	/** The chance of firing a projectile for the hit trigger. **/
	public double hitChance = 0.05;

	/** The cooldown (in ticks) for primary and secondary triggers. **/
	public int cooldown = 2;

	/** How many projectiles to fire per round. **/
	public int count = 1;

	/** The x spread for the spread projectile pattern. **/
	public double spreadX = 0;

	/** The y spread for the spread projectile pattern. **/
	public double spreadY = 0;

	/** The range in degrees for the ring pattern. **/
	public double ringRange = 0;

	/** Additional damage added to the projectile. **/
	public int bonusDamage = 0;

	/** Stores a channeled projectile currently in use by this feature, used for lasers. **/
	private BaseProjectileEntity channeledProjectile;


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		this.projectileName = json.get("projectileName").getAsString();

		if(json.has("projectileTrigger"))
			this.projectileTrigger = json.get("projectileTrigger").getAsString();

		if(json.has("projectilePattern"))
			this.projectilePattern = json.get("projectilePattern").getAsString();

		if(json.has("hitChance"))
			this.hitChance = json.get("hitChance").getAsDouble();

		if(json.has("cooldown"))
			this.cooldown = json.get("cooldown").getAsInt();

		if(json.has("count"))
			this.count = json.get("count").getAsInt();

		if(json.has("spreadX"))
			this.spreadX = json.get("spreadX").getAsDouble();

		if(json.has("spreadY"))
			this.spreadY = json.get("spreadY").getAsDouble();

		if(json.has("ringRange"))
			this.ringRange = json.get("ringRange").getAsDouble();

		if(json.has("bonusDamage"))
			this.bonusDamage = json.get("bonusDamage").getAsInt();
	}

	@Override
	public boolean isActive(ItemStack itemStack, int level) {
		if(!super.isActive(itemStack, level)) {
			return false;
		}
		return ProjectileManager.getInstance().getProjectile(this.projectileName) != null;
	}

	@Override
	public ITextComponent getDescription(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}

		ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
		TextComponent description = (TextComponent) new TranslationTextComponent("equipment.feature." + this.featureType).append(" ")
				.append(projectileInfo.getTitle());

		if(this.bonusDamage != 0) {
			description.append(" +" + this.bonusDamage);
		}

		if(!"simple".equals(this.projectilePattern)) {
			description.append(" ")
					.append(new TranslationTextComponent("equipment.feature.projectile.pattern." + this.projectilePattern));
		}

		description.append(" ")
				.append( new TranslationTextComponent("equipment.feature.projectile.trigger." + this.projectileTrigger));
		if("hit".equals(this.projectileTrigger)) {
			description.append(" " + String.format("%.0f", this.hitChance * 100) + "%");
		}
		else {
			description.append(" " + String.format("%.1f", (float)this.cooldown / 20) + "s");
		}

		return description;
	}

	@Override
	public ITextComponent getSummary(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
		TextComponent summary = projectileInfo.getTitle();
		if(this.bonusDamage != 0) {
			summary.append(" +" + this.bonusDamage);
		}
		return summary;
	}

	/**
	 * Called when a player left clicks to use their equipment.
	 * @param world The world the player is in.
	 * @param shooter The player using the equipment.
	 * @param hand The hand the player is holding the equipment in.
	 */
	public boolean onUsePrimary(World world, PlayerEntity shooter, Hand hand) {
		if(!"primary".equalsIgnoreCase(this.projectileTrigger)) {
			return false;
		}
		ExtendedEntity shooterExt = ExtendedEntity.getForEntity(shooter);
		if(shooterExt == null) {
			return false;
		}
		if(shooterExt.getProjectileCooldown(1, this.projectileName) > 0) {
			return false;
		}
		shooterExt.setProjectileCooldown(1, this.projectileName, this.cooldown);
		this.fireProjectile(shooter);
		return true;
	}

	/**
	 * Called when a player right click begins to use their equipment.
	 * @param world The world the player is in.
	 * @param shooter The player using the equipment.
	 * @param hand The hand the player is holding the equipment in.
	 * @return True so that the item becomes active.
	 */
	public boolean onUseSecondary(World world, PlayerEntity shooter, Hand hand) {
		return "secondary".equalsIgnoreCase(this.projectileTrigger);
	}

	/**
	 * Called when an entity using their equipment.
	 * @param shooter The entity using the equipment.
	 * @param count How long (in ticks) the equipment has been used for.
	 */
	public boolean onHoldSecondary(LivingEntity shooter, int count) {
		if(!"secondary".equalsIgnoreCase(this.projectileTrigger)) {
			return false;
		}
		ExtendedEntity shooterExt = ExtendedEntity.getForEntity(shooter);
		if(shooterExt == null) {
			return false;
		}
		if(shooterExt.getProjectileCooldown(2, this.projectileName) > 0) {
			return false;
		}
		shooterExt.setProjectileCooldown(2, this.projectileName, this.cooldown);
		this.fireProjectile(shooter);
		return true;
	}

	/**
	 * Called when an entity is hit by equipment with this feature.
	 * @param itemStack The ItemStack being hit with.
	 * @param target The target entity being hit.
	 * @param attacker The entity using this item to hit.
	 */
	public boolean onHitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
		if(target == null || attacker == null || attacker.getCommandSenderWorld().isClientSide || attacker.isShiftKeyDown() || !"hit".equals(this.projectileTrigger)) { // isSneaking()
			return false;
		}

		// Fire Projectile:
		if(attacker.getRandom().nextDouble() <= this.hitChance) {
			this.fireProjectile(attacker);
			return true;
		}

		return false;
	}

	/**
	 * Fires a projectile from this feature.
	 * @param shooter The entity firing the projectile.
	 */
	public void fireProjectile(LivingEntity shooter) {
		if(shooter == null || shooter.getCommandSenderWorld().isClientSide|| this.count <= 0) {
			return;
		}

		// Projectile Channeling:
		if(this.channeledProjectile != null) {
			if(!this.channeledProjectile.isAlive()) {
				this.channeledProjectile = null;
			}
			else {
				this.channeledProjectile.projectileLife = 20;
				return;
			}
		}

		World world = shooter.getCommandSenderWorld();
		BaseProjectileEntity mainProjectile = null;
		Vector3d firePos = new Vector3d(shooter.position().x(), shooter.position().y() + (shooter.getDimensions(Pose.STANDING).height * 0.65), shooter.position().z());
		double offsetX = 0;

		// Patterns:
		if("spread".equals(this.projectilePattern)) {
			for(int i = 0; i < this.count; i++) {
				double yaw = shooter.yRot + (this.spreadX * shooter.getRandom().nextDouble()) - (this.spreadX / 2);
				double pitch = shooter.xRot + (this.spreadY * shooter.getRandom().nextDouble()) - (this.spreadY / 2);
				ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
				BaseProjectileEntity projectile = projectileInfo.createProjectile(world, shooter);
				projectile.setPos(firePos.x, firePos.y, firePos.z);
				projectile.shootFromRotation(shooter, (float)pitch, (float)yaw - (float)offsetX, 0, (float)projectileInfo.velocity, 0);
				projectile.setOwner(shooter);
				projectile.setBonusDamage(this.bonusDamage);
				world.addFreshEntity(projectile);
				mainProjectile = projectile;
			}
		}
		else if("ring".equals(this.projectilePattern)) {
			double angle = this.ringRange / this.count;
			for(int i = 0; i < this.count; i++) {
				double yaw = shooter.yRot + (angle * i) - (this.ringRange / 2);
				ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
				BaseProjectileEntity projectile = projectileInfo.createProjectile(world, shooter);
				projectile.setPos(firePos.x, firePos.y, firePos.z);
				projectile.setBonusDamage(this.bonusDamage);
				world.addFreshEntity(projectile);
				projectile.shootFromRotation(shooter, shooter.xRot, (float)yaw - (float)offsetX, 0, (float)projectileInfo.velocity, 0);
				projectile.setOwner(shooter);
				mainProjectile = projectile;
			}
		}
		else {
			ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
			mainProjectile = projectileInfo.createProjectile(world, shooter);
			mainProjectile.setPos(firePos.x, firePos.y, firePos.z);
			mainProjectile.shootFromRotation(shooter, shooter.xRot, shooter.yRot - (float)offsetX, 0, (float)projectileInfo.velocity, 0);
			mainProjectile.setOwner(shooter);
			mainProjectile.setBonusDamage(this.bonusDamage);
			world.addFreshEntity(mainProjectile);
		}

		// Channeling:
		if(this.count == 1 && mainProjectile instanceof CustomProjectileEntity) {
			CustomProjectileEntity customProjectileEntity = (CustomProjectileEntity)mainProjectile;
			if(customProjectileEntity.shouldChannel()) {
				this.channeledProjectile = customProjectileEntity;
			}
		}

		if(shooter instanceof PlayerEntity && mainProjectile != null) {
			world.playSound(null, shooter.blockPosition(), mainProjectile.getLaunchSound(), SoundCategory.NEUTRAL, 0.5F, 0.4F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
		}
	}

	/** Returns the Vec3f in front or behind the provided entity's position coords with the given distance and angle (in degrees), use a negative distance for behind. **/
	public Vector3d getFacingPosition(LivingEntity entity, double distance, double angle) {
		angle = Math.toRadians(angle);
		double xAmount = -Math.sin(angle);
		double zAmount = Math.cos(angle);
		return new Vector3d(entity.position().x() + (distance * xAmount), entity.position().y(), entity.position().z() + (distance * zAmount));
	}
}
