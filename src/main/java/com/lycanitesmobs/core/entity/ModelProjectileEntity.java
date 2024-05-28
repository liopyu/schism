package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.client.TextureManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ModelProjectileEntity extends BaseProjectileEntity {
	public ModelProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public ModelProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, World world, LivingEntity entityLiving) {
		super(entityType, world, entityLiving);
	}

	public ModelProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, World world, double x, double y, double z) {
		super(entityType, world, x, y, z);
	}

	@Override
	public String getTextureName() {
	return this.entityName.toLowerCase();
	}

	@Override
	public ResourceLocation getTexture() {
		if(TextureManager.getTexture(this.getTextureName()) == null)
			TextureManager.addTexture(this.getTextureName(), this.modInfo, "textures/projectile/" + this.getTextureName() + ".png");
		return TextureManager.getTexture(this.getTextureName());
	}
}
