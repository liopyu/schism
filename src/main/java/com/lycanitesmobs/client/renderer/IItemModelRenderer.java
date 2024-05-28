package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.client.renderer.layer.LayerItem;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IItemModelRenderer {
	void bindItemTexture(ResourceLocation location);

	List<LayerItem> addLayer(LayerItem renderLayer);
}
