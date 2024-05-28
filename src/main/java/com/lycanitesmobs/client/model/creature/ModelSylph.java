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
public class ModelSylph extends ModelTemplateElemental {

    public ModelSylph() {
        this(1.0F);
    }

    public ModelSylph(float shadowSize) {

		// Load Model:
		this.initModel("sylph", LycanitesMobs.modInfo, "entity/sylph");

		// Trophy:
		this.trophyScale = 1.2F;
		this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }

	@Override
	public void addCustomLayers(CreatureRenderer renderer) {
		super.addCustomLayers(renderer);
		renderer.addLayer(new LayerCreatureEffect(renderer, "hood", false, CustomRenderStates.BLEND.NORMAL.id, true));
		renderer.addLayer(new LayerCreatureScrolling(renderer, "wing", true, CustomRenderStates.BLEND.ADD.id, true, new Vector2f(0, 1)));
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
	public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
		super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
		if(this.currentModelState != null) {
			this.currentModelState.attackAnimationSpeed = 0.08F;
		}

		// Idle:
		if (partName.contains("wingleft")) {
			float attackAngle = 30;
			if(partName.equals("wingleft02")) {
				attackAngle = 50;
			}
			else if(partName.equals("wingleft03")) {
				attackAngle = 45;
			}
			this.rotate(
					0,
					5 + (float)Math.toDegrees(MathHelper.sin(loop * 0.2F) * 0.4F),
					4 + (float)Math.toDegrees(MathHelper.sin(loop * 0.2F) * 0.08F) + (attackAngle * this.getAttackProgress())
			);
		}
		else if (partName.contains("wingright")) {
			float attackAngle = -30;
			if(partName.equals("wingright02")) {
				attackAngle = -50;
			}
			else if(partName.equals("wingright03")) {
				attackAngle = -45;
			}
			this.rotate(
					0,
					-5 + (float)Math.toDegrees(MathHelper.sin(loop * 0.2F + (float)Math.PI) * 0.4F),
					-4 + (float)Math.toDegrees(MathHelper.sin(loop * 0.2F + (float)Math.PI) * 0.08F) + (attackAngle * this.getAttackProgress())
			);
		}

		// Fingers:
		else if(partName.contains("finger")) {
			if(partName.contains("thumb")) {
				this.rotate(-(float) Math.toDegrees(MathHelper.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
			}
			else {
				this.rotate((float) Math.toDegrees(MathHelper.cos(loop * 0.2F) * 0.2F - 0.2F), 0, 0);
			}
		}
	}

	@Override
	public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
		if (partName.contains("hood")) {
			return layer != null && "hood".equals(layer.name);
		}
		if (partName.contains("wing")) {
			return layer != null && "wing".equals(layer.name);
		}
		return layer == null;
	}

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
