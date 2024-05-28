package com.lycanitesmobs.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface IProxy {
	void registerEvents();

	World getWorld();

	void addEntityToWorld(int entityId, Entity entity);

	public PlayerEntity getClientPlayer();

	public void openScreen(int screenId, PlayerEntity player);
}
