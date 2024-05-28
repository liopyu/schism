package com.lycanitesmobs.core.info.projectile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.ModelManager;
import com.lycanitesmobs.core.dispenser.BaseProjectileDispenseBehaviour;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileModelEntity;
import com.lycanitesmobs.core.entity.EntityFactory;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.ElementManager;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.projectile.behaviours.ProjectileBehaviour;
import com.lycanitesmobs.core.item.ChargeItem;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProjectileInfo {

	// Core Info:
	/** The name of this projectile. Lowercase, no space, used for language entries and for generating the projectile id, etc. Required. **/
	protected String name;

	/** Whether this projectile is enabled or not. **/
	public boolean enabled = true;

	/** The entity class used by this projectile. Defaults to EntityProjectileCustom but can be changed to special classes for unique behaviour, etc. **/
	@Nonnull
	public Class<? extends BaseProjectileEntity> entityClass = CustomProjectileEntity.class;

	/** The constructor used by this projectile to create entity instances. **/
	public Constructor<? extends BaseProjectileEntity> entityConstructor;

	/** The class of the model this subspecies should use, loaded client side only. **/
	public String modelClassName;

	/** The group that this projectile belongs to. **/
	public ModInfo modInfo;

	/** The entity type used to store base attributes of this projectile. **/
	protected EntityType<? extends BaseProjectileEntity> entityType;

	// Item:
	/** The item used to fire this projectile from a dispenser and on use. **/
	public Item chargeItem;
	/** The name of the charge item for this projectile. Can be automatically generated using the name of this projectile or overridden. **/
	public String chargeItemName;
	/** If true, no charge item is generated for this projectile. **/
	public boolean noChargeItem = false;
	/** The dispenser behaviour used by this projectile. If null, this item cannot be fired from a dispenser. **/
	public BaseProjectileDispenseBehaviour dispenserBehaviour;

	// Stats:
	/** The width of the projectile. **/
	public float width = 0.75F;
	/** The height of the projectile. **/
	public float height = 0.75F;
	/** The scale of the projectile. **/
	public float scale = 0.5F;
	/** How many ticks the projectile is active for. **/
	public int lifetime = 200;
	/** The base amount of damage that this projectile deals. **/
	public int damage = 1;
	/** The base amount of damage caused by this projectile that can ignore armor and similar defenses. **/
	public int pierce = 1;
	/** The chance of this projectile knocking back an entity hit by it. **/
	public double knockbackChance = 0;
	/** How long (in seconds) any element debuffs applied by this projectile last for. **/
	public int effectDuration = 1;
	/** How strong any element debuffs applied by this projectile are. **/
	public int effectAmplifier = 1;
	/** The default velocity that this projectile is launched at. **/
	public double velocity = 1.1D;
	/** How much gravity affects this projectile. **/
	public double weight = 1.0D;
	/** How fast the projectile sprite spins. **/
	public float rollSpeed = 0;

	// Elements:
	/** The Elements of this projectile, affects buffs and debuffs amongst other things. **/
	public List<ElementInfo> elements = new ArrayList<>();

	// Behaviours:
	/** A list of behaviours that this projectile has. **/
	public List<ProjectileBehaviour> behaviours = new ArrayList<>();

	// Flags:
	/** If true, this projectile wont be destroyed when hitting the water or underwater. **/
	public boolean waterproof = false;
	/** If true, this projectile wont be destroyed when hitting the lava or submerged in lava. **/
	public boolean lavaproof = false;
	/** If true, this projectile will destroy long grass and similar blocks. **/
	public boolean cutGrass = false;
	/** If true, this projectile will cut through entities hit. **/
	public boolean ripper = false;
	/** If true, this projectile will cut through blocks hit. **/
	public boolean pierceBlocks = false;
	/** If true, this projectile will play a sound on impact. **/
	public boolean impactSound = false;

	/** How many vertical animation frames the sprite has, set to 1 for no animation. **/
	public int animationFrames = 1;
	/** If true, this projectile glow in the dark. **/
	public boolean glow = false;
	/** If true, this projectile will have a burning effect on it. **/
	public boolean burningEffect = false;
	/** How many particles to create per tick. **/
	public int particleCount = 0;
	/** The vanilla particle to play from this projectile. **/
	public String particleId = null;
	/** The vanilla particle to play from this projectile when in water. **/
	public String waterParticleId = null;
	/** The vanilla particle to play from this projectile. **/
	public BasicParticleType particleType = null;
	/** The vanilla particle to play from this projectile when in water. **/
	public BasicParticleType waterParticleType = null;


	/**
	 * Constructor
	 * @param modInfo The group that this projectile definition will belong to.
	 */
	public ProjectileInfo(ModInfo modInfo) {
		this.modInfo = modInfo;
	}


	/** Loads this projectile from a JSON object. **/
	public void loadFromJSON(JsonObject json) {
		this.name = json.get("name").getAsString();

		if(json.has("chargeItemName")) {
			this.chargeItemName = json.get("chargeItemName").getAsString();
		}
		else {
			this.chargeItemName = this.name + "charge";
		}

		if(json.has("noChargeItem")) {
			this.noChargeItem = json.get("noChargeItem").getAsBoolean();
		}

		try {
			if(json.has("entityClass")) {
				this.entityClass = (Class<? extends BaseProjectileEntity>) Class.forName(json.get("entityClass").getAsString());
			}
			this.entityConstructor = this.entityClass.getConstructor(EntityType.class, World.class);
		} catch (Exception e) {
			LycanitesMobs.logError("[Projectile] Unable to find the Java Entity Class: " + json.get("entityClass").getAsString() + " for " + this.getName());
			throw new RuntimeException(e);
		}

		if(json.has("modelClass")) {
			this.modelClassName = json.get("modelClass").getAsString();
		}

		// Size:
		if(json.has("width"))
			this.width = json.get("width").getAsFloat();
		if(json.has("height"))
			this.height = json.get("height").getAsFloat();
		if(json.has("scale"))
			this.scale = json.get("scale").getAsFloat();

		// Stats:
		if(json.has("damage"))
			this.damage = json.get("damage").getAsInt();
		if(json.has("pierce"))
			this.pierce = json.get("pierce").getAsInt();
		if(json.has("knockbackChance"))
			this.knockbackChance = json.get("knockbackChance").getAsDouble();
		if(json.has("effectDuration"))
			this.effectDuration = json.get("effectDuration").getAsInt();
		if(json.has("effectAmplifier"))
			this.effectAmplifier = json.get("effectAmplifier").getAsInt();
		if(json.has("velocity"))
			this.velocity = json.get("velocity").getAsDouble();
		if(json.has("weight"))
			this.weight = json.get("weight").getAsDouble();
		if(json.has("lifetime"))
			this.lifetime = json.get("lifetime").getAsInt();

		// Visual:
		if(json.has("rollSpeed"))
			this.rollSpeed = json.get("rollSpeed").getAsFloat();

		// Elements:
		List<String> elementNames = new ArrayList<>();
		if(json.has("elements")) {
			elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
		}
		this.elements.clear();
		for(String elementName : elementNames) {
			ElementInfo element = ElementManager.getInstance().getElement(elementName);
			if (element == null) {
				throw new RuntimeException("[Projectile] Unable to initialise Projectile Info for " + this.getName() + " as the element " + elementName + " cannot be found.");
			}
			this.elements.add(element);
		}

		// Behaviours:
		if(json.has("behaviours")) {
			JsonArray jsonArray = json.get("behaviours").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject behaviorJson = jsonIterator.next().getAsJsonObject();
				ProjectileBehaviour projectileBehaviour = ProjectileBehaviour.createFromJSON(behaviorJson);
				if(projectileBehaviour != null) {
					this.behaviours.add(projectileBehaviour);
				}
				else {
					LycanitesMobs.logWarning("", "Unable to load Projectile Behaviour: " + behaviorJson.get("type").getAsString());
				}
			}
		}

		// Flags:
		if(json.has("waterproof"))
			this.waterproof = json.get("waterproof").getAsBoolean();
		if(json.has("lavaproof"))
			this.lavaproof = json.get("lavaproof").getAsBoolean();
		if(json.has("cutGrass"))
			this.cutGrass = json.get("cutGrass").getAsBoolean();
		if(json.has("ripper"))
			this.ripper = json.get("ripper").getAsBoolean();
		if(json.has("pierceBlocks"))
			this.pierceBlocks = json.get("pierceBlocks").getAsBoolean();

		if(json.has("animationFrames"))
			this.animationFrames = json.get("animationFrames").getAsInt();
		if(json.has("impactSound"))
			this.impactSound = json.get("impactSound").getAsBoolean();
		if(json.has("glow"))
			this.glow = json.get("glow").getAsBoolean();
		if(json.has("burningEffect"))
			this.burningEffect = json.get("burningEffect").getAsBoolean();
		if(json.has("particleCount"))
			this.particleCount = json.get("particleCount").getAsInt();
		if(json.has("particleId")) {
			this.particleId = json.get("particleId").getAsString();
		}
		if(json.has("waterParticleId")) {
			this.waterParticleId = json.get("waterParticleId").getAsString();
		}
	}

	/**
	 * Loads this projectile (should only be called during startup), generates charge items, etc.
	 */
	public void load() {
		// Charge Item:
		if(!this.noChargeItem) {
			this.chargeItem = ObjectManager.getItem(this.chargeItemName);
			if (this.chargeItem == null) {
				Item.Properties properties = new Item.Properties();
				properties.tab(ItemManager.getInstance().chargesGroup);
				this.chargeItem = new ChargeItem(properties, this);
				ObjectManager.addItem(this.chargeItemName, this.chargeItem);
			}

			// Dispenser:
			this.dispenserBehaviour = new BaseProjectileDispenseBehaviour(this);
			DispenserBlock.registerBehavior(this.chargeItem, this.dispenserBehaviour);
		}

		// Sounds:
		ObjectManager.addSound(name, modInfo, "projectile." + name);
		if(this.impactSound) {
			ObjectManager.addSound(name + "_impact", modInfo, "projectile." + name + ".impact");
		}
	}


	/**
	 * Returns the name of this projectile, this is the unformatted lowercase name. Ex: chaosorb
	 * @return Projectile name.
	 */
	public String getName() {
		return this.name.toLowerCase();
	}

	/**
	 * Returns the registry id of this projectile. Ex: elementalmobs:chaosorb
	 * @return Projectile registry entity id.
	 */
	public String getEntityId() {
		return this.modInfo.modid + ":" + this.getName();
	}

	public BasicParticleType getParticleType() {
		if (this.particleCount <= 0 || this.particleId == null) {
			return null;
		}
		if (this.particleType == null) {
			ParticleType<?> particleType = RegistryManager.ACTIVE.getRegistry(Registry.PARTICLE_TYPE_REGISTRY).getValue(new ResourceLocation(this.particleId));
			if (particleType instanceof BasicParticleType) {
				this.particleType = (BasicParticleType)particleType;
			}
		}
		return this.particleType;
	}

	public BasicParticleType getWaterParticleType() {
		if (this.particleCount <= 0 || this.waterParticleId == null) {
			return null;
		}
		if (this.waterParticleType == null) {
			ParticleType<?> particleType = RegistryManager.ACTIVE.getRegistry(Registry.PARTICLE_TYPE_REGISTRY).getValue(new ResourceLocation(this.waterParticleId));
			if(particleType instanceof BasicParticleType) {
				this.waterParticleType = (BasicParticleType)particleType;
			}
		}
		return this.waterParticleType;
	}


	/**
	 * Returns the entity type of this projectile.
	 * @return Projectiles's entity type.
	 */
	@Nonnull
	public EntityType<? extends BaseProjectileEntity> getEntityType() {
		if(this.entityType == null) {
			EntityType.Builder entityTypeBuilder = EntityType.Builder.of(EntityFactory.getInstance(), EntityClassification.MISC);
			//entityTypeBuilder.setCustomClientFactory(EntityFactory.getInstance().createOnClientFunction);
			entityTypeBuilder.setTrackingRange(40);
			entityTypeBuilder.setUpdateInterval(3);
			entityTypeBuilder.setShouldReceiveVelocityUpdates(true);
			entityTypeBuilder.sized(this.width, this.height);
			this.entityType = entityTypeBuilder.build(this.getName());
			this.entityType.setRegistryName(this.modInfo.modid, this.getName());
			EntityFactory.getInstance().addEntityType(this.entityType, this.entityConstructor, this.getName());
		}
		return this.entityType;
	}

	/**
	 * Returns the resource location for this projectile.
	 * @return Projectile resource location.
	 */
	public ResourceLocation getResourceLocation() {
		return new ResourceLocation(this.modInfo.modid, this.getName());
	}

	/**
	 * Returns the language key for this projectile. Ex: lycanitesmobs.chaosorb
	 * @return Creature language key.
	 */
	public String getLocalisationKey() {
		return this.modInfo.modid + "." + this.getName();
	}

	/**
	 * Returns a translated title for this projectile. Ex: Chaos Orb
	 * @return The display name of this projectile.
	 */
	public TextComponent getTitle() {
		return new TranslationTextComponent("entity." + this.getLocalisationKey());
	}

	/**
	 * Creates a projectile instance using this info.
	 * @param world The world to create the projectile in.
	 * @param entityLivingBase The entity that created the projectile.
	 */
	public BaseProjectileEntity createProjectile(World world, LivingEntity entityLivingBase) {
		if(this.modelClassName != null && ModelManager.getInstance().getProjectileModel(this) != null) {
			return new CustomProjectileModelEntity(this.getEntityType(), world, entityLivingBase, this);
		}
		return new CustomProjectileEntity(this.getEntityType(), world, entityLivingBase, this);
	}

	/**
	 * Creates a projectile instance using this info.
	 * @param world The world to create the projectile in.
	 * @param x The x position of the projectile.
	 * @param y The y position of the projectile.
	 * @param z The z position of the projectile.
	 */
	public BaseProjectileEntity createProjectile(World world, double x, double y, double z) {
		return new CustomProjectileEntity(this.getEntityType(), world, x, y, z, this);
	}

	public SoundEvent getLaunchSound() {
		return ObjectManager.getSound(this.name);
	}

	public SoundEvent getImpactSound() {
		return ObjectManager.getSound(this.name + "_impact");
	}

	/**
	 * Returns if this Projectile has the provided element.
	 * @param element The element to check for.
	 * @return True if this projectile has the element.
	 */
	public boolean hasElement(ElementInfo element) {
		return this.elements.contains(element);
	}
}
