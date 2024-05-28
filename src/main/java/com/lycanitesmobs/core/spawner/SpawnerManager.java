package com.lycanitesmobs.core.spawner;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.FileLoader;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.StreamLoader;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.spawner.condition.SpawnCondition;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class SpawnerManager extends JSONLoader {
	/** This manages all Spawners, it load them and can also destroy them. Spawners are then ran by Spawn Triggers which are called from the SpawnerEventListener. **/

	public static SpawnerManager INSTANCE;

	public Map<String, Spawner> spawners = new HashMap<>();
	public List<SpawnCondition> globalSpawnConditions = new ArrayList<>();


	/** Returns the main SpawnerManager INSTANCE or creates it and returns it. **/
	public static SpawnerManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new SpawnerManager();
		}
		return INSTANCE;
	}


	/** Loads all JSON Spawners. **/
	public void loadAllFromJson(ModInfo modInfo) {
		// Spawners:
		this.loadAllJson(modInfo, "Spawner", "spawners", "name", true, "spawner", FileLoader.SERVER, StreamLoader.SERVER);
		LycanitesMobs.logDebug("Spawner", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");

		// Mob Event Spawners:
		this.loadAllJson(modInfo, "Spawner", "mobevents", "name", true, "spawner", FileLoader.COMMON, StreamLoader.COMMON);
		LycanitesMobs.logDebug("Spawner", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");

		// Load Global Spawn Conditions:
		Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
		String configPath = new File(".") + "/config/" + LycanitesMobs.MODID + "/";
		this.globalSpawnConditions.clear();

		JsonObject defaultGlobalJson;
		if(FileLoader.SERVER.ready) {
			Path defaultGlobalPath = FileLoader.SERVER.getPath("globalspawner.json");
			LycanitesMobs.logDebug("", "Global Spawner JSON Path:" + defaultGlobalPath);
			defaultGlobalJson = this.loadJsonObject(gson, defaultGlobalPath);
		}
		else {
			defaultGlobalJson = this.loadJsonObject(gson, StreamLoader.SERVER.getStream("globalspawner.json"));
		}
		if(defaultGlobalJson == null) {
			LycanitesMobs.logWarning("", "Could not find Global Spawning JSON.");
		}

		File customGlobalFile = new File(configPath + "globalspawner.json");
		JsonObject customGlobalJson = null;
		if(customGlobalFile.exists()) {
			customGlobalJson = this.loadJsonObject(gson, customGlobalFile.toPath());
		}

		JsonObject globalJson = this.writeDefaultJSONObject(gson, "globalspawner", defaultGlobalJson, customGlobalJson);
		if(globalJson.has("conditions")) {
			JsonArray jsonArray = globalJson.get("conditions").getAsJsonArray();
			Iterator<JsonElement> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonObject spawnConditionJson = jsonIterator.next().getAsJsonObject();
				SpawnCondition spawnCondition = SpawnCondition.createFromJSON(spawnConditionJson);
				this.globalSpawnConditions.add(spawnCondition);
			}
		}
		if(this.globalSpawnConditions.size() > 0) {
			LycanitesMobs.logDebug("JSONSpawner", "Loaded " + this.globalSpawnConditions.size() + " Global Spawn Conditions.");
		}
	}


	@Override
	public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
		LycanitesMobs.logDebug("JSONSpawner", "Loading Spawner JSON: " + json);
		Spawner spawner = new Spawner();
		spawner.loadFromJSON(json);
		this.addSpawner(spawner);
	}


	/** Reloads all JSON Spawners. **/
	public void reload() {
		LycanitesMobs.logDebug("JSONSpawner", "Destroying JSON Spawners!");
		for(Spawner spawner : this.spawners.values().toArray(new Spawner[this.spawners.size()])) {
			spawner.destroy();
		}

		this.loadAllFromJson(LycanitesMobs.modInfo);
	}


	/** Adds a new Spawner to this Manager. **/
	public void addSpawner(Spawner spawner) {
		if(this.spawners.containsKey(spawner.name)) {
			LycanitesMobs.logWarning("", "[Spawner Manager] Tried to add a Spawner with a name that is already in use: " + spawner.name);
			return;
		}
		if(this.spawners.values().contains(spawner)) {
			LycanitesMobs.logWarning("", "[Spawner Manager] Tried to add a Spawner that is already added: " + spawner.name);
			return;
		}
		this.spawners.put(spawner.name, spawner);
	}


	/** Removes a Spawner from this Manager. **/
	public void removeSpawner(Spawner spawner) {
		if(!this.spawners.containsKey(spawner.name)) {
			LycanitesMobs.logWarning("", "[Spawner Manager] Tried to remove a Spawner that hasn't been added: " + spawner.name);
			return;
		}
		this.spawners.remove(spawner.name);
	}
}
