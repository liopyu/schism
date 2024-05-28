package com.lycanitesmobs.client;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureModel;
import com.lycanitesmobs.client.model.EquipmentModel;
import com.lycanitesmobs.client.model.ModelEquipmentPart;
import com.lycanitesmobs.client.model.ProjectileModel;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.item.equipment.EquipmentPartManager;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ModelManager {
	private static ModelManager INSTANCE;
	public static ModelManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new ModelManager();
		}
		return INSTANCE;
	}

	public Map<CreatureInfo, CreatureModel> creatureModels = new HashMap<>();
	public Map<Subspecies, CreatureModel> creatureSubspeciesModels = new HashMap<>();
	public Map<ProjectileInfo, ProjectileModel> projectileModels = new HashMap<>();
	public Map<String, ProjectileModel> oldProjectileModels = new HashMap<>();
	public EquipmentModel equipmentModel;
	public Map<ItemEquipmentPart, ModelEquipmentPart> equipmentPartModels = new HashMap<>();

	/**
	 * Creates all models to be used.
	 */
	public void createModels() {
		try {
			// Creature Models:
			for(CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
				if(creatureInfo.dummy) {
					continue;
				}
				this.creatureModels.put(creatureInfo, (CreatureModel)Class.forName(creatureInfo.modelClassName).getConstructor().newInstance());
				for(Subspecies subspecies : creatureInfo.subspecies.values()) {
					if(subspecies.modelClassName != null) {
						this.creatureSubspeciesModels.put(subspecies, (CreatureModel)Class.forName(subspecies.modelClassName).getConstructor().newInstance());
					}
				}
			}
		}
		catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			LycanitesMobs.logError("Unable to load a Creature model, check that the model class name(s) is correct in the associated creature json (check subspecies models if any too).");
			throw new RuntimeException(e);
		}

		try {
			// Projectile Models:
			for(ProjectileInfo projectileInfo : ProjectileManager.getInstance().projectiles.values()) {
				if(projectileInfo.modelClassName != null) {
					this.projectileModels.put(projectileInfo, (ProjectileModel)Class.forName(projectileInfo.modelClassName).getConstructor().newInstance());
				}
			}
		}
		catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
			LycanitesMobs.logError("Unable to load a Projectile model, check that the model class name is correct in the associated projectile json.");
			throw new RuntimeException(e);
		}

		// Equipment Models:
		this.equipmentModel = new EquipmentModel();
		for(ItemEquipmentPart equipmentPart : EquipmentPartManager.getInstance().equipmentParts.values()) {
			this.equipmentPartModels.put(equipmentPart, new ModelEquipmentPart(equipmentPart));
		}
	}

	/**
	 * Gets the model used by the provided Creature and Subspecies.
	 * @param creatureInfo The creature info to get the model for.
	 * @param subspecies The creature's subspecies, can be null for default model.
	 * @return The Creature Model.
	 */
	public CreatureModel getCreatureModel(CreatureInfo creatureInfo, @Nullable Subspecies subspecies) {
		if(subspecies != null && this.creatureSubspeciesModels.containsKey(subspecies)) {
			return this.creatureSubspeciesModels.get(subspecies);
		}
		if(this.creatureModels.containsKey(creatureInfo)) {
			return this.creatureModels.get(creatureInfo);
		}
		return null;
	}

	/**
	 * Gets the model used by the provided Projectile.
	 * @param projectileInfo The projectile info to get the model for.
	 * @return The Projectile Model.
	 */
	public ProjectileModel getProjectileModel(ProjectileInfo projectileInfo) {
		if(this.projectileModels.containsKey(projectileInfo)) {
			return this.projectileModels.get(projectileInfo);
		}
		return null;
	}

	/**
	 * Gets the model used by the provided Old Projectile Name, this should be phased out in favor of the new JSON based projectiles.
	 * @param projectileName The name of the old projectile to get the model for.
	 * @return The Old Projectile Model.
	 */
	@Deprecated
	public ProjectileModel getOldProjectileModel(String projectileName) {
		if(this.oldProjectileModels.containsKey(projectileName)) {
			return this.oldProjectileModels.get(projectileName);
		}
		return null;
	}

	/**
	 * Gets the model used by assembled Equipment Pieces.
	 * @return The Equipment Model, the same model should always be used.
	 */
	public EquipmentModel getEquipmentModel() {
		return this.equipmentModel;
	}

	/**
	 * Gets the model used by the provided Equipment Part.
	 * @param equipmentPart The equipment part item to get the model for.
	 * @return The Equipment Part Model.
	 */
	public ModelEquipmentPart getEquipmentPartModel(ItemEquipmentPart equipmentPart) {
		if(this.equipmentPartModels.containsKey(equipmentPart)) {
			return this.equipmentPartModels.get(equipmentPart);
		}
		return null;
	}
}
