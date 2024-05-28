package com.lycanitesmobs.core;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.stats.IStatFormatter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class StatManager {
	private static StatManager INSTANCE;
	public static StatManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new StatManager();
		}
		return INSTANCE;
	}

	public Map<String, StatType<ResourceLocation>> statTypes = new HashMap<>();

	/**
	 * Creates all base Stat Types.
	 */
	public void createStatTypes() {
		StatManager.getInstance().addStatType("learn");
		StatManager.getInstance().addStatType("summon");
	}

	/**
	 * Adds a new Stat Type. TODO Implement stats.
	 * @param name The unique name of the stat type.
	 */
	public void addStatType(String name) {
//		Registry<ResourceLocation> statRegistry = new SimpleRegistry<>();
//		StatType<ResourceLocation> statType = new StatType<>(statRegistry);
//		statType.setRegistryName(LycanitesMobs.modInfo.modid, name);
//		this.statTypes.put(name, statType);
	}

	/**
	 * Gets a from a stat type.
	 * @param typeName The stat type name.
	 * @param name The stat name.
	 * @return The stat instance or null.
	 */
	@Nullable
	public Stat getStat(String typeName, String name) {
		if(!this.statTypes.containsKey(typeName)) {
			return null;
		}
		return this.statTypes.get(typeName).get(new ResourceLocation(LycanitesMobs.modInfo.modid, name), IStatFormatter.DEFAULT);
	}

	@SubscribeEvent
	public void registerStats(RegistryEvent.Register<StatType<?>> event) {
		this.createStatTypes();
		for(StatType statType : this.statTypes.values()) {
			event.getRegistry().register(statType);
		}
	}
}
