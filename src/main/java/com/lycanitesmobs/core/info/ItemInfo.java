package com.lycanitesmobs.core.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.item.GenericItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

import java.util.Iterator;

public class ItemInfo {
	/** The Item based on this Item Info. **/
	public GenericItem item;

	// Core Info:
	/** The name of this item. Lowercase, no space, used for language entries and for generating the projectile id, etc. Required. **/
	protected String name;

	/** The group that this item belongs to. **/
	public ModInfo group;

	/**
	 * Constructor
	 * @param group The group that this item definition will belong to.
	 */
	public ItemInfo(ModInfo group) {
		this.group = group;
	}

	/** Loads this item from a JSON object. **/
	public void loadFromJSON(JsonObject json) {
		this.name = json.get("name").getAsString();

		// Model (Optional):
		String modelName = null;
		if(json.has("model")) {
			modelName = json.get("model").getAsString();
		}

		// Group:
		ItemGroup group = ItemManager.getInstance().itemsGroup;
		if(json.has("group")) {
			String groupName = json.get("group").getAsString();
			if("blocks".equalsIgnoreCase(groupName))
				group = ItemManager.getInstance().blocksGroup;
			if("creatures".equalsIgnoreCase(groupName))
				group = ItemManager.getInstance().creaturesGroups;
		}

		// Stack Size:
		int maxStackSize = 64;
		if(json.has("maxStackSize"))
			maxStackSize = json.get("maxStackSize").getAsInt();

		// Food:
		Food food = null;
		if(json.has("food")) {
			JsonObject foodJson = json.get("food").getAsJsonObject();
			Food.Builder foodBuilder = new Food.Builder();
			foodBuilder.nutrition(foodJson.get("hunger").getAsInt());
			foodBuilder.saturationMod(foodJson.get("saturation").getAsFloat());

			if(!foodJson.has("alwaysEdible") || foodJson.get("alwaysEdible").getAsBoolean())
				foodBuilder.alwaysEat();

			if(foodJson.has("fast") && foodJson.get("fast").getAsBoolean())
				foodBuilder.fast();

			if(foodJson.has("meat") && foodJson.get("meat").getAsBoolean())
				foodBuilder.meat();

			if(foodJson.has("effects")) {
				JsonArray effectsJson = foodJson.getAsJsonArray("effects");
				Iterator<JsonElement> jsonIterator = effectsJson.iterator();
				while (jsonIterator.hasNext()) {
					JsonObject foodEffectJson = jsonIterator.next().getAsJsonObject();
					String effectId = foodEffectJson.get("effectId").getAsString();
					String[] effectIds = effectId.split(":"); // Can't get effects from registry yet, this means no effects from other mods. :(
					Effect effect;
					if("minecraft".equals(effectIds[0]))
						effect = ObjectLists.allEffects.get(effectIds[1]);
					else
						effect = ObjectManager.getEffect(effectIds[1]);
					if(effect == null) {
						LycanitesMobs.logWarning("", "Unable to add food effect: " + effectId);
						continue;
					}
					EffectInstance effectInstance = new EffectInstance(effect, foodEffectJson.get("duration").getAsInt() * 20, foodEffectJson.get("amplifier").getAsInt());

					float chance = 1F;
					if(foodEffectJson.has("chance"))
						chance = foodEffectJson.get("chance").getAsFloat();

					foodBuilder.effect(effectInstance, chance);
				}
			}

			food = foodBuilder.build();
		}

		// Create Item Properties:
		Item.Properties properties = new Item.Properties();
		/*if(modelName != null) TODO Generic Item Model Renderer
			properties.setTEISR(() -> com.lycanitesmobs.core.renderer.EquipmentRenderer::new);*/
		properties.tab(group);
		properties.stacksTo(maxStackSize);
		if(food != null)
			properties.food(food);

		// Create Item:
		this.item = new GenericItem(properties, this.name);
		this.item.modelName = modelName;
	}

	/**
	 * Gets the Item based on this ItemInfo/
	 * @return The item.
	 */
	public GenericItem getItem() {
		return this.item;
	}

	/**
	 * Returns the name of this item info, this is the unformatted lowercase name. Ex: cleansingcrystal
	 * @return Item name.
	 */
	public String getName() {
		return this.name;
	}
}
