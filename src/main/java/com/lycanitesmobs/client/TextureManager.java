package com.lycanitesmobs.client;

import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
	private static TextureManager INSTANCE;
	public static TextureManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new TextureManager();
		}
		return INSTANCE;
	}
	
	// Maps:
	public static Map<String, ResourceLocation> textures = new HashMap<>();
	public static Map<String, ResourceLocation[]> textureGroups = new HashMap<>();

	/**
	 * Registers GUI and misc textures.
	 */
	public void createTextures(ModInfo modInfo) {
		// Beastiary:
		addTexture("GUIBeastiaryBackground", modInfo, "textures/guis/beastiary/background.png");
		addTexture("GUIPetLevel", modInfo, "textures/guis/beastiary/level.png");
		addTexture("GUIPetSpirit", modInfo, "textures/guis/beastiary/spirit.png");
		addTexture("GUIPetSpiritEmpty", modInfo, "textures/guis/beastiary/spirit_empty.png");
		addTexture("GUIPetSpiritUsed", modInfo, "textures/guis/beastiary/spirit_used.png");
		addTexture("GUIPetSpiritFilling", modInfo, "textures/guis/beastiary/spirit_filling.png");
		addTexture("GUIPetBarHealth", modInfo, "textures/guis/beastiary/bar_health.png");
		addTexture("GUIPetBarRespawn", modInfo, "textures/guis/beastiary/bar_respawn.png");
		addTexture("GUIBarExperience", modInfo, "textures/guis/beastiary/bar_experience.png");
		addTexture("GUIPetBarEmpty", modInfo, "textures/guis/beastiary/bar_empty.png");

		// Containers:
		addTexture("GUIInventoryCreature", modInfo, "textures/guis/inventory_creature.png");
		addTexture("GUISummoningPedestal", modInfo, "textures/guis/minion_lg.png");
		addTexture("GUIEquipmentForge", modInfo, "textures/guis/equipmentforge.png");
	}

	public static void addTexture(String name, ModInfo modInfo, String path) {
		name = name.toLowerCase();
		textures.put(name, new ResourceLocation(modInfo.modid, path));
	}

	public static ResourceLocation getTexture(String name) {
		name = name.toLowerCase();
		if(!textures.containsKey(name))
			return null;
		return textures.get(name);
	}
}
