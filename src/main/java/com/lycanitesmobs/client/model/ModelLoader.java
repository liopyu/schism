package com.lycanitesmobs.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class ModelLoader implements IModelLoader {
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		// Loaded by ModelManager instead.
	}

	@Override
	public IModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
		return null;
	}
}
