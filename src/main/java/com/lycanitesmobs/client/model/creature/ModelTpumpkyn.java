package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.CustomRenderStates;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureEffect;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureScrolling;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelTpumpkyn extends ModelTemplateQuadruped {

    public ModelTpumpkyn() {
        this(1.0F);
    }

    public ModelTpumpkyn(float shadowSize) {
    	this.initModel("tpumpkyn", LycanitesMobs.modInfo, "entity/tpumpkyn");

        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, -0.2F, 0.0F};
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureScrolling(renderer, "effect", true, CustomRenderStates.BLEND.NORMAL.id, false, new Vector2f(0, 2)));
	}

    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {

    	// Standing:
		if (distance < 0.1F && !((BaseCreatureEntity)entity).hasAttackTarget()) {
			if (partName.contains("body")) {
				this.translate(0, -0.25F, 0);
			}
			if (partName.contains("leg")) {
				this.scale(distance, distance, distance);
			}
			if (partName.equals("eyes") || partName.equals("effect")) {
				this.scale(0, 0, 0);
			}
			if (partName.equals("mouth")) {
				this.translate(0, 0.1F, 0);
			}
			return;
		}

    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

		if (partName.contains("hair")) {
			float rotZ = (float)Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
			float rotX = (float)Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
			this.rotate(rotX, 0, rotZ);
		}
    }

	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if (partName.contains("effect") || partName.contains("eye")) {
			return layer != null && "effect".equals(layer.name);
		}
		return layer == null;
	}
}
