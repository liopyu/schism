package com.lycanitesmobs.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigClient {
	public static ConfigClient INSTANCE;

	public final ForgeConfigSpec.ConfigValue<Boolean> modelMultipass;
	public final ForgeConfigSpec.ConfigValue<Boolean> inventoryTab;

	public ConfigClient(ForgeConfigSpec.Builder builder) {
		builder.push("Client");
		builder.comment("Client side settings, defaults are recommended except for low end systems or for compatibility with mods that alter rendering.");

		this.modelMultipass = builder
				.comment("Set to false to disable multipass rendering. This renders model layers twice so that they can show each over through alpha textures, disable for performance on low end systems.")
				.translation(CoreConfig.CONFIG_PREFIX + "client.modelMultipass")
				.define("modelMultipass", true);

		this.inventoryTab = builder
				.comment("Set to false to disable the inventory tabs added by this mod.")
				.translation(CoreConfig.CONFIG_PREFIX + "client.inventoryTab")
				.define("inventoryTab", true);

		builder.pop();
	}
}
