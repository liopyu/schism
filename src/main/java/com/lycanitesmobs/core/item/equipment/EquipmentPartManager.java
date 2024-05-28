package com.lycanitesmobs.core.item.equipment;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.FileLoader;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.StreamLoader;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentPartManager extends JSONLoader {

	public static EquipmentPartManager INSTANCE;

	public Map<String, ItemEquipmentPart> equipmentParts = new HashMap<>();

	/** A list of mod groups that have loaded with this Equipment Part Manager. **/
	public List<ModInfo> loadedGroups = new ArrayList<>();

	/** Returns the main EquipmentPartManager INSTANCE or creates it and returns it. **/
	public static EquipmentPartManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new EquipmentPartManager();
		}
		return INSTANCE;
	}

	/** Loads all JSON Equipment Parts. **/
	public void loadAllFromJson(ModInfo modInfo) {
		if(!this.loadedGroups.contains(modInfo)) {
			this.loadedGroups.add(modInfo);
		}
		this.loadAllJson(modInfo, "Equipment", "equipment", "itemName", false, null, FileLoader.COMMON, StreamLoader.COMMON);
		LycanitesMobs.logDebug("Equipment", "Complete! " + this.equipmentParts.size() + " JSON Equipment Parts Loaded In Total.");
	}

	@Override
	public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
		Item.Properties properties = new Item.Properties().stacksTo(1).setNoRepair().tab(ItemManager.getInstance().equipmentPartsGroup).setISTER(() -> com.lycanitesmobs.client.renderer.EquipmentPartRenderer::new);
		ItemEquipmentPart equipmentPart = new ItemEquipmentPart(properties, modInfo);
		equipmentPart.loadFromJSON(json);
		if(this.equipmentParts.containsKey(equipmentPart.itemName)) {
			LycanitesMobs.logWarning("", "[Equipment] Tried to add a Equipment Part with a name that is already in use: " + equipmentPart.itemName);
			throw new RuntimeException("[Equipment] Tried to add a Equipment Part with a name that is already in use: " + equipmentPart.itemName);
		}
		if(this.equipmentParts.values().contains(equipmentPart)) {
			LycanitesMobs.logWarning("", "[Equipment] Tried to add a Equipment Part that is already added: " + equipmentPart.itemName);
			throw new RuntimeException("[Equipment] Tried to add a Equipment Part that is already added: " + equipmentPart.itemName);
		}
		this.equipmentParts.put(equipmentPart.itemName, equipmentPart);
		ObjectManager.addItem(equipmentPart.itemName, equipmentPart);
	}

	/**
	 * Reloads all Equipment part JSON.
	 */
	public void reload() {
		this.equipmentParts.clear();
		for(ModInfo group : this.loadedGroups) {
			this.loadAllFromJson(group);
		}
	}
}
