package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class EntityFactory implements EntityType.IFactory<Entity> {
	public static EntityFactory INSTANCE;
	/** Returns the main Entity Factory instance or creates it and returns it. **/
	public static EntityFactory getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new EntityFactory();
		}
		return INSTANCE;
	}

	public Map<EntityType, Constructor<? extends Entity>> entityTypeConstructorMap = new HashMap<>();
	public Map<Constructor<? extends Entity>, EntityType> entityConstructorTypeMap = new HashMap<>();
	public Map<String, EntityType> entityTypeNetworkMap = new HashMap<>();

	/**
	 * Adds a new Entity Type and Entity Class mapping for this Factory to create.
	 * @param entityType The Entity Type to create from.
	 * @param entityClass The Entity Class to instantiate for the type.
	 */
	public void addEntityType(EntityType entityType, Constructor<? extends Entity> entityClass, String networkName) {
		LycanitesMobs.logDebug("Entity", "Adding entity: " + entityClass + " Type: " + entityType.getDescription() + " Classification: " + entityType.getCategory());
		this.entityTypeConstructorMap.put(entityType, entityClass);
		this.entityConstructorTypeMap.put(entityClass, entityType);
		this.entityTypeNetworkMap.put(networkName, entityType);
	}

	/**
	 * Creates an entity from an entity type in the provided world.
	 * @param entityType The entity type to create an entity from.
	 * @param world The world to create the entity in.
	 * @return The created entity or null if no entity could be created.
	 */
	@Override
	public Entity create(EntityType entityType, World world) {
		if(!this.entityTypeConstructorMap.containsKey(entityType)) {
			LycanitesMobs.logWarning("", "Unable to find constructor for Entity Type: " + entityType);
			return null;
		}
		LycanitesMobs.logDebug("Entity", "Spawning entity: " + this.entityTypeConstructorMap.get(entityType) + " - " + entityType.getCategory());
		Constructor<? extends Entity> constructor = this.entityTypeConstructorMap.get(entityType);

		try {
			return constructor.newInstance(entityType, world);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public BiFunction<net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity, World, Entity> createOnClientFunction = this::createOnClient;
	/**
	 * Spawns an entity on the client side from a server packet.
	 * @param spawnPacket The entity spawn packet.
	 * @param world The world to spawn in.
	 */
	public Entity createOnClient(net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity spawnPacket, World world) {
		LycanitesMobs.logDebug("", "Client factory called!");
		return null;
	}
}
