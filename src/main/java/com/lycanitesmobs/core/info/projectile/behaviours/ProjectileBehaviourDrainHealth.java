package com.lycanitesmobs.core.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProjectileBehaviourDrainHealth extends ProjectileBehaviour {
	/** The scale of damage converted into healing. **/
	public float rate = 0.5F;

	/** The scale of damage converted into healing for the attacker's mount if they are riding one. **/
	public float mountRate = 0.1F;

	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("rate"))
			this.rate = json.get("rate").getAsFloat();
		if(json.has("mountRate"))
			this.mountRate = json.get("mountRate").getAsFloat();
	}

	@Override
	public void onProjectileDamage(BaseProjectileEntity projectile, World world, LivingEntity target, float damage) {
		if(projectile.getOwner() == null) {
			return;
		}

		if (projectile.getOwner() instanceof LivingEntity) {
			((LivingEntity) projectile.getOwner()).heal(damage * this.rate);
		}
		if(projectile.getOwner().getVehicle() instanceof LivingEntity) {
			((LivingEntity)projectile.getOwner().getVehicle()).heal(damage * this.mountRate);
		}
	}
}
