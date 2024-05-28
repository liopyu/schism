package com.lycanitesmobs.core.info.projectile;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.FileLoader;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.StreamLoader;
import com.lycanitesmobs.core.entity.*;
import com.lycanitesmobs.core.entity.projectile.*;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ProjectileManager extends JSONLoader {
	public static ProjectileManager INSTANCE;

	/** A map of all projectiles by name. **/
	public Map<String, ProjectileInfo> projectiles = new HashMap<>();

	/** A map of old projectile classes that are hardcoded instead of using json definitions that use the default item sprite renderer. **/
	public Map<String, Class<? extends Entity>> oldSpriteProjectiles = new HashMap<>();

	/** A map of old projectiles that use the obj model renderer. Newer json based projectiles provide their model class in their ProjectileInfo definition instead. **/
	public Map<String, Class<? extends Entity>> oldModelProjectiles = new HashMap<>();

	/** A map of old projectile classes to types for creating new instances. **/
	public Map<Class<? extends Entity>, EntityType<? extends BaseProjectileEntity>> oldProjectileTypes = new HashMap<>();

	/** A map of old projectile classes to simple names for translation, etc. **/
	public Map<Class<? extends Entity>, String> oldProjectileNames = new HashMap<>();

	/** Returns the main Projectile Manager instance or creates it and returns it. **/
	public static ProjectileManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new ProjectileManager();
		}
		return INSTANCE;
	}

	/**
	 * Called during startup and initially loads everything in this manager.
	 * @param modInfo The mod loading this manager.
	 */
	public void startup(ModInfo modInfo) {
		this.loadAllFromJSON(modInfo);
		for(ProjectileInfo projectileInfo : this.projectiles.values()) {
			projectileInfo.load();
		}
		this.loadOldProjectiles();
	}

	/** Loads all JSON Creature Types. Should be done before creatures are loaded so that they can find their type on load. **/
	public void loadAllFromJSON(ModInfo groupInfo) {
		this.loadAllJson(groupInfo, "Projectile", "projectiles", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
		LycanitesMobs.logDebug("Projectile", "Complete! " + this.projectiles.size() + " JSON Projectile Info Loaded In Total.");
	}

	@Override
	public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
		ProjectileInfo projectileInfo = new ProjectileInfo(modInfo);
		projectileInfo.loadFromJSON(json);
		if (projectileInfo.name == null) {
			LycanitesMobs.logWarning("", "[Projectile] Unable to load " + loadGroup + " json due to missing name.");
			return;
		}

		// Already Exists:
		if (this.projectiles.containsKey(projectileInfo.name)) {
			projectileInfo = this.projectiles.get(projectileInfo.name);
			projectileInfo.loadFromJSON(json);
		}

		this.projectiles.put(projectileInfo.name, projectileInfo);
		return;
	}

	/**
	 * Registers all creatures added to this creature manager, called from the registry event.
	 * @param event The entity register event.
	 */
	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
		LycanitesMobs.logDebug("Projectile", "Forge registering all " + this.projectiles.size() + " projectiles...");

		// New JSON Projectiles:
		for(ProjectileInfo projectileInfo : this.projectiles.values()) {
			event.getRegistry().register(projectileInfo.getEntityType());
		}

		// Old Sprite Projectiles:
		for(String entityName : this.oldSpriteProjectiles.keySet()) {
			event.getRegistry().register(this.createEntityType(entityName, this.oldSpriteProjectiles.get(entityName)));
		}

		// Old Model Projectiles:
		for(String entityName : this.oldModelProjectiles.keySet()) {
			event.getRegistry().register(this.createEntityType(entityName, this.oldModelProjectiles.get(entityName)));
		}
	}

	/**
	 * Creates an Entity Type for older projectiles.
	 * @param entityName The projectile name to register with.
	 * @param entityClass The projectile entity class to register.
	 * @return The projectile's Entity Type.
	 */
	public EntityType createEntityType(String entityName, Class<? extends Entity> entityClass) {
		EntityType.Builder entityTypeBuilder = EntityType.Builder.of(EntityFactory.getInstance(), EntityClassification.MISC);
		entityTypeBuilder.setTrackingRange(40);
		entityTypeBuilder.setUpdateInterval(3);
		entityTypeBuilder.setShouldReceiveVelocityUpdates(true);
		entityTypeBuilder.sized(0.25F, 0.25F);
		EntityType entityType = entityTypeBuilder.build(entityName);
		entityType.setRegistryName(LycanitesMobs.MODID, entityName);
		try {
			EntityFactory.getInstance().addEntityType(entityType, entityClass.getConstructor(EntityType.class, World.class), entityName.toLowerCase());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		LycanitesMobs.logDebug("Projectile", "Added (Projectile) Entity Type: " + entityName + " Type: " + entityType);
		this.oldProjectileTypes.put(entityClass, entityType);
		return entityType;
	}

	/**
	 * Gets a projectile by name.
	 * @param projectileName The name of the projectile to get.
	 * @return The Projectile Info.
	 */
	@Nullable
	public ProjectileInfo getProjectile(String projectileName) {
		if(!this.projectiles.containsKey(projectileName)) {
			return null;
		}
		return this.projectiles.get(projectileName);
	}

	/**
	 * Gets a Projectile Entity Type by name.
	 * @param projectileName The name of the projectile to get.
	 * @return The Entity Type or null.
	 */
	@Nullable
	public EntityType<? extends BaseProjectileEntity> getEntityType(String projectileName) {
		ProjectileInfo projectileInfo = this.getProjectile(projectileName);
		if(projectileInfo == null)
			return null;
		return projectileInfo.getEntityType();
	}

	/** Called during early start up, loads all items. **/
	public void loadOldProjectiles() {
		this.addOldProjectile("summoningportal", PortalEntity.class);
		this.addOldProjectile("rapidfire", RapidFireProjectileEntity.class);
		this.addOldProjectile("laserend", LaserEndProjectileEntity.class);

		this.addOldProjectile("shadowfirebarrier", EntityShadowfireBarrier.class, false);
		this.addOldProjectile("hellfirewall", EntityHellfireWall.class, false);
		this.addOldProjectile("hellfireorb", EntityHellfireOrb.class, false);
		this.addOldProjectile("hellfirewave", EntityHellfireWave.class, false);
		this.addOldProjectile("hellfirewavepart", EntityHellfireWavePart.class, false);
		this.addOldProjectile("hellfirebarrier", EntityHellfireBarrier.class, false);
		this.addOldProjectile("hellfirebarrierpart", EntityHellfireBarrierPart.class, false);
		this.addOldProjectile("devilgatling", EntityDevilGatling.class, false);
		this.addOldProjectile("hellshield", EntityHellShield.class, false);
		this.addOldProjectile("helllaser", EntityHellLaser.class, false);
		this.addOldProjectile("helllaserend", EntityHellLaserEnd.class, false);
	}

	public void addOldProjectile(String name, Class<? extends BaseProjectileEntity> entityClass) {
		this.oldProjectileNames.put(entityClass, name);
		if(ModelProjectileEntity.class.isAssignableFrom(entityClass)) {
			this.oldModelProjectiles.put(name, entityClass);
			return;
		}
		this.oldSpriteProjectiles.put(name, entityClass);
	}
	
	public void addOldProjectile(String name, Class<? extends BaseProjectileEntity> entityClass, boolean impactSound) {
		ModInfo modInfo = LycanitesMobs.modInfo;
		ObjectManager.addSound(name, modInfo, "projectile." + name);
		if(impactSound) {
			ObjectManager.addSound(name + "_impact", modInfo, "projectile." + name + ".impact");
		}
		this.addOldProjectile(name, entityClass);
	}

	public BaseProjectileEntity createOldProjectile(Class<? extends BaseProjectileEntity> projectileClass, World world, LivingEntity entity) {
		try {
			return projectileClass.getConstructor(EntityType.class, World.class, LivingEntity.class).newInstance(this.oldProjectileTypes.get(projectileClass), world, entity);
		} catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
			//throw new RuntimeException(e);
			return null;
		}
	}

	public BaseProjectileEntity createOldProjectile(Class<? extends BaseProjectileEntity> projectileClass, World world, double x, double y, double z) {
		try {
			return projectileClass.getConstructor(EntityType.class, World.class, double.class, double.class, double.class).newInstance(this.oldProjectileTypes.get(projectileClass), world, x, y, z);
		} catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
			//throw new RuntimeException(e);
			return null;
		}
	}
}
