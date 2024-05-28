package com.lycanitesmobs.core.info;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

/** Elements affect what buffs and debuffs a creature has in addition to some strengths and weaknesses and fusion. **/
public class ElementInfo {

	/** The name of this element. **/
	public String name = "default";

	/** The names of elements that make up this elements, used by compound elements. **/
	protected List<String> componentNames = new ArrayList<>();

	/** The elements that make up this elements, used by compound elements. This is populated from the component names list once all elements are loaded. **/
	public List<ElementInfo> components = new ArrayList<>();

	/** The type of this element, can be primal or compound. Default: Automatic (primal if no components, compound if there are components.) **/
	public String type;

	/** A list of beneficial potion effects that this element can grant. **/
	public List<String> buffs = new ArrayList<>();

	/** A list of detrimental potion effects that this element can inflict as well as grant immunity to. **/
	public List<String> debuffs = new ArrayList<>();

	/** A list of potion effects that this element has immunity to. **/
	public List<String> immunities = new ArrayList<>();

	/** A multiplier applied to any buffs caused by this element. **/
	public double buffDurationMultiplier = 1;

	/** A multiplier applied to any debuffs caused by this element. **/
	public double debuffDurationMultiplier = 1;

	/** Whether creatures of this element are able to burn by default. Some creatures will override this with their own settings. **/
	public boolean canBurn = true;

	/** Whether creatures of this element are able to freeze by default. Some creatures will override this with their own settings. **/
	public boolean canFreeze = true;


	/**
	 * Loads this element from a JSON object.
	 * @param json The json to load from.
	 */
	public void loadFromJSON(JsonObject json) {
		if(json.has("name"))
			this.name = json.get("name").getAsString();

		if(json.has("components"))
			this.componentNames = JSONHelper.getJsonStrings(json.get("components").getAsJsonArray());

		if(json.has("type"))
			this.type = json.get("type").getAsString();
		else this.type = this.componentNames.isEmpty() ? "primal" : "compound";

		if(json.has("buffs"))
			this.buffs = JSONHelper.getJsonStrings(json.get("buffs").getAsJsonArray());

		if(json.has("debuffs"))
			this.debuffs = JSONHelper.getJsonStrings(json.get("debuffs").getAsJsonArray());

		if(json.has("immunities"))
			this.immunities = JSONHelper.getJsonStrings(json.get("immunities").getAsJsonArray());

		if(json.has("buffDurationMultiplier"))
			this.buffDurationMultiplier = json.get("buffDurationMultiplier").getAsDouble();

		if(json.has("debuffDurationMultiplier"))
			this.debuffDurationMultiplier = json.get("debuffDurationMultiplier").getAsDouble();

		if(json.has("canBurn"))
			this.canBurn = json.get("canBurn").getAsBoolean();

		if(json.has("canFreeze"))
			this.canFreeze = json.get("canFreeze").getAsBoolean();
	}


	/** Initialises this Element, called once all Elements have been loaded. **/
	public void init() {
		// Compound components:
		if(this.type.equalsIgnoreCase("compound")) {
			for(String componentName : this.componentNames) {
				if(ElementManager.getInstance().elements.containsKey(componentName)) {
					this.components.add(ElementManager.getInstance().elements.get(componentName));
				}
			}
		}
	}


	/**
	 * Returns the display title of this element.
	 * @return The title text.
	 */
	public String getName() {
		return this.name;
	}


	/**
	 * Returns the display title of this element.
	 * @return The title text.
	 */
	public ITextComponent getTitle() {
		return new TranslationTextComponent("element." + this.name);
	}


	/**
	 * Returns the description of this element.
	 * @return The description text.
	 */
	public ITextComponent getDescription() {
		return new TranslationTextComponent("element." + this.name + ".description");
	}


	/**
	 * Applies buffs to the target entity based on this element.
	 * @param targetEntity The entity to buffs.
	 * @param duration The duration (in seconds) of the buffs. If 0 or below, no debuff is applied.
	 * @param amplifier The amplifier of the buffs. If 0 or below, no debuff is applied.
	 */
	public void buffEntity(LivingEntity targetEntity, int duration, int amplifier) {
		if(duration <= 0 || amplifier < 0) {
			return;
		}
		duration = Math.round((float)duration * (float)this.buffDurationMultiplier);
		for(String buff : this.buffs) {
			Effect effect = GameRegistry.findRegistry(Effect.class).getValue(new ResourceLocation(buff));
			if(effect != null) {
				targetEntity.addEffect(new EffectInstance(effect, duration, amplifier));
			}
		}
	}


	/**
	 * Applies debuffs to the target entity based on this element.
	 * @param targetEntity The entity to debuffs.
	 * @param duration The duration (in seconds) of the debuffs. If 0 or below, no debuff is applied.
	 * @param amplifier The amplifier of the debuffs. If 0 or below, no debuff is applied.
	 */
	public void debuffEntity(LivingEntity targetEntity, int duration, int amplifier) {
		if(duration <= 0 || amplifier < 0) {
			return;
		}
		duration = Math.round((float)duration * (float)this.debuffDurationMultiplier);
		for(String debuff : this.debuffs) {
			if("burning".equalsIgnoreCase(debuff)) {
				targetEntity.setSecondsOnFire(duration / 20);
				continue;
			}
			Effect effect = GameRegistry.findRegistry(Effect.class).getValue(new ResourceLocation(debuff));
			if(effect != null) {
				targetEntity.addEffect(new EffectInstance(effect, duration, amplifier));
			}
		}
	}


	/**
	 * Returns if a creature of this element can be affected by the provided effect.
	 * @param effect The effect to check.
	 * @return True if the effect can be applied.
	 */
	public boolean isEffectApplicable(EffectInstance effect) {
		if(effect == null || effect.getEffect() == null || effect.getEffect().getRegistryName() == null) {
			return false;
		}
		if(this.debuffs.contains(effect.getEffect().getRegistryName().toString())) {
			return false;
		}
		if(this.immunities.contains(effect.getEffect().getRegistryName().toString())) {
			return false;
		}
		/*for(ElementInfo element : this.components) {
			if(!element.isEffectApplicable(effect)) {
				return false;
			}
		}*/
		return true;
	}


	/**
	 * Returns if creatures of this element can burn by default. (Fire damage).
	 * @return True if can burn.
	 */
	public boolean canBurn() {
		if(!this.canBurn) {
			return false;
		}
		/*for(ElementInfo element : this.components) {
			if(!element.canBurn()) {
				return false;
			}
		}*/
		return true;
	}


	/**
	 * Returns if creatures of this element can freeze by default. (Ooze damage).
	 * @return True if can burn.
	 */
	public boolean canFreeze() {
		if(!this.canFreeze) {
			return false;
		}
		/*for(ElementInfo element : this.components) {
			if(!element.canFreeze()) {
				return false;
			}
		}*/
		return true;
	}
}
