package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelCinder extends ModelTemplateElemental {
    public ModelCinder() {
        this(1.0F);
    }
    
    public ModelCinder(float shadowSize) {

		// Load Model:
		this.initModel("cinder", LycanitesMobs.modInfo, "entity/cinder");

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "eyes"));
	}

	@Override
	public int getBrightness(String partName, LayerCreatureBase layer, BaseCreatureEntity entity, int brightness) {
		return ClientManager.FULL_BRIGHT;
	}

	@Override
	public boolean getGlow(BaseCreatureEntity entity, LayerCreatureBase layer) {
		if(layer != null) {
			return super.getGlow(entity, layer);
		}
		return true;
	}

	@Override
	public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
    	if(partName.contains("effect")) {
    		return super.getBaseTextureOffset(partName, entity, trophy, loop);
		}
		return new Vector2f(loop, 0);
	}
}
