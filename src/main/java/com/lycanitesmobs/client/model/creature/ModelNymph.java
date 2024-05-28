package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelNymph extends ModelTemplateElemental {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelNymph() {
        this(1.0F);
    }

    public ModelNymph(float shadowSize) {

		// Load Model:
		this.initModel("nymph", LycanitesMobs.modInfo, "entity/nymph");

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }


	// ==================================================
	//             Add Custom Render Layers
	// ==================================================
	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "overlay"));
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


	// ==================================================
	//                 Animate Part
	// ==================================================
	float maxLeg = 0F;
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Idle:
		if (partName.equals("wingleftbottom") || partName.equals("winglefttop")) {
			this.rotate(0, 10 + (float)Math.toDegrees(MathHelper.sin(loop * 0.2F) * 0.4F), 0);
		}
		else if (partName.equals("wingrightbottom") || partName.equals("wingrighttop")) {
			this.rotate(0, -10 + (float)Math.toDegrees(MathHelper.sin(loop * 0.2F + (float)Math.PI) * 0.4F), 0);
		}
		else if(partName.equals("hairleft")) {
			this.rotate(
					-(float)Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F),
					0,
					-(float)Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.1F)
			);
		}
		else if(partName.equals("hairright")) {
			this.rotate(
					(float)Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F),
					0,
					(float)Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.1F)
			);
		}
	}


	// ==================================================
	//                Get Part Color
	// ==================================================
	/** Returns the coloring to be used for this part and layer. **/
	@Override
	public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
		if(layer == null) {
			float glowSpeed = 40;
			float glow = loop * glowSpeed % 360;
			float color = ((float)Math.cos(Math.toRadians(glow)) * 0.1f) + 0.9f;
			return new Vector4f(color, color, color, 1);
		}

		return super.getPartColor(partName, entity, layer, trophy, loop);
	}
}
