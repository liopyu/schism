package com.lycanitesmobs.core.spawner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.spawner.location.SpawnLocation;
import com.lycanitesmobs.core.spawner.trigger.ChunkSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.SpawnTrigger;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.*;

public class Spawner {
    /** Spawners are loaded from JSON and operate around their Triggers, Conditions and Locations. **/

	/** A list of all Spawn Conditions which determine if the Triggers should be active or not. **/
	public List<SpawnCondition> conditions = new ArrayList<>();

	/** A list of all Spawn Triggers which listen to events and ticks and attempt a spawn, if conditions are met. **/
	public List<SpawnTrigger> triggers = new ArrayList<>();

	/** A list of all Spawn Locations which are used to determine where this Spawner can actually spawn the mob, some Triggers require a specific spawn location other provide an area. **/
	public List<SpawnLocation> locations = new ArrayList<>();

	/** A list of all Mobs that can be spawned by this spawner. **/
	public List<MobSpawn> mobSpawns = new ArrayList<>();

    /** The name of this spawner, must be unique, used by creatures/groups when entering spawners. **/
    public String name;

	/** The name of this spawner when, used by creatures/groups when entering spawners. By default this copies the name but can be changed and doesn't have to be unique. **/
	public String sharedName;

	/** If set to true and this spawner is a default spawner, it will reset when loading. This must be set to false or removed from the json if it is a customised spawner. **/
	public boolean loadDefault = true;

    /** Can be set to false to completely disable this Spawner. **/
    public boolean enabled = true;

    /** Determines how many Conditions must be met. If 0 or less all are required. **/
    public int conditionsRequired = 0;

	/** Sets how many times any Trigger must activate (per player) before a wave is spawned. Only supported by player based triggers **/
	public int triggersRequired = 1;

	/** A list of messages to send to the player whenever the associated count is reached. **/
	public Map<Integer, String> triggerCountMessages = new HashMap<>();

    /** Determines how a Location is chosen if there are multiple. Can be: order, random or combine. **/
    public String multipleLocations = "combine";

	/** The minimum amount of mobs to spawn each wave. **/
	public int mobCountMin = 1;

	/** The maximum amount of mobs to spawn each wave. **/
	public int mobCountMax = 1;

	/** If true, this Spawner will divide the amount of mobs to spawn by the amount of players near the trigger player (does not affect non-player triggers). **/
	public boolean mobCountPlayerScaling = true;

	/** How far to search around a player for finding nearby other player for mob spawn scaling. **/
	public int mobCountPlayerRange = 64;

	/** If true, this Spawner will ignore all mob dimension checks (not Spawn Condition checks), this bypasses the checks in MobSpawns and SpawnInfos. **/
	public boolean ignoreDimensions = false;

	/** If true, this Spawner will ignore all biome checks, this bypasses the checks in MobSpawns and SpawnInfos. **/
	public boolean ignoreBiomes = false;

	/** If true, this Spawner will ignore collision checks, this may cause mobs to become stuck and suffocate. **/
	public boolean ignoreCollision = false;

	/** If true, this Spawner will ignore light level checks, this bypasses the checks in MobSpawns and SpawnInfos. **/
	public boolean ignoreLightLevel = false;

	/** If true, this Spawner will ignore group limit checks, this bypasses the checks in MobSpawns and SpawnInfos. **/
	public boolean ignoreGroupLimit = false;

	/** The range of how far this Spawner should check for surrounding mobs of the same type. **/
	protected double groupLimitRange = 32;

	/** If set to true, the Forge Can Spawn Event is fired but its result is ignored, use this to prevent other mods from stopping the spawn via the event. **/
	protected boolean ignoreForgeCanSpawnEvent = false;

    /** If true, mobs spawned by this Spawner will not naturally despawn. **/
    public boolean forceNoDespawn = false;

	/** If true, actions caused by this spawner such as block destruction can trigger additional spawn triggers creating a spawner chain. **/
	public boolean chainSpawning = true;

	/** If true, this Spawner will act as enabled even if it can't find any mobs to spawn, useful if you just want to use trigger messages, etc. **/
	public boolean enableWithoutMobs = false;

	/** If set, this is the name of the mob event that must be active, otherwise this Spawner is always active. **/
	public String eventName = "";

	/** If set, when a mob is spawned, blocks in the radius around the spawned mob will be destroyed. **/
	public int blockBreakRadius = -1;

	/** If true, spawned mobs will not drop any items until a player has damaged them in some way. **/
	protected boolean dropsRequirePlayerDamage = false;


	/** Stores how many times each player has triggered any of this spawners Spawn Triggers, the count is reset when a wave is spawned. **/
	protected Map<PlayerEntity, Integer> triggerCounts = new HashMap<>();


    /** Loads this Spawner from the provided JSON data. **/
    public void loadFromJSON(JsonObject json) {
    	// Spawner Properties:
        this.name = json.get("name").getAsString();
        this.sharedName = this.name;

		if(json.has("sharedName"))
			this.sharedName = json.get("sharedName").getAsString();

		if(json.has("loadDefault"))
			this.loadDefault = json.get("loadDefault").getAsBoolean();

        if(json.has("enabled"))
			this.enabled = json.get("enabled").getAsBoolean();

		if(json.has("conditionsRequired"))
			this.conditionsRequired = json.get("conditionsRequired").getAsInt();

		if(json.has("triggersRequired"))
			this.triggersRequired = json.get("triggersRequired").getAsInt();

		if(json.has("triggerCountMessages")) {
			JsonArray jsonArray = json.get("triggerCountMessages").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject tcmJson = jsonIterator.next().getAsJsonObject();
				this.triggerCountMessages.put(tcmJson.get("count").getAsInt(), tcmJson.get("message").getAsString());
			}
		}

		if(json.has("multipleLocations"))
			this.multipleLocations = json.get("multipleLocations").getAsString();

		if(json.has("mobCountMin"))
			this.mobCountMin = json.get("mobCountMin").getAsInt();

		if(json.has("mobCountMax"))
			this.mobCountMax = json.get("mobCountMax").getAsInt();

		if(json.has("mobCountPlayerScaling"))
			this.mobCountPlayerScaling = json.get("mobCountPlayerScaling").getAsBoolean();

		if(json.has("mobCountPlayerRange"))
			this.mobCountPlayerRange = json.get("mobCountPlayerRange").getAsInt();

		if(json.has("ignoreDimensions"))
			this.ignoreDimensions = json.get("ignoreDimensions").getAsBoolean();

		if(json.has("ignoreBiomes"))
		this.ignoreBiomes = json.get("ignoreBiomes").getAsBoolean();

		if(json.has("ignoreCollision"))
			this.ignoreCollision = json.get("ignoreCollision").getAsBoolean();

		if(json.has("ignoreLightLevel"))
			this.ignoreLightLevel = json.get("ignoreLightLevel").getAsBoolean();

		if(json.has("ignoreGroupLimit"))
			this.ignoreGroupLimit = json.get("ignoreGroupLimit").getAsBoolean();

		if(json.has("groupLimitRange"))
			this.groupLimitRange = json.get("groupLimitRange").getAsDouble();

		if(json.has("ignoreForgeCanSpawnEvent"))
			this.ignoreForgeCanSpawnEvent = json.get("ignoreForgeCanSpawnEvent").getAsBoolean();

		if(json.has("forceNoDespawn"))
			this.forceNoDespawn = json.get("forceNoDespawn").getAsBoolean();

		if(json.has("chainSpawning"))
			this.chainSpawning = json.get("chainSpawning").getAsBoolean();

		if(json.has("enableWithoutMobs"))
			this.enableWithoutMobs = json.get("enableWithoutMobs").getAsBoolean();

		if(json.has("eventName"))
			this.eventName = json.get("eventName").getAsString();

		if(json.has("blockBreakRadius"))
			this.blockBreakRadius = json.get("blockBreakRadius").getAsInt();

		if(json.has("dropsRequirePlayerDamage"))
			this.dropsRequirePlayerDamage = json.get("dropsRequirePlayerDamage").getAsBoolean();

		// Conditions:
		if(json.has("conditions")) {
			JsonArray jsonArray = json.get("conditions").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject conditionJson = jsonIterator.next().getAsJsonObject();
				SpawnCondition spawnCondition = SpawnCondition.createFromJSON(conditionJson);
				this.conditions.add(spawnCondition);
			}
		}

		// Triggers:
		if(json.has("triggers")) {
			JsonArray jsonArray = json.get("triggers").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject triggerJson = jsonIterator.next().getAsJsonObject();
				SpawnTrigger spawnTrigger = SpawnTrigger.createFromJSON(triggerJson, this);
				this.triggers.add(spawnTrigger);
				if(this.enabled) {
					SpawnerEventListener.getInstance().addTrigger(spawnTrigger);
				}
			}
		}

		// Locations:
		if(json.has("locations")) {
			JsonArray jsonArray = json.get("locations").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject locationJson = jsonIterator.next().getAsJsonObject();
				SpawnLocation spawnLocation = SpawnLocation.createFromJSON(locationJson);
				this.locations.add(spawnLocation);
			}
		}

		// Mob Spawns:
		if(json.has("mobSpawns")) {
			JsonArray jsonArray = json.get("mobSpawns").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject mobSpawnJson = jsonIterator.next().getAsJsonObject();
				MobSpawn mobSpawn = MobSpawn.createFromJSON(mobSpawnJson);
				if(mobSpawn != null) {
					this.mobSpawns.add(mobSpawn);
				}
			}
		}
    }


	/** Remove this Spawner. This removes all Triggers from the Event Listener and does other cleanup. **/
	public void destroy() {
		for(SpawnTrigger spawnTrigger : this.triggers) {
			SpawnerEventListener.getInstance().removeTrigger(spawnTrigger);
		}
		SpawnerManager.getInstance().removeSpawner(this);
	}


	/** Returns true if this Spawner is considered enabled, this is checked first before major logging and more in depth checks are done in canSpawn(). **/
	public boolean isEnabled(World world, PlayerEntity player) {
		if(!this.enabled || CreatureManager.getInstance().spawnConfig.disableAllSpawning || !world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
			return false;
		}

		// Event Spawner Check:
		if(!"".equals(this.eventName)) {
			ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
			if(worldExt == null) {
				return false;
			}
			if(worldExt.getMobEventPlayerServer(this.eventName) == null) {
				return false;
			}
		}

		if(!this.enableWithoutMobs && this.mobSpawns.isEmpty() && SpawnerMobRegistry.getMobSpawns(this.sharedName) == null) {
			return false;
		}

		return true;
	}


    /** Returns true if Triggers are allowed to operate for this Spawner. **/
    public boolean canSpawn(World world, PlayerEntity player, BlockPos triggerPos) {
    	if(!SpawnerManager.getInstance().globalSpawnConditions.isEmpty()) {
			LycanitesMobs.logDebug("JSONSpawner", "Global Conditions Required: " + SpawnerManager.getInstance().globalSpawnConditions.size());
			for(SpawnCondition condition : SpawnerManager.getInstance().globalSpawnConditions) {
				if(!condition.isMet(world, player, triggerPos)) {
					LycanitesMobs.logDebug("JSONSpawner", "Global Condition: " + condition + "Failed");
					return false;
				}
			}
		}

        if(this.conditions.isEmpty()) {
			LycanitesMobs.logDebug("JSONSpawner", "No Conditions");
        	return true;
		}

		LycanitesMobs.logDebug("JSONSpawner", "Conditions Required: " + (this.conditionsRequired > 0 ? this.conditionsRequired : "All"));
        int conditionsMet = 0;
        int conditionsRequired = this.conditionsRequired > 0 ? this.conditionsRequired : this.conditions.size();
        for(SpawnCondition condition : this.conditions) {
        	boolean met = condition.isMet(world, player, triggerPos);
			LycanitesMobs.logDebug("JSONSpawner", "Condition: " + condition + " " + (met ? "Passed" : "Failed"));
            if(met) {
                if(++conditionsMet >= conditionsRequired) {
					LycanitesMobs.logDebug("JSONSpawner", "Sufficient Conditions Met");
                    return true;
                }
            }
        }

		LycanitesMobs.logDebug("JSONSpawner", "Insufficient Conditions Met: " + conditionsMet + "/" + conditionsRequired);
        return false;
    }


	/**
	 * Starts a new spawn, usually called by Triggers.
	 * @param world The World to spawn in.
	 * @param player The player that is being spawned around. If null all player based checks and features are ignored.
	 * @param spawnTrigger The spawn trigger that has triggered this spawn.
	 * @param triggerPos The location that the spawn was triggered, usually used as the center for spawning around or on.
	 * @param level The level of the spawn trigger, higher levels are from rarer spawn conditions and can result in tougher mobs being spawned.
	 * @param countAmount How much this trigger affects the trigger count by.
	 * @param chain How far along a spawner chain this trigger is, this increases whenever the actions of one spawner trigger another spawner and is used to prevent loops, etc.
	 * @return True on a successful spawn and false on failure.
	 **/
	public boolean trigger(World world, PlayerEntity player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int countAmount, int chain) {
		if(!this.isEnabled(world, player)) {
			return false;
		}

		LycanitesMobs.logDebug("JSONSpawner", "~O==================== Spawner Triggered: " + this.name + " ====================O~");
		if(!this.canSpawn(world, player, triggerPos)) {
			LycanitesMobs.logDebug("JSONSpawner", "This Spawner Cannot Spawn");
			return false;
		}

		// Only One Trigger Required:
		if(this.triggersRequired <= 1 || player == null) {
			LycanitesMobs.logDebug("JSONSpawner", "Only one trigger required.");
			return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
		}

		// Get Current Count:
		if(!this.triggerCounts.containsKey(player)) {
			this.triggerCounts.put(player, Math.max(countAmount, 0));
		}
		int currentCount = this.triggerCounts.get(player);
		int lastCount = currentCount;

		// Change Count:
		if(countAmount == 0) {
			currentCount = 0;
		}
		else {
			currentCount += countAmount;
		}
		if(currentCount != lastCount) {
			if(this.triggerCountMessages.containsKey(currentCount)) {
				ITextComponent message = new TranslationTextComponent(this.triggerCountMessages.get(currentCount));
				ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
				if (extendedPlayer != null) {
					extendedPlayer.sendOverlayMessage(message);
				}
			}
		}

		// Required Count Met:
		LycanitesMobs.logDebug("JSONSpawner", "Trigger " + currentCount + "/" + this.triggersRequired);
		if(currentCount >= this.triggersRequired) {
			this.triggerCounts.put(player, 0);
			return this.doSpawn(world, player, spawnTrigger, triggerPos, level, chain);
		}

		this.triggerCounts.put(player, currentCount);
		return false;
	}


    /**
     * Starts a new spawn, usually called from trigger() when the count is sufficient.
     * @param world The World to spawn in.
     * @param player The player that is being spawned around.
	 * @param spawnTrigger The spawn trigger that has triggered this spawn.
     * @param triggerPos The location that the spawn was triggered, usually used as the center for spawning around or on.
	 * @param level The level of the spawn trigger, higher levels are from rarer spawn conditions and can result in tougher mobs being spawned.
	 * @param chain How far along a spawner chain this trigger is, this increases whenever the actions of one spawner trigger another spawner and is used to prevent loops, etc.
     * @return True on a successful spawn and false on failure.
     **/
    public boolean doSpawn(World world, PlayerEntity player, SpawnTrigger spawnTrigger, BlockPos triggerPos, int level, int chain) {
		// Get Amount of Mobs To Spawn:
		int mobCount = this.mobCountMin;
    	if(this.mobCountMax > this.mobCountMin) {
    		mobCount = world.random.nextInt(this.mobCountMax - this.mobCountMin) + this.mobCountMin;
		}
		if(player != null && this.mobCountPlayerScaling) {
			int nearbyPlayers = 0;
			List<? extends PlayerEntity> allPlayers = world.players();
			for(PlayerEntity targetPlayer : allPlayers) {
				if(targetPlayer.isCreative() || targetPlayer.isSpectator()) {
					if(!SpawnerEventListener.testOnCreative)
						break;
				}
				if(targetPlayer.distanceTo(player) > this.mobCountPlayerRange) {
					break;
				}
				nearbyPlayers++;
			}
			LycanitesMobs.logDebug("JSONSpawner", "Nearby Players: " + nearbyPlayers);
			if(nearbyPlayers > 0) {
				mobCount = (int) Math.ceil((double) mobCount / nearbyPlayers);
			}
		}

		ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
		LycanitesMobs.logDebug("JSONSpawner", "Spawning Wave: " + mobCount + " Mob(s)");
		LycanitesMobs.logDebug("JSONSpawner", "Trigger World: " + world);
		LycanitesMobs.logDebug("JSONSpawner", "Trigger Dimension: " + world.dimension().location());
		LycanitesMobs.logDebug("JSONSpawner", "Trigger Player: " + player);
		LycanitesMobs.logDebug("JSONSpawner", "Trigger Position: " + triggerPos);

    	// Get Positions:
		List<BlockPos> spawnPositions = this.getSpawnPos(world, player, triggerPos);
		// Restrict To Chunk On Chunk Spawn Trigger:
		if(spawnTrigger instanceof ChunkSpawnTrigger) {
			for(BlockPos spawnPos : spawnPositions.toArray(new BlockPos[spawnPositions.size()])) {
				if(spawnPos.getX() < triggerPos.getX() - 8 || spawnPos.getX() > triggerPos.getX() + 8) {
					LycanitesMobs.logDebug("JSONSpawner", "A spawn position exceeds chunk x boundaries and will not be used for worldgen triggered spawning. Trigger Pos: " + triggerPos + " Spawn Pos: " + spawnPos);
					spawnPositions.remove(spawnPos);
					continue;
				}
				if(spawnPos.getZ() < triggerPos.getZ() - 8 || spawnPos.getZ() > triggerPos.getZ() + 8) {
					LycanitesMobs.logDebug("JSONSpawner", "A spawn position exceeds chunk z boundaries and will not be used for worldgen triggered spawning. Trigger Pos: " + triggerPos + " Spawn Pos: " + spawnPos);
					spawnPositions.remove(spawnPos);
				}
			}
		}
		if(spawnPositions.isEmpty()) {
			LycanitesMobs.logDebug("JSONSpawner", "No Spawn Positions Found From Spawn Location");
			return false;
		}
		Collections.shuffle(spawnPositions);
		LycanitesMobs.logDebug("JSONSpawner", "Positions Found: " + spawnPositions.size());

		// Get Biomes:
		List<Biome> biomes = null;
		if(!this.ignoreBiomes) {
			biomes = new ArrayList<>();
			for(BlockPos spawnPos : spawnPositions) {
				Biome biome = world.getBiomeManager().getBiome(spawnPos); //getBiomeManager().getBiome()
				if(!biomes.contains(biome))
					biomes.add(biome);
			}
			LycanitesMobs.logDebug("JSONSpawner", "Biomes Found: " + biomes.size());
		}
		else {
			LycanitesMobs.logDebug("JSONSpawner", "All biome checks are ignored by this Spawner.");
		}

		// Get Mobs:
		Map<Biome, List<MobSpawn>> mobSpawns = this.getMobSpawns(world, player, spawnPositions.size(), biomes);
		LycanitesMobs.logDebug("JSONSpawner", "Spawnable Mobs Found: " + mobSpawns.size());
		if(mobSpawns.size() == 0) {
			return false;
		}

		int mobsSpawned = 0;
		for(BlockPos spawnPos : spawnPositions) {
			if(mobsSpawned >= mobCount) {
				break;
			}
			Biome spawnBiome = world.getBiomeManager().getBiome(spawnPos);

			LycanitesMobs.logDebug("JSONSpawner", "---------- Spawn Iteration: " + mobsSpawned + " ----------");
			LycanitesMobs.logDebug("JSONSpawner", "Spawn Position: " + spawnPos);
			LycanitesMobs.logDebug("JSONSpawner", "Spawn Biome: " + spawnBiome.getRegistryName());

			// Choose Mob To Spawn:
			MobSpawn mobSpawn = null;
			if(mobSpawns.containsKey(null)) {
				LycanitesMobs.logDebug("JSONSpawner", "Biome Ignored");
				mobSpawn = this.chooseMobToSpawn(world, mobSpawns.get(null));
			}
			else if(mobSpawns.containsKey(spawnBiome)) {
				mobSpawn = this.chooseMobToSpawn(world, mobSpawns.get(spawnBiome));
			}
			if(mobSpawn == null) {
				LycanitesMobs.logDebug("JSONSpawner", "No Mob Spawn Chosen");
				continue;
			}
			LycanitesMobs.logDebug("JSONSpawner", "Spawn Mob: " + mobSpawn);

			// Create Entity:
			LivingEntity entityLiving = mobSpawn.createEntity(world);
			if (entityLiving == null) {
				LycanitesMobs.logWarning("JSONSpawner", "Unable to instantiate an entity. Class: " + mobSpawn.entityType);
				continue;
			}
			BaseCreatureEntity entityCreature = null;
			if (entityLiving instanceof BaseCreatureEntity) {
				entityCreature = (BaseCreatureEntity)entityLiving;
				LycanitesMobs.logDebug("JSONSpawner", "Mob is a Lycanites Mob");
			}
			else {
				LycanitesMobs.logDebug("JSONSpawner", "Mob is not a Lycanites Mob");
			}

			// Attempt To Spawn LivingEntity:
			LycanitesMobs.logDebug("JSONSpawner", "Attempting to spawn " + entityLiving + "...");
			entityLiving.moveTo((double) spawnPos.getX() + 0.5D, (double) spawnPos.getY(), (double) spawnPos.getZ() + 0.5D, world.random.nextFloat() * 360.0F, 0.0F);

			// Forge Can Spawn Event:
			Event.Result canSpawn = Event.Result.ALLOW;
			if(entityLiving instanceof MobEntity) {
				canSpawn = ForgeEventFactory.canEntitySpawn((MobEntity)entityLiving, world, (float) spawnPos.getX() + 0.5F, (float) spawnPos.getY(), (float) spawnPos.getZ() + 0.5F, null, SpawnReason.NATURAL);
				if (canSpawn == Event.Result.DENY && !this.ignoreForgeCanSpawnEvent && !mobSpawn.ignoreForgeCanSpawnEvent) {
					LycanitesMobs.logDebug("JSONSpawner", "Spawn Check Failed! Spawning blocked by Forge Can Spawn Event, this is caused by another mod.");
					continue;
				}
			}

			// Mob Instance Spawn Check:
			if(!this.mobInstanceSpawnCheck(entityLiving, mobSpawn, world, player, spawnPos, level, canSpawn == Event.Result.ALLOW)) {
				LycanitesMobs.logDebug("JSONSpawner", "Mob Instance Spawn Check Failed!");
				continue;
			}

			// Spawn The Mob:
			this.spawnEntity(world, worldExt, entityLiving, level, mobSpawn, player, chain);

			// Call Entity's Initial Spawn:
			if (entityLiving instanceof MobEntity) {
				if (!ForgeEventFactory.doSpecialSpawn((MobEntity)entityLiving, world, (float) spawnPos.getX() + 0.5F, (float) spawnPos.getY(), (float) spawnPos.getZ() + 0.5F, null, SpawnReason.NATURAL)) {
					((MobEntity)entityLiving).finalizeSpawn((IServerWorld) world, world.getCurrentDifficultyAt(spawnPos), SpawnReason.NATURAL, null, null);
				}
			}

			LycanitesMobs.logDebug("JSONSpawner", "Spawn Checks Passed! Mob Spawned!");
			mobsSpawned++;
		}

        return mobsSpawned > 0;
    }


		/**
		 * Gets a list of BlockPos to spawn at. Returns an empty list if all Spawn Locations fail to get a spawn position. If no Spawn Locations are set, the triggerPos is used.
		 * @param world The World to spawn in.
		 * @param player The player that is being spawned around.
		 * @param triggerPos The location that the spawn was triggered, usually used as the center for spawning around or on.
		 * @return True on a successful spawn and false on failure.
		 **/
	public List<BlockPos> getSpawnPos(World world, PlayerEntity player, BlockPos triggerPos) {
		LycanitesMobs.logDebug("JSONSpawner", "Searching for Spawn Positions...");
		List<BlockPos> spawnPositions = new ArrayList<>();

		if(this.locations.isEmpty()) {
			LycanitesMobs.logDebug("JSONSpawner", "No Spawn Locations, Using Trigger Position");
			spawnPositions.add(triggerPos);
			return spawnPositions;
		}
		if(this.locations.size() == 1) {
			LycanitesMobs.logDebug("JSONSpawner", "Only One Spawn Location Type");
			return this.locations.get(0).getSpawnPositions(world, player, triggerPos);
		}

		LycanitesMobs.logDebug("JSONSpawner", "Multiple Spawn Location Types, Mode: " + this.multipleLocations);
		if("order".equals(this.multipleLocations)) {
			for(SpawnLocation location : this.locations) {
				spawnPositions = location.getSpawnPositions(world, player, triggerPos);
				if(!spawnPositions.isEmpty()) {
					return spawnPositions;
				}
			}
		}

		if("random".equals(this.multipleLocations)) {
			List<List<BlockPos>> possibleSpawnPosLists = new ArrayList<>();
			for(SpawnLocation location : this.locations) {
				spawnPositions = location.getSpawnPositions(world, player, triggerPos);
				if(!spawnPositions.isEmpty()) {
					possibleSpawnPosLists.add(spawnPositions);
				}
			}
			if(possibleSpawnPosLists.isEmpty()) {
				return spawnPositions;
			}
			if(possibleSpawnPosLists.size() == 1) {
				return possibleSpawnPosLists.get(0);
			}
			int randomIndex = world.random.nextInt(possibleSpawnPosLists.size());
			return possibleSpawnPosLists.get(randomIndex);
		}

		if("combine".equals(this.multipleLocations)) {
			for(SpawnLocation location : this.locations) {
				spawnPositions.addAll(location.getSpawnPositions(world, player, triggerPos));
			}
			return spawnPositions;
		}

		return spawnPositions;
	}

    /**
     * Returns all viable mobs that can be spawned within the spawn conditions. Spawn block costs, chances and biomes are checked here.
     * @param world The World to spawn in.
     * @param player The player that is being spawned around. Can be null.
	 * @param blockCount The total number of possible spawn positions found.
	 * @param biomes A list of all biomes within the spawning area. If null, biome checks will be ignored, all mob spawn will be added to a null map key.
     * @return A map of biomes and mob spawn lists with each mob spawn available to each biome.
     **/
    public Map<Biome, List<MobSpawn>> getMobSpawns(World world, @Nullable PlayerEntity player, int blockCount, @Nullable List<Biome> biomes) {
    	Map<Biome, List<MobSpawn>> mobSpawns = new HashMap<>();

    	if(biomes == null) {
			mobSpawns.put(null, this.getBiomeSpawns(world, player, blockCount, null));
			return mobSpawns;
		}

    	for(Biome biome: biomes) {
    		mobSpawns.put(biome, this.getBiomeSpawns(world, player, blockCount, biome));
		}
		return mobSpawns;
    }

	/**
	 * Returns all viable mobs that can be spawned within the spawn conditions and specific biome.
	 * @param world The world to spawn in.
	 * @param player The player that is being spawned around. Can be null.
	 * @param blockCount The total number of possible spawn positions found.
	 * @param biome The biome to spawn in.
	 * @return A list of mob spawns.
	 */
	public List<MobSpawn> getBiomeSpawns(World world, @Nullable PlayerEntity player, int blockCount, @Nullable Biome biome) {
		List<MobSpawn> possibleSpawns = new ArrayList<>();

		// Global Spawns:
		Collection<MobSpawn> globalSpawns = SpawnerMobRegistry.getMobSpawns(this.sharedName);
		if(globalSpawns != null) {
			possibleSpawns.addAll(globalSpawns);
		}

		// Local Spawns:
		possibleSpawns.addAll(this.mobSpawns);

		// Get Viable Spawns:
		List<MobSpawn> viableMobSpawns = new ArrayList<>();
		for(MobSpawn possibleMobSpawn : possibleSpawns) {
			if(possibleMobSpawn.canSpawn(world, blockCount, biome, this.ignoreDimensions)) {
				viableMobSpawns.add(possibleMobSpawn);
			}
		}

		return viableMobSpawns;
	}

	/**
	 * Gets a weighted random mob to spawn.
	 * @param world The World to spawn in.
	 * @param mobSpawns A list of MobSpawns to choose from.
	 * @return The MobSpawn of the mob to spawn or null if no mob can be spawned.
	 **/
	public MobSpawn chooseMobToSpawn(World world, List<MobSpawn> mobSpawns) {
		// Get Weights:
		int totalWeights = 0;
		for(MobSpawn mobSpawn : mobSpawns) {
			totalWeights += mobSpawn.getWeight();
		}
		if(totalWeights <= 0) {
			return null;
		}

		// Pick Random Spawn Using Weights:
		int randomWeight = 1;
		if(totalWeights > 1) {
			randomWeight = world.random.nextInt(totalWeights);
		}
		int searchWeight = 0;
		MobSpawn chosenMobSpawn = null;
		for(MobSpawn mobSpawn : mobSpawns) {
			chosenMobSpawn = mobSpawn;
			if(mobSpawn.getWeight() + searchWeight > randomWeight) {
				break;
			}
			searchWeight += mobSpawn.getWeight();
		}
		return chosenMobSpawn;
	}

	/**
	 * Performs a check from the Mob Instance, this mob has not yet been spawned into the world, but has been instantiated and is able to do per mob checks.
	 * @param entityLiving The to be spawned mob INSTANCE.
	 * @param mobSpawn The MobSpawn that is controlling the conditions of the spawn.
	 * @param world The World to spawn in.
	 * @param player The Player that triggered the spawn. Can be null.
	 * @param spawnPos The position to spawn at.
	 * @param level The level of the spawn.
	 * @param forgeForced If true, the Forge Can Spawn Event wants to force this mob to spawn.
	 * @return True if the check has passed.
	 **/
	public boolean mobInstanceSpawnCheck(LivingEntity entityLiving, MobSpawn mobSpawn, World world, PlayerEntity player, BlockPos spawnPos, int level, boolean forgeForced) {
		// Lycanites Mobs:
		if (entityLiving instanceof BaseCreatureEntity) {
			BaseCreatureEntity entityCreature = (BaseCreatureEntity)entityLiving;

			LycanitesMobs.logDebug("JSONSpawner", "Checking Mob Collision...");
			if(!this.ignoreCollision && !entityCreature.checkSpawnCollision(world, spawnPos)) {
				return false;
			}

			LycanitesMobs.logDebug("JSONSpawner", "Checking For Nearby Boss...");
			if(!entityCreature.checkSpawnBoss(world, spawnPos)) {
				return false;
			}

			if(mobSpawn.ignoreMobInstanceConditions) {
				LycanitesMobs.logDebug("JSONSpawner", "All Mob Instance Checks Ignored");
				return true;
			}

			if(!this.ignoreLightLevel && !mobSpawn.ignoreLightLevel) {
				LycanitesMobs.logDebug("JSONSpawner", "Checking Light Level...");
				if(!entityCreature.checkSpawnLightLevel(world, spawnPos)) {
					return false;
				}
			}

			if(!this.ignoreGroupLimit && !mobSpawn.ignoreGroupLimit) {
				LycanitesMobs.logDebug("JSONSpawner", "Checking Group Limit...");
				if(!entityCreature.checkSpawnGroupLimit(world, spawnPos, this.groupLimitRange)) {
					return false;
				}
			}

			return true;
		}

		// Vanilla or Other Mod Mob:
		if(!mobSpawn.ignoreMobInstanceConditions && entityLiving instanceof MobEntity) {
			LycanitesMobs.logDebug("JSONSpawner", "None Lycanites Mobs Checks...");
			return ((MobEntity)entityLiving).checkSpawnRules(world, SpawnReason.NATURAL);
		}
		LycanitesMobs.logDebug("JSONSpawner", "All Mob Instance Checks Ignored");
		return true;
	}

	/**
	 * Spawns an entity into the world. The mob INSTANCE should have already been positioned.
	 * @param world The world to spawn in.
	 * @param entityLiving The entity to spawn.
	 */
	public void spawnEntity(World world, ExtendedWorld worldExt, LivingEntity entityLiving, int level, MobSpawn mobSpawn, PlayerEntity player, int chain) {
		// Before Spawn:
		entityLiving.setPortalCooldown(); // Start Portal Cooldown

		BaseCreatureEntity entityCreature;
		if(entityLiving instanceof BaseCreatureEntity) {
			entityCreature = (BaseCreatureEntity)entityLiving;
			entityCreature.forceNoDespawn = this.forceNoDespawn;
			entityCreature.spawnedRare = level > 0;
			if (this.blockBreakRadius > -1 && chain == 0) {
				BlockPos creaturePos = entityLiving.blockPosition();
				entityCreature.destroyArea(creaturePos.getX(), creaturePos.getY() - 1, creaturePos.getZ(), 100, true, this.blockBreakRadius, this.chainSpawning ? player : null, chain + 1);
			}
			entityCreature.setDropsRequirePlayerDamage(this.dropsRequirePlayerDamage);

			// Apply Mob Event:
			if (!"".equals(this.eventName) && worldExt != null) {
				MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
				if(mobEventPlayerServer != null) {
					entityCreature.spawnEventType = mobEventPlayerServer.mobEvent.title;

					// World Event Binding:
					if(mobEventPlayerServer.mobEvent.name.equals(worldExt.getWorldEventName())) {
						entityCreature.spawnEventCount = worldExt.getWorldEventCount();
					}
				}
			}
		}

		// Spawn:
		LycanitesMobs.logDebug("JSONSpawner", "Adding Entity To World: " + world + " - " + entityLiving);
		world.addFreshEntity(entityLiving);

		// After Spawn:
		mobSpawn.onSpawned(entityLiving, player);
		if(!"".equals(this.eventName) && worldExt != null) {
			MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.eventName);
			if(mobEventPlayerServer != null) {
				MobEvent mobEvent = mobEventPlayerServer.mobEvent;
				mobEvent.onSpawn(entityLiving, mobEventPlayerServer.world, mobEventPlayerServer.player, mobEventPlayerServer.origin, mobEventPlayerServer.level, mobEventPlayerServer.ticks, -1);
			}
		}
	}
}
