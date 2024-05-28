package com.lycanitesmobs.client.localisation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class LanguageLoader implements ISelectiveResourceReloadListener {
	public static LanguageLoader INSTANCE;

	protected Map<String, String> map = new HashMap<>();

	/** Returns the main Item Manager instance or creates it and returns it. **/
	public static LanguageLoader getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new LanguageLoader();
		}
		return INSTANCE;
	}


	/**
	 * Called when the Resource Manager is reloaded included the initial load up of the game.
	 * @param resourceManager The resource manager instance.
	 */
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		LanguageManager.getInstance().loadLanguage(Minecraft.getInstance().options.languageCode);
	}
}
