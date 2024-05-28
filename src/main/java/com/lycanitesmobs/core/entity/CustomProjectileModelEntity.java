package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CustomProjectileModelEntity extends CustomProjectileEntity {

	// ==================================================
	//                   Constructors
	// ==================================================
	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.modInfo = LycanitesMobs.modInfo;
	}

	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, World world, ProjectileInfo projectileInfo) {
		super(entityType, world, projectileInfo);
	}

	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, World world, LivingEntity entityLiving, ProjectileInfo projectileInfo) {
		super(entityType, world, entityLiving, projectileInfo);
	}

	public CustomProjectileModelEntity(EntityType<? extends BaseProjectileEntity> entityType, World world, double x, double y, double z, ProjectileInfo projectileInfo) {
		super(entityType, world, x, y, z, projectileInfo);
	}


	// ==================================================
	//                      Visuals
	// ==================================================
	@Override
	public String getTextureName() {
		return this.entityName.toLowerCase();
	}

	@Override
	public ResourceLocation getTexture() {
		if("projectile".equals(this.getTextureName()))
			return null;
		if(TextureManager.getTexture(this.getTextureName()) == null)
			TextureManager.addTexture(this.getTextureName(), this.modInfo, "textures/projectile/" + this.getTextureName() + ".png");
		return TextureManager.getTexture(this.getTextureName());
	}
}
