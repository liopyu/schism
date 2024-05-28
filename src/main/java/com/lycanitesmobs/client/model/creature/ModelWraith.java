package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureScrolling;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelWraith extends ModelTemplateElemental {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelWraith() {
        this(1.0F);
    }

    public ModelWraith(float shadowSize) {

		// Load Model:
		this.initModel("wraith", LycanitesMobs.modInfo, "entity/wraith");

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
		renderer.addLayer(new LayerCreatureEffect(renderer, "overlay", true, CustomRenderStates.BLEND.NORMAL.id, true));
		renderer.addLayer(new LayerCreatureEffect(renderer, "skull", false, CustomRenderStates.BLEND.NORMAL.id, true));
		renderer.addLayer(new LayerCreatureScrolling(renderer, "", true, CustomRenderStates.BLEND.ADD.id, true, new Vector2f(-8, 0)));
	}

	@Override
	public int getBrightness(String partName, LayerCreatureBase layer, BaseCreatureEntity entity, int brightness) {
		return ClientManager.FULL_BRIGHT;
	}


	// ==================================================
	//                 Animate Part
	// ==================================================
	@Override
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		// Idle:
		if(entity instanceof BaseCreatureEntity) {
			BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
			if (entityCreature.hasAttackTarget() && partName.equals("mouth")) {
				this.rotate(20 + (float)-Math.toDegrees(MathHelper.cos(loop) * 0.1F), 0.0F, 0.0F);
			}
		}

		// Vibrate:
		float vibration = loop * 2;
		if("head".equals(partName)) {
			this.translate(MathHelper.cos(vibration) * 0.01f, MathHelper.cos(vibration) * 0.01f, MathHelper.cos(vibration) * 0.01f);
		}
		else if("mouth".equals(partName)) {
			this.rotate((float)-Math.toDegrees(MathHelper.cos(vibration) * 0.025F), 0.0F, 0.0F);
		}
	}


	// ==================================================
	//                Can Render Part
	// ==================================================
	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if("head".equals(partName)) {
			return layer == null || "overlay".equals(layer.name);
		}
		if("fire".equals(partName)) {
			return layer != null && layer instanceof LayerCreatureScrolling;
		}
		return layer != null && "skull".equals(layer.name);
	}


	// ==================================================
	//              Get Part Texture Offset
	// ==================================================
	@Override
	public Vector2f getBaseTextureOffset(String partName, Entity entity, boolean trophy, float loop) {
    	return new Vector2f(-loop * 8, 0);
	}


	// ==================================================
	//                Get Part Color
	// ==================================================
	/** Returns the coloring to be used for this part and layer. **/
	public Vector4f getPartColor(String partName, Entity entity, LayerCreatureBase layer, boolean trophy, float loop) {
		if(layer == null || layer instanceof LayerCreatureScrolling) {
			float glowSpeed = 80;
			float glow = loop * glowSpeed % 360;
			float color = ((float)Math.cos(Math.toRadians(glow)) * 0.1f) + 0.9f;
			float alpha = 1.0f;
			if("fire".equals(partName)) {
				alpha = 1f;
			}
			return new Vector4f(color, color, color, alpha);
		}

		return super.getPartColor(partName, entity, layer, trophy, loop);
	}

}
