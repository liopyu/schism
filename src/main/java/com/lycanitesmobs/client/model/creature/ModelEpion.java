package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelEpion extends CreatureObjModel {
    public ModelEpion() {
        this(1.0F);
    }
    
    public ModelEpion(float shadowSize) {
    	// Load Model:
		this.initModel("epion", LycanitesMobs.modInfo, "entity/epion");
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.2F};
    }

    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    	float posX = 0F;
    	float posY = 0F;
    	float posZ = 0F;
    	float rotX = 0F;
    	float rotY = 0F;
    	float rotZ = 0F;

		// Idle:
		if(partName.equals("mouth")) {
			this.rotate((float)-Math.toDegrees(MathHelper.cos(loop * 0.1F) * 0.1F - 0.1F), 0.0F, 0.0F);
		}
		if(partName.equals("earleft")) {
			rotZ -= Math.toDegrees(MathHelper.cos(loop * 0.18F) * 0.05F + 0.05F);
			rotX -= Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
		}
		if(partName.equals("earright")) {
			rotZ += Math.toDegrees(MathHelper.cos(loop * 0.18F) * 0.05F + 0.05F);
			rotX += Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
		}
		if(entity.isVehicle()) {
			if(partName.equals("earleft") || partName.equals("earright")) {
				rotX -= 30;
			}
		}

		// Walking:
		if(entity.isOnGround() || entity.isInWater()) {
			float walkSwing = 0.6F;
			if(partName.equals("wingleft01")) {
				rotZ -= 29;
				rotY += Math.toDegrees(MathHelper.cos(time * walkSwing + (float) Math.PI) * 0.1F * distance);
			}
			if(partName.equals("wingright01")) {
				rotZ += 29;
				rotY += (Math.toDegrees(MathHelper.cos(time * walkSwing) * 0.1F) - 10 * distance);
			}
			if(partName.equals("wingleft02")) {
				rotZ += 120;
			}
			if(partName.equals("wingright02")) {
				rotZ -= 120;
			}
		}

		// Flying:
		else {
			if (partName.equals("body")) {
				float bob = -MathHelper.sin(loop * 0.2F) * (entity.isVehicle() ? 0.1F : 0.6F);
				if (bob < 0) bob = -bob;
				posY += bob;
			}
			if (partName.contains("wingleft")) {
				rotZ -= Math.toDegrees(MathHelper.sin(loop * 0.4F) * 0.6F);
			}
			if (partName.contains("wingright")) {
				rotZ -= Math.toDegrees(MathHelper.sin(loop * 0.4F + (float) Math.PI) * 0.6F);
			}
			if (partName.equals("wingleft01")) {
				rotX -= Math.toDegrees(MathHelper.sin(loop * 0.4F) * 0.1F);
			}
			if (partName.equals("wingright01")) {
				rotX -= Math.toDegrees(MathHelper.sin(loop * 0.4F) * 0.1F);
			}
		}

		// Apply Animations:
		translate(posX, posY, posZ);
		rotate(rotX, rotY, rotZ);
    }
}
